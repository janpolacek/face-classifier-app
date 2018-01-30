package jp.faceclass.nn;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.AsyncTask;

public class Detection {

    private int left = 0;
    private int top = 0;
    private int right = 0;
    private int bottom = 0;

    private int width = 160;
    private int height = 160;


    private byte[] image = null;

    public Detection() {
    }

    public Detection(int x, int y, int r, int b) {
        left = x;
        top = y;
        right = r;
        bottom = b;
    }
    public Detection(int x, int y, int r, int b, byte[] img) {
        this(x, y, r, b);
        setImage(img);
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] img) {
        image = img;

        int [] ret = new int[image.length];
        for(int i=0; i<ret.length; i++){
            ret[i] = (image[i] & 0xff) * 0x00010101;
        }

//        new CheckPhotoTask().execute(ret, null, null);
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


    class CheckPhotoTask extends AsyncTask<int[], Void, Void> {
        @Override
        protected Void doInBackground(int[]... image) {
            Bitmap bmp = Bitmap.createBitmap(image[0], width, height, Bitmap.Config.RGB_565);
            return(null);
        }
    }

}


