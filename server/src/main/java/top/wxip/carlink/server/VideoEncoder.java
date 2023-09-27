package top.wxip.carlink.server;

import android.graphics.Rect;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.IBinder;
import android.view.Surface;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import top.wxip.carlink.common.SocketUtil;
import top.wxip.carlink.common.VideoPacket;
import top.wxip.carlink.common.VideoPacketType;
import top.wxip.carlink.server.util.Ln;
import top.wxip.carlink.server.wrapper.DisplayInfo;
import top.wxip.carlink.server.wrapper.SurfaceControl;

public class VideoEncoder {
    public void stream(DisplayInfo displayInfo, OutputStream videoStream) throws IOException {

        final int width = displayInfo.getWidth();
        final int height = displayInfo.getHeight();

        final Rect rect = new Rect(0, 0, width, height);

        final MediaFormat format = new MediaFormat();
        format.setInteger(MediaFormat.KEY_WIDTH, width);
        format.setInteger(MediaFormat.KEY_HEIGHT, height);
        format.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_VIDEO_AVC);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 12 * 1000 * 1024);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 60);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);
        format.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 100_000);

        MediaCodec codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        Surface surface = codec.createInputSurface();

        SurfaceControl surfaceControl = SurfaceControl.getInstance();
        IBinder display = surfaceControl.createDisplay("carlink", false);
        surfaceControl.openTransaction();
        try {
            surfaceControl.setDisplaySurface(display, surface);
            surfaceControl.setDisplayProjection(display, 0, rect, rect);
            surfaceControl.setDisplayLayerStack(display, 0);
        } finally {
            surfaceControl.closeTransaction();
        }

        codec.start();

        // 发送video宽高给client
        SocketUtil.sendPacket(videoStream, new VideoPacket().setType(VideoPacketType.INFO).setWidth(width).setHeight(height).toByte());

        try {
            encode(codec, videoStream);
        } finally {
            codec.stop();
            surfaceControl.destroyDisplay(display);
            codec.release();
            surface.release();
        }
    }

    private void encode(MediaCodec codec, OutputStream outputStream) throws IOException {
        Ln.i("视频编码开始");
        boolean eof = false;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (!eof) {
            int outputBufferId = codec.dequeueOutputBuffer(bufferInfo, -1);
            eof = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
            try {
                if (outputBufferId >= 0) {
                    ByteBuffer outputBuffer;
                    outputBuffer = codec.getOutputBuffer(outputBufferId);
                    if (bufferInfo.size > 0 && outputBuffer != null) {
                        outputBuffer.position(bufferInfo.offset);
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                        byte[] b = new byte[outputBuffer.remaining()];
                        outputBuffer.get(b);
                        SocketUtil.sendPacket(outputStream, new VideoPacket()
                                .setType(VideoPacketType.DATA)
                                .setFlags(bufferInfo.flags)
                                .setData(b)
                                .toByte());
                    }
                }
            } finally {
                if (outputBufferId >= 0) {
                    codec.releaseOutputBuffer(outputBufferId, false);
                }
            }
        }
        Ln.i("视频编码结束");
    }
}
