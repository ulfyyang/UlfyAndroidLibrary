package com.bumptech.glide.integration.okhttp3;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.OkHttpClient;

/** A simple model loader for fetching media over http/https using OkHttp. */
public class OkHttpUrlLoader implements ModelLoader<GlideUrl, InputStream> {

    private final Call.Factory client;

    // Public API.
    @SuppressWarnings("WeakerAccess")
    public OkHttpUrlLoader(Call.Factory client) {
        this.client = client;
    }

    @Override
    public boolean handles(GlideUrl url) {
        return true;
    }

    @Override
    public LoadData<InputStream> buildLoadData(
            GlideUrl model, int width, int height, Options options) {
        return new LoadData<>(model, new OkHttpStreamFetcher(client, model));
    }

    /** The default factory for {@link OkHttpUrlLoader}s. */
    // Public API.
    @SuppressWarnings("WeakerAccess")
    public static class Factory implements ModelLoaderFactory<GlideUrl, InputStream> {
        private static volatile Call.Factory internalClient;
        private final Call.Factory client;

        private static Call.Factory getInternalClient() {
            if (internalClient == null) {
                synchronized (Factory.class) {
                    if (internalClient == null) {

                        X509TrustManager xtm = new X509TrustManager() {
                            @Override public void checkClientTrusted(X509Certificate[] chain, String authType) { }
                            @Override public void checkServerTrusted(X509Certificate[] chain, String authType) { }
                            @Override public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        };

                        SSLSocketFactory sslSocketFactory = null;

                        try {
                            SSLContext sslContext = SSLContext.getInstance("SSL");
                            sslContext.init(null, new TrustManager[]{xtm}, new SecureRandom());
                            sslSocketFactory = sslContext.getSocketFactory();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                            @Override public boolean verify(String hostname, SSLSession session) {
                                return true;
                            }
                        };

                        OkHttpClient.Builder builder = new OkHttpClient.Builder();
                        if (sslSocketFactory != null) {
                            builder.sslSocketFactory(sslSocketFactory, xtm);
                        }
                        builder.hostnameVerifier(hostnameVerifier);

                        internalClient = builder.build();
                    }
                }
            }
            return internalClient;
        }

        /** Constructor for a new Factory that runs requests using a static singleton client. */
        public Factory() {
            this(getInternalClient());
        }

        /**
         * Constructor for a new Factory that runs requests using given client.
         *
         * @param client this is typically an instance of {@code OkHttpClient}.
         */
        public Factory(Call.Factory client) {
            this.client = client;
        }

        @Override
        public ModelLoader<GlideUrl, InputStream> build(MultiModelLoaderFactory multiFactory) {
            return new OkHttpUrlLoader(client);
        }

        @Override
        public void teardown() {
            // Do nothing, this instance doesn't own the client.
        }
    }
}