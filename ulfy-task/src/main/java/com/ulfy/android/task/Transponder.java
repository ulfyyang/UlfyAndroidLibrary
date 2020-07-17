package com.ulfy.android.task;

/**
 * 应答器的基类
 */
public abstract class Transponder {

    final void onTranspondMessage(Message message) {
        switch (message.getType()) {
            case Message.TYPE_NO_NET_CONNECTION:
                onNoNetConnection(message.getData());
                break;
            case Message.TYPE_NET_ERROR:
                onNetError(message.getData());
                break;
            case Message.TYPE_START:
                onStart(message.getData());
                break;
            case Message.TYPE_SUCCESS:
                onSuccess(message.getData());
                break;
            case Message.TYPE_FAIL:
                onFail(message.getData());
                break;
            case Message.TYPE_UPDATE:
                onUpdate(message.getData());
                break;
            case Message.TYPE_FINISH:
                onFinish(message.getData());
                break;
        }
    }

    protected void onNoNetConnection(Object data) { }
    protected void onNetError(Object data) { }
    protected void onStart(Object data) { }
    protected void onSuccess(Object data) { }
    protected void onFail(Object data) { }
    protected void onUpdate(Object data) { }
    protected void onFinish(Object data) { }
}
