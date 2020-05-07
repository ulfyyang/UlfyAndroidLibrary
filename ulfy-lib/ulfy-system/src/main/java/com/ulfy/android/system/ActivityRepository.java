package com.ulfy.android.system;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

class ActivityRepository {

	//-- single instance start --

	private final static ActivityRepository instance = new ActivityRepository();

	private ActivityRepository() {
		activityList = new LinkedList<>();
	}

	public static ActivityRepository getInstance() {
		return instance;
	}

	//-- single instance end --

    private List<ActivityInfo> activityList;		// Activity的排列顺序按照先创建先添加的顺序进行添加的

	void notifyActivityCreated(Activity activity){
		if (activity == null) {
			throw new NullPointerException("activity can not be null");
		}
		for (int i = 0; i < activityList.size(); i++) {
			if (activityList.get(i).activity.equals(activity)) {
				return;
			}
		}
		activityList.add(new ActivityInfo(activity));
//		logStack();
	}

	void notifyActivityDestroyed(Activity activity){
		for (int i = activityList.size() - 1; i >= 0; i--) {
			if (activityList.get(i).activity.equals(activity)) {
				activityList.remove(i);
			}
		}
//		logStack();
	}

	void removeTopActivity() {
		if(activityList.size() == 0) {
			return;
		}
		activityList.get(activityList.size() - 1).activity.finish();
	}
	
	void removeActivity(int position) {
        if (position < 0 || position >= activityList.size()) {
            return;
        }
		activityList.get(position).activity.finish();
	}
	
	void removeActivity(int startPosition, int endPosition) {
		for(int i = endPosition; i >= startPosition; i--) {
            if (i < 0 || i >= activityList.size()) {
                continue;
            }
			activityList.get(i).activity.finish();
		}
	}
	
	void removeAllActivity() {
		for(int i = activityList.size() - 1; i >= 0; i--) {
			activityList.get(i).activity.finish();
		}
	}

	ActivityInfo findInfoByActivity(Activity activity) {
		for (ActivityInfo activityInfo : activityList) {
			if (activityInfo.activity != activity) {
				continue;
			}
			return activityInfo;
		}
		return null;
	}

	ActivityInfo getTopActivityInfo(){
		if(activityList.size() == 0) {
			return null;
		}
		return activityList.get(activityList.size() - 1);
	}

	int findActivityPosition(Class<? extends Activity> clazz) {
		for(int i = 0; i < activityList.size(); i++) {
			if(activityList.get(i).activity.getClass() == clazz) {
				return i;
			}
		}
		return -1;
	}

	public int size() {
		return activityList.size();
	}

//	private void logStack() {
//		StringBuffer stringBuffer = new StringBuffer();
//		stringBuffer.append(String.format("Activity数量：%d\n", activityList.size()));
//		for (ActivityInfo activityInfo : activityList) {
//			stringBuffer.append(String.format("Activity名称：%s\n", activityInfo.activity.getClass().toString()));
//			stringBuffer.append(String.format("Activity状态：%s\n", activityInfo.receiveDataState.getStateName()));
//			stringBuffer.append(String.format("Activity摇一摇支持：%s\n", activityInfo.enableShake ? "支持" : "不支持"));
//		}
//		Log.i("ulfy-log", stringBuffer.toString());
//	}
}
	