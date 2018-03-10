package classification;

import android.graphics.Bitmap;
import android.graphics.RectF;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class Detection {

    private int left = 0;
    private int top = 0;
    private int right = 0;
    private int bottom = 0;

    private Mat mat;

    public Detection() {
        mat = new Mat();
    }

    public Detection(int x, int y, int r, int b) {
        this();
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

    public Mat getMat() {
        return mat;
    }

    public void setMat(Mat mat) {
        this.mat = mat;
    }

    public long getNativeObjAddr(){
        return mat.getNativeObjAddr();
    }

    public void viewBitmap(){
        Bitmap bmp;
        bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(mat, bmp);
    }
}


