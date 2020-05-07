package com.ulfy.android.image;

/**
 * 位图处理链，可以将多个位图处理接单链接起来。位图会在联调中按顺序依次处理
 */
public final class BitmapProcessChain {
    private BitmapProcessNode firstProcessNode;
    private BitmapProcessNode currentProcessNode;

    public BitmapProcessChain connect(BitmapProcessNode bitmapProcessNode) {
        if (bitmapProcessNode == null) {
            return this;
        }

        if (firstProcessNode == null) {
            firstProcessNode = bitmapProcessNode;
            currentProcessNode = bitmapProcessNode;
        } else {
            currentProcessNode = currentProcessNode.connect(bitmapProcessNode);
        }

        return this;
    }

    public BitmapProcessNode build() {
        return firstProcessNode;
    }
}
