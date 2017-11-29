package jp.faceclass.detection;

import android.graphics.RectF;

public class Detection {

    private int left;
    private int top;
    private int right;
    private int bottom;

    public Detection() {
    }

    public Detection(int x, int y, int r, int b) {
        left = x;
        top = y;
        right = r;
        bottom = b;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public RectF getRectangle() {
        return new RectF(left, top, right, bottom);
    }
}
