package com.ulfy.android.task;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 分页加载列表数据的 ui 任务，通过传入执行体代码块与响应器即可拼装成一个
 * 完整的任务。任务在执行过程中会在任务的不同阶段通过响应器来响应。
 */
public final class LoadListPageUiTask extends UiTask {
	public static final int LOAD_START_PAGE = 1;				// 当前状态为加载第一页的状态
	public static final int LOAD_NEXT_PAGE = 2;					// 当前状态为加载下一页的状态
	public static final int LOAD_RELOAD_ALL = 3;				// 当前状态为重新加载已有的页面的状态
	public static int DEFAULT_START_PAGE = 1;					// 起始页默认为1
	public static int DEFAULT_PAGE_SIZE = 20;					// 每页默认大小为20

	private LoadListPageUiTaskInfo taskInfo;					// 任务处理的信息（不能为空）
	private OnLoadListPage loadListPageBody;					// 任务执行代码块，如果没有则会执行执行成功
	private Transponder transponder;							// 任务执行过程中的应答器，没有则不执行响应

	public LoadListPageUiTask(Context context, Transponder transponder) {
		this(context, null, null, transponder);
	}

	public LoadListPageUiTask(Context context, LoadListPageUiTaskInfo taskInfo, OnLoadListPage loadListPageBody, Transponder transponder) {
		super(context);
		this.taskInfo = taskInfo;
		this.loadListPageBody = loadListPageBody;
		this.transponder = transponder;
	}

	public final void setTaskInfo(LoadListPageUiTaskInfo taskInfo) {
		this.taskInfo = taskInfo;
	}

	public final LoadListPageUiTaskInfo getTaskInfo() {
		return taskInfo;
	}

	public final void setLoadListPageBody(OnLoadListPage loadListPageBody) {
		this.loadListPageBody = loadListPageBody;
	}

	@Override protected final void run(Task task) {
		if (isCancelUiHandler()) {		// 不执行已经取消更新 ui 的任务
			return;
		}
		if (loadListPageBody == null) {
			notifySuccess(TaskConfig.Config.LOAD_DATA_SUCCESS_TIP);
		} else {
			Objects.requireNonNull(taskInfo, "task info can not be null");
			loadListPageBody.onLoadListPage(this, taskInfo.getDataList(), taskInfo.getTempList(), taskInfo.getLoadFromPage(), taskInfo.getLoadToPage(), taskInfo.getPageSize());
		}
	}

	/**
	 * 通知任务启动了
	 */
	public final void notifyStart(final Object tipData) {
		if (transponder == null) {		// 没有设置响应器则不执行
			return;
		}
		runOnUiThread(() -> transponder.onTranspondMessage(new Message(Message.TYPE_START, tipData)));
	}

	/**
	 * 通知任务成功了
	 */
	public final void notifySuccess(final Object tipData) {
		runOnUiThread(() -> {
			taskInfo.combileTempListToDataList();
			if (transponder != null) {
				transponder.onTranspondMessage(new Message(Message.TYPE_SUCCESS, tipData));
				transponder.onTranspondMessage(new Message(Message.TYPE_FINISH, tipData));
			}
		});
	}

	/**
	 * 通知任务失败了
	 */
	public final void notifyFail(Exception e) {
		notifyFail(e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
	}

	/**
	 * 通知任务失败了
	 */
	public final void notifyFail(final Object tipData) {
		runOnUiThread(() -> {
			taskInfo.clearTemp();
			if (transponder != null) {
				transponder.onTranspondMessage(new Message(Message.TYPE_FAIL, tipData));
				transponder.onTranspondMessage(new Message(Message.TYPE_FINISH, tipData));
			}
		});
	}

	/**
	 * 将加载分页任务中需要持久维护的信息抽象到外部中
	 * 隔绝客户端于人物的直接依赖，使得客户端只需要知道一个任务必要的信息
	 */
	public final static class LoadListPageUiTaskInfo<D> {
		// 在没有设置最后一页的情况下可以考虑以 tempList 大小是否为 0 来默认判断是否到底了
		private static final int END_PAGE_NO_POINT = -1;				// 尾页未指定（这时会自动判断是否到底了）
		private List<D> dataList;										// 真正的数据容器
		private List<D> tempList;										// 临时存储结果的容器（加载完毕后会合并到真实数据中）

		private int startPage = DEFAULT_START_PAGE;						// 起始页
		private int pageSize = DEFAULT_PAGE_SIZE;						// 每页大小

		private int loadingStatus;										// 当前所处的加载状态
		private int currentPage;										// 当前已经加载的页码指针（初始状态下为起始页的前一页）
		private int toPagePointer;										// 加载至页码指针
		private int endPage = END_PAGE_NO_POINT;						// 最后一页的页码，这个也可以不设置。-1 表示没有设置结束页做小设置为 0
		private boolean isLoadTempListEmpty;							// 是否加载了一个空的临时列表，如果没有最后页码设置，则已该边变量作为是否到底的依据

		private OnPageCombineListener<D> onPageCombineListener;			// 当分页合并后的回调

		public LoadListPageUiTaskInfo(List<D> dataList) {
			Objects.requireNonNull(dataList, "data list can not be null");
			this.dataList = dataList;
			this.tempList = new ArrayList<>();
			loadStartPage();					// 新的任务默认为加载起始页状态
		}

		/*
		设置获取数据状态
		 */

		public final LoadListPageUiTaskInfo loadStartPage() {
			this.loadingStatus = LOAD_START_PAGE;
			this.currentPage = startPage - 1;
			this.toPagePointer = this.currentPage + 1;
			this.endPage = END_PAGE_NO_POINT;
			isLoadTempListEmpty = false;
			return this;
		}

		public final LoadListPageUiTaskInfo loadNextPage() {
			this.loadingStatus = LOAD_NEXT_PAGE;
			this.toPagePointer = this.currentPage + 1;
			return this;
		}

		public final LoadListPageUiTaskInfo reloadAllPage() {
			this.loadingStatus = LOAD_RELOAD_ALL;
			this.toPagePointer = this.currentPage;
			this.currentPage = startPage - 1;
			return this;
		}

		/*
		数据合并与清除
		 */

		final void combileTempListToDataList() {
			// 加载起始页或者重新加载
			if (loadingStatus == LOAD_START_PAGE || loadingStatus == LOAD_RELOAD_ALL) {
				dataList.clear();
				dataList.addAll(tempList);
			}
			// 加载下一页
			else if (loadingStatus == LOAD_NEXT_PAGE) {
				isLoadTempListEmpty = tempList.isEmpty();
				dataList.addAll(tempList);
			}
			// 如果加载了数据则回调数据合并接口
			if (onPageCombineListener != null) {
				onPageCombineListener.onPageCombined(this, dataList);
			}
			// 加载完毕之后设置状态
			tempList.clear();
			currentPage = toPagePointer;
		}

		final void clearTemp() {
			tempList.clear();
		}


		/*
		修改初始设置
		 */

		public final LoadListPageUiTaskInfo resetStartPage(int startPage) {
			this.startPage = startPage;
			this.currentPage = startPage - 1;
			this.toPagePointer = startPage;
			return this;
		}

		public final LoadListPageUiTaskInfo setPageSize(int pageSize) {
			this.pageSize = pageSize;
			return this;
		}

		public final int getPageSize() {
			return pageSize;
		}

		/**
		 * 设置当前已经加载过的页的最大页码
		 * 		由于加载下一页时会自动+1，因此这里需要-1；该指针表示的是已经加载出来的最大页的页码
		 * 		如：没有加载过数据时，设置当前页为0，则加载时会自动切到1取加载
		 */
		public final LoadListPageUiTaskInfo setCurrentPointer(int currentPointer) {
			this.currentPage = currentPointer;
			correctPagePointer();
			return this;
		}

		public final int getCurrentPage() {
			return currentPage;
		}

		public final LoadListPageUiTaskInfo moveCurrentPointer(int offset) {
			this.currentPage += offset;
			correctPagePointer();
			return this;
		}

		private void correctPagePointer() {
			if (this.currentPage < this.startPage - 1) {
				this.currentPage = this.startPage - 1;
			}
			if (this.currentPage >= this.toPagePointer) {
				this.toPagePointer = this.currentPage + 1;
			}
		}

		public final LoadListPageUiTaskInfo setOnPageCombineListener(OnPageCombineListener<D> onPageCombineListener) {
			this.onPageCombineListener = onPageCombineListener;
			return this;
		}

		/**
		 * 设置最后一页的页码
		 */
		public final LoadListPageUiTaskInfo setEndPage(int endPage) {
			this.endPage = endPage;
			return this;
		}

		/**
		 * 设置末尾页的页码，该方法将会自动计算出最后一页的页码
		 */
		public final LoadListPageUiTaskInfo setEndIndex(int endIndex) {
			endPage = (int) (startPage + Math.ceil(endIndex * 1.0f / pageSize) - 1);
			return this;
		}

		/**
		 * 是否已经加载了最后一页的数据，该方法应该在任务生命周期回调中使用，并且必须设置最后一页的页码才能得到正确的判断
		 * 判断依据：本次加载数据小于页大小、达到指定的最后一页、尝试加载一下页时无数据
		 */
		public final boolean isLoadedEndPage() {
			if (endPage == END_PAGE_NO_POINT) {
				return isLoadTempListEmpty;
			} else {
				return endPage >= 0 && currentPage >= endPage;
			}
		}

		/*
		获取内部存储数据
		 */

		public final List<D> getDataList() {
			return dataList;
		}

		final List<D> getTempList() {
			return tempList;
		}

		/*
		获取加载时的页码区间
		 */

		final int getLoadFromPage() {
			return currentPage + 1;
		}

		final int getLoadToPage() {
			return toPagePointer;
		}

		/*
		获取页码和大小，与加载状态
		 */

		final int getLoadingStatus() {
			return loadingStatus;
		}

		/**
		 * 当分页合并后的回调，只有在当前分页有数据时才会回调
		 */
		public interface OnPageCombineListener<D> {
			void onPageCombined(LoadListPageUiTaskInfo<D> loadListPageUiTaskInfo, List<D> dataList);
		}
	}

	/**
	 *	加载单页数据
	 */
	public interface OnLoadListPage {
		/**
		 * 加载一页的回调
		 */
		void onLoadListPage(LoadListPageUiTask task, List<Object> dataList, List<Object> tempList, int fromPage, int toPage, int pageSize);
	}

	/**
	 * 对OnLoadListPage的一个默认实现
	 */
	public static abstract class OnLoadSimpleListPage implements OnLoadListPage {
		@Override public final void onLoadListPage(LoadListPageUiTask task, List<Object> dataList, List<Object> tempList, int fromPage, int toPage, int pageSize) {
			//-- 开始阶段
			if (task.taskInfo.getLoadingStatus() == LoadListPageUiTask.LOAD_START_PAGE) {
				task.notifyStart(TaskConfig.Config.LOAD_LIST_PAGE_START_TIP);
			} else if (task.taskInfo.getLoadingStatus() == LoadListPageUiTask.LOAD_RELOAD_ALL) {
				task.notifyStart(TaskConfig.Config.LOAD_LIST_PAGE_START_TIP);
			} else if (task.taskInfo.getLoadingStatus() == LoadListPageUiTask.LOAD_NEXT_PAGE) {
				task.notifyStart(TaskConfig.Config.LOAD_LIST_PAGE_START_TIP);
			}
			//-- 加载具体数据阶段
			for (int page = fromPage; page <= toPage; page++) {
				try {
					loadSimplePage(task, dataList, tempList, page, pageSize);
				} catch (Exception e) {
					e.printStackTrace();
					task.notifyFail(e);
					return;
				}
			}
			//-- 扫尾工作阶段
			if (task.taskInfo.getLoadingStatus() == LoadListPageUiTask.LOAD_START_PAGE) {
				task.notifySuccess(TaskConfig.Config.LOAD_LIST_PAGE_SUCCESS_START_PAGE_TIP);
			} else if (task.taskInfo.getLoadingStatus() == LoadListPageUiTask.LOAD_RELOAD_ALL) {
				task.notifySuccess(TaskConfig.Config.LOAD_LIST_PAGE_SUCCESS_RELOAD_ALL_TIP);
			} else if (task.taskInfo.getLoadingStatus() == LoadListPageUiTask.LOAD_NEXT_PAGE) {
				task.notifySuccess(TaskConfig.Config.LOAD_LIST_PAGE_SUCCESS_NEXT_PAGE_TIP);
			}
		}

		/**
		 * 加载每一页的具体方法
		 */
		protected abstract void loadSimplePage(LoadListPageUiTask task, List<Object> modelList, List<Object> tempList, int page, int pageSize) throws Exception;
	}

}
