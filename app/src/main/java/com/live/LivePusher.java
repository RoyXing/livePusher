package com.live;

import android.app.Activity;
import android.view.SurfaceHolder;

import com.live.media.VideoChannel;

/**
 * @author roy.xing
 * @date 2019-05-29
 */
public class LivePusher {

    static {
        System.loadLibrary("pusher");
    }

    private VideoChannel videoChannel;

    public LivePusher(Activity activity, int width, int height, int bitrate, int fps, int cameraId) {
        videoChannel = new VideoChannel(this, activity, width, height, bitrate, fps, cameraId);
    }

    public void setPreviewDisplay(SurfaceHolder surfaceHolder) {
        videoChannel.setPreviewDisplay(surfaceHolder);
    }
    public void switchCamera() {
        videoChannel.switchCamera();
    }
}
