package com.live.media;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.util.Iterator;
import java.util.List;

/**
 * @author roy.xing
 * @date 2019-05-29
 */
public class CameraHelper implements  SurfaceHolder.Callback, Camera.PreviewCallback {

    private String TAG = CameraHelper.class.getSimpleName();
    private Activity activity;
    private int height;
    private int width;
    private int cameraId;
    private Camera camera;
    private byte[] buffer;
    private SurfaceHolder surfaceHolder;
    private Camera.PreviewCallback previewCallback;
    private int mRotation;
    private OnChangedSizeListener mOnChangedSizeListener;
    private byte[] bytes;


    public CameraHelper(Activity activity, int height, int width, int cameraId) {
        this.activity = activity;
        this.height = height;
        this.width = width;
        this.cameraId = cameraId;
    }

    public void switchCamera() {
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }

        stopPreview();
        startPreview();
    }

    private void stopPreview() {
        if (camera != null) {
            //预览数据回调接口
            camera.setPreviewCallback(null);
            //停止预览
            camera.stopPreview();
            //释放摄像头
            camera.release();
            camera = null;
        }
    }

    private void startPreview() {
        try {
            //获得camera对象
            camera = Camera.open(cameraId);
            //配置camera的属性
            Camera.Parameters parameters = camera.getParameters();
            //设置预览数据格式为nv21
            parameters.setPreviewFormat(ImageFormat.NV21);
            //这是摄像头宽、高
            setPreviewSize(parameters);
            setPreviewOrientation(parameters);
            camera.setParameters(parameters);
            buffer = new byte[width * height * 3 / 2];
            bytes = new byte[buffer.length];
            //数据缓存区
            camera.addCallbackBuffer(buffer);
            camera.setPreviewCallbackWithBuffer(this);
            //设置预览画面
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //显示画面会调用编码 横着的
    private void setPreviewOrientation(Camera.Parameters parameters) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        mRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (mRotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                break;
        }
        mOnChangedSizeListener.onChanged(width, height);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private void setPreviewSize(Camera.Parameters parameters) {
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size size = previewSizes.get(0);
        Log.e(TAG, "支持 " + size.width + "x" + size.height);

        int m = Math.abs(size.height * size.width - width * height);
        previewSizes.remove(0);
        Iterator<Camera.Size> iterator = previewSizes.iterator();
        while (iterator.hasNext()) {
            Camera.Size next = iterator.next();
            Log.e(TAG, "支持 " + size.width + "x" + size.height);
            int n = Math.abs(next.height * next.width - width * height);
            if (m > n) {
                m = n;
                size = next;
            }
        }
        width = size.width;
        height = size.height;
        parameters.setPreviewSize(width, height);
        Log.e(TAG, "设置预览分辨率 width:" + size.width + " height:" + size.height);
    }

    public void setPreviewDisplay(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        this.surfaceHolder.addCallback(this);
    }

    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        this.previewCallback = previewCallback;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        switch (mRotation) {
            case Surface.ROTATION_0:
                rotation90(data);
                break;
            case Surface.ROTATION_90:
                // 横屏 左边是头部(home键在右边)
                break;
            case Surface.ROTATION_270:
                // 横屏 头部在右边
                break;
            default:
                break;
        }
        // data数据依然是倒的
        previewCallback.onPreviewFrame(data, camera);
        camera.addCallbackBuffer(buffer);
    }

    private void rotation90(byte[] data) {
        int index = 0;
        int ySize = width * height;
        //u和v
        int uvHeight = height / 2;
        //后置摄像头顺时针旋转90度
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            //将y的数据旋转之后 放入新的byte数组
            for (int i = 0; i < width; i++) {
                for (int j = height - 1; j >= 0; j--) {
                    bytes[index++] = data[width * j + i];
                }
            }

            //每次处理两个数据
            for (int i = 0; i < width; i += 2) {
                for (int j = uvHeight - 1; j >= 0; j--) {
                    // v
                    bytes[index++] = data[ySize + width * j + i];
                    // u
                    bytes[index++] = data[ySize + width * j + i + 1];
                }
            }
        } else {
            //逆时针旋转90度
            for (int i = 0; i < width; i++) {
                int nPos = width - 1;
                for (int j = 0; j < height; j++) {
                    bytes[index++] = data[nPos - i];
                    nPos += width;
                }
            }
            //u v
            for (int i = 0; i < width; i += 2) {
                int nPos = ySize + width - 1;
                for (int j = 0; j < uvHeight; j++) {
                    bytes[index++] = data[nPos - i - 1];
                    bytes[index++] = data[nPos - i];
                    nPos += width;
                }
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //释放摄像头
        stopPreview();
        //开启摄像头
        startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopPreview();
    }


    public void setOnChangedSizeListener(OnChangedSizeListener listener) {
        mOnChangedSizeListener = listener;
    }


    public interface OnChangedSizeListener {
        /**
         * @param w
         * @param h
         */
        void onChanged(int w, int h);
    }
}
