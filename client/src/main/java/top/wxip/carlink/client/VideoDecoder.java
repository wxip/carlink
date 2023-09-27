package top.wxip.carlink.client;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

import cn.hutool.core.thread.ThreadUtil;

public class VideoDecoder {

    private MediaCodec codec;
    private boolean ready;
    private boolean running = true;

    public VideoDecoder() {
        new Thread(() -> {
            while (running) {
                startRender();
            }
        }).start();
    }

    public void configure(int width, int height, ByteBuffer csd0, ByteBuffer csd1, Surface surface) throws IOException {
        if (ready) {
            return;
        }
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 60);
        format.setByteBuffer("csd-0", csd0);
        format.setByteBuffer("csd-1", csd1);
        codec = MediaCodec.createDecoderByType("video/avc");
//        codec = MediaCodec.createByCodecName("OMX.google.h264.decoder");
        codec.configure(format, surface, null, 0);
        codec.start();
        ready = true;
    }

    public void handle(int flag, byte[] data) {
        if (!ready) {
            return;
        }
        int index = codec.dequeueInputBuffer(-1);
        if (index >= 0) {
            ByteBuffer buffer;
            buffer = codec.getInputBuffer(index);
            if (buffer != null) {
                buffer.clear();
                buffer.put(data, 0, data.length);
                codec.queueInputBuffer(index, 0, data.length, 0, flag);
            }
        }
    }

    public void startRender() {
        if (!ready) {
            ThreadUtil.safeSleep(100);
            return;
        }
        final MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int index = codec.dequeueOutputBuffer(info, 0);
        if (index >= 0) {
            codec.releaseOutputBuffer(index, true);
        }
    }

    public void destroy() {
        if (ready) {
            codec.stop();
            codec.release();
            ready = false;
        }
        running = false;
    }
}
