package com.live.media;

import android.app.Activity;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.live.LivePusher;

/**
 * @author roy.xing
 * @date 2019-05-30
 */
public class VideoChannel implements Camera.PreviewCallback, CameraHelper.OnChangedSizeListener {

    private CameraHelper cameraHelper;
    private int bitrate;
    private int pfs;
    private boolean isLiving;

    public VideoChannel(LivePusher livePusher, Activity activity, int width, int height, int bitrate, int fps, int cameraId) {
        this.bitrate = bitrate;
        this.pfs = fps;
        cameraHelper = new CameraHelper(activity, height, width, cameraId);
        cameraHelper.setPreviewCallback(this);
        cameraHelper.setOnChangedSizeListener(this);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
    }

    @Override
    public void onChanged(int w, int h) {

    }

    public void switchCamera() {
        cameraHelper.switchCamera();
    }

    public void setPreviewDisplay(SurfaceHolder surfaceHolder) {
        cameraHelper.setPreviewDisplay(surfaceHolder);
    }
}
