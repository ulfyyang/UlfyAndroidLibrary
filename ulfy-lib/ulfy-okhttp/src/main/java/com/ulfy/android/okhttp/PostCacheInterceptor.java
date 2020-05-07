package com.ulfy.android.okhttp;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Internal;
import okhttp3.internal.cache.CacheRequest;
import okhttp3.internal.cache.CacheStrategy;
import okhttp3.internal.http.HttpCodec;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.http.RealResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import okio.Source;
import okio.Timeout;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static okhttp3.internal.Util.closeQuietly;
import static okhttp3.internal.Util.discard;

/**
 * post缓存，从应用拦截器层，拦截
 * 		具体可参照CacheInterceptor.java类
 */
class PostCacheInterceptor implements Interceptor {
	final NetPostCache cache;

	PostCacheInterceptor(NetPostCache cache) {
		this.cache = cache;
	}

	@Override public Response intercept(Chain chain) throws IOException {
		final Request request = chain.request();

		// 如果是post请求，并且设置了缓存，则在这里进行拦截，如果缓存有效，后续的拦截器将不执行
		if ("POST".equalsIgnoreCase(request.method()) && (null != request.cacheControl() && !request.cacheControl().noStore())) {

			// 强制刷新，删除旧有post缓存
			checkForceRefresh(request);

			// 以下代码逻辑来家：okhttp3 源码中的 CacheInterceptor.java，模拟其运行
			Response cacheCandidate = cache != null ? cache.get(request) : null;
			long now = System.currentTimeMillis();
			CacheStrategy strategy = new CacheStrategy.Factory(now, chain.request(), cacheCandidate).get();
			Request networkRequest = strategy.networkRequest;
			Response cacheResponse = strategy.cacheResponse;

			if (cache != null) {
				cache.trackResponse(strategy);
			}

			if (cacheCandidate != null && cacheResponse == null) {
				closeQuietly(cacheCandidate.body()); // The cache candidate wasn't applicable. Close it.
			}

			// 有则从缓存中返回，直接跳过后面的拦截器，不访问网络了
			// If we don't need the network, we're done.
			if (networkRequest == null && cacheResponse != null) {
				return cacheResponse.newBuilder()
						.cacheResponse(stripBody(cacheResponse))
						.build();
			}

			Response networkResponse = null;
			try {
				// 执行网络请求，并包装一下
				networkResponse = chain.proceed(request);
			} finally {
				// If we're crashing on I/O or otherwise, don't leak the cache body.
				if (networkResponse == null && cacheCandidate != null) {
					closeQuietly(cacheCandidate.body());
				}
			}

			try {
				if (cache != null) {
					if (HttpHeaders.hasBody(networkResponse)) {
						CacheRequest cacheRequest = cache.put(networkResponse);
						networkResponse = cacheWritingResponse(cacheRequest, networkResponse);
					}
				}
			} catch (Exception e) { }

			// 当 onSuccess调用时，会写入缓存
			return networkResponse;
		}

		return chain.proceed(request);
	}

	/**
	 * 如果当前 post 是强制刷新 即：noCache @see，即：删除旧有缓存
	 */
	private void checkForceRefresh(Request request) {
		try {
			if (request.cacheControl().noCache()) {
				cache.remove(request);
			}
		} catch (Exception e) {
		}
	}

	// 以下摘自 okhttp中的CacheInterceptor
	private static Response stripBody(Response response) {
		return response != null && response.body() != null
				? response.newBuilder().body(null).build()
				: response;
	}

	/**
	 * Returns a new source that writes bytes to {@code cacheRequest} as they are read by the source
	 * consumer. This is careful to discard bytes left over when the stream is closed; otherwise we
	 * may never exhaust the source stream and therefore not complete the cached response.
	 */
	private Response cacheWritingResponse(final CacheRequest cacheRequest, Response response)
			throws IOException {
		// Some apps return a null body; for compatibility we treat that like a null cache request.
		if (cacheRequest == null) return response;
		Sink cacheBodyUnbuffered = cacheRequest.body();
		if (cacheBodyUnbuffered == null) return response;

		final BufferedSource source = response.body().source();
		final BufferedSink cacheBody = Okio.buffer(cacheBodyUnbuffered);

		Source cacheWritingSource = new Source() {
			boolean cacheRequestClosed;

			@Override
			public long read(Buffer sink, long byteCount) throws IOException {
				long bytesRead;
				try {
					bytesRead = source.read(sink, byteCount);
				} catch (IOException e) {
					if (!cacheRequestClosed) {
						cacheRequestClosed = true;
						cacheRequest.abort(); // Failed to write a complete cache response.
					}
					throw e;
				}

				if (bytesRead == -1) {
					if (!cacheRequestClosed) {
						cacheRequestClosed = true;
						cacheBody.close(); // The cache response is complete!
					}
					return -1;
				}

				sink.copyTo(cacheBody.buffer(), sink.size() - bytesRead, bytesRead);
				cacheBody.emitCompleteSegments();
				return bytesRead;
			}

			@Override
			public Timeout timeout() {
				return source.timeout();
			}

			@Override
			public void close() throws IOException {
				if (!cacheRequestClosed
						&& !discard(this, HttpCodec.DISCARD_STREAM_TIMEOUT_MILLIS, MILLISECONDS)) {
					cacheRequestClosed = true;
					cacheRequest.abort();
				}
				source.close();
			}
		};

		String contentType = response.header("Content-Type");
		long contentLength = response.body().contentLength();
		return response.newBuilder()
				.body(new RealResponseBody(contentType, contentLength, Okio.buffer(cacheWritingSource)))
				.build();
	}


	/** Combines cached headers with a network headers as defined by RFC 7234, 4.3.4. */
	private static Headers combine(Headers cachedHeaders, Headers networkHeaders) {
		Headers.Builder result = new Headers.Builder();

		for (int i = 0, size = cachedHeaders.size(); i < size; i++) {
			String fieldName = cachedHeaders.name(i);
			String value = cachedHeaders.value(i);
			if ("Warning".equalsIgnoreCase(fieldName) && value.startsWith("1")) {
				continue; // Drop 100-level freshness warnings.
			}
			if (!isEndToEnd(fieldName) || networkHeaders.get(fieldName) == null) {
				Internal.instance.addLenient(result, fieldName, value);
			}
		}

		for (int i = 0, size = networkHeaders.size(); i < size; i++) {
			String fieldName = networkHeaders.name(i);
			if ("Content-Length".equalsIgnoreCase(fieldName)) {
				continue; // Ignore content-length headers of validating responses.
			}
			if (isEndToEnd(fieldName)) {
				Internal.instance.addLenient(result, fieldName, networkHeaders.value(i));
			}
		}

		return result.build();
	}


	/**
	 * Returns true if {@code fieldName} is an end-to-end HTTP header, as defined by RFC 2616,
	 * 13.5.1.
	 */
	static boolean isEndToEnd(String fieldName) {
		return !"Connection".equalsIgnoreCase(fieldName)
				&& !"Keep-Alive".equalsIgnoreCase(fieldName)
				&& !"Proxy-Authenticate".equalsIgnoreCase(fieldName)
				&& !"Proxy-Authorization".equalsIgnoreCase(fieldName)
				&& !"TE".equalsIgnoreCase(fieldName)
				&& !"Trailers".equalsIgnoreCase(fieldName)
				&& !"Transfer-Encoding".equalsIgnoreCase(fieldName)
				&& !"Upgrade".equalsIgnoreCase(fieldName);
	}
}
