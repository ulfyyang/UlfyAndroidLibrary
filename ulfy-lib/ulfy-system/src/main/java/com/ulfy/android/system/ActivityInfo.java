package com.ulfy.android.system;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.ulfy.android.bus.BusUtils;
import com.ulfy.android.system.event.OnDeviceShakedEvent;

import static android.content.Context.SENSOR_SERVICE;

class ActivityInfo {
    Activity activity;                                                      // 存储对应的Activity
    ReceiveDataState receiveDataState = new ReceiveDataState();             // 存储当前Activity接收数据的内部状态
    boolean enableShake;                                                    // 是否开启摇一摇功能，该变量用于在Activity开始和停止时注册和恢复使用
    private SensorEventListener sensorEventListener = new SensorEventListenerImpl();    // 摇一摇使用的回调实现

    /**
     * 构造方法
     */
    ActivityInfo(Activity activity) {
        this.activity = activity;
    }

    /**
     * 向系统注册摇一摇回调
     */
    ActivityInfo registerShake() {
        SensorManager sensorManager = ((SensorManager) activity.getSystemService(SENSOR_SERVICE));
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometerSensor != null) {
            sensorManager.unregisterListener(sensorEventListener);
            sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
        }
        return this;
    }

    /**
     * 向系统取消摇一摇回调
     */
    ActivityInfo unRegisterShake() {
        SensorManager sensorManager = ((SensorManager) activity.getSystemService(SENSOR_SERVICE));
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometerSensor != null) {
            sensorManager.unregisterListener(sensorEventListener, accelerometerSensor);
        }
        return this;
    }

    /**
     * 自定义摇一摇触发，当满足条件时会发布在当前上下文发布一个摇一摇事件
     */
    private class SensorEventListenerImpl implements SensorEventListener {
        private long lastTime;
        public void onSensorChanged(SensorEvent event) {
            // 记录当前的时刻和加速器对应三维上的加速变化
            long currentTime = System.currentTimeMillis();
            float x = event.values[0], y = event.values[1], z = event.values[2];
            // 不同的传感器有不同的速率，很难达到一个统一的用户体验，因此这里采用一个相对较小的值已确保能够触发
            int needSpeed = 25, needTime = 1000;
            // 一秒内重复操作无效且速度要达到要求后触发摇一摇，触发之后充值上次记录时间
            if ((currentTime - lastTime > needTime) && (Math.abs(x) > needSpeed || Math.abs(y) > needSpeed || Math.abs(z) > needSpeed)) {
                AppUtils.viber();
                BusUtils.post(activity, new OnDeviceShakedEvent());
                lastTime = currentTime;
            }
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    }

}
