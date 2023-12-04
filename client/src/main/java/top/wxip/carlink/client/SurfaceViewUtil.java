package top.wxip.carlink.client;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.orhanobut.logger.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

import cn.hutool.core.util.HexUtil;
import top.wxip.carlink.client.util.H264Util;
import top.wxip.carlink.client.util.StreamSetting;
import top.wxip.carlink.common.ControlPacket;
import top.wxip.carlink.common.Port;
import top.wxip.carlink.common.SocketUtil;
import top.wxip.carlink.common.VideoPacket;

public class SurfaceViewUtil {
    public static void initSurfaceView(SurfaceView displayView, Activity activity) {
        final VideoDecoder videoDecoder = new VideoDecoder();

        displayView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                final Surface surface = holder.getSurface();
                // 读取视频流
                new Thread(() -> {
                    Logger.i("displayView width:" + displayView.getWidth() + " height:" + displayView.getHeight());
                    final Socket videoSocket = GlobalItem.getInstance().getVideoSocket();
                    int remoteWidth = 0;
                    int remoteHeight = 0;
                    while (true) {
                        try {
                            byte[] packet = SocketUtil.readPacket(videoSocket.getInputStream());
                            VideoPacket videoPacket = new VideoPacket(packet);
                            switch (videoPacket.getType()) {
                                case INFO:
                                    remoteWidth = videoPacket.getWidth();
                                    remoteHeight = videoPacket.getHeight();
                                    Logger.i("远端视频流 %dx%d", remoteWidth, remoteHeight);

                                    Logger.i("本地显示 " + displayView.getWidth() + " " + displayView.getHeight());

                                    final ViewGroup.LayoutParams lp = displayView.getLayoutParams();
                                    lp.width = displayView.getHeight() * remoteWidth / remoteHeight;
                                    activity.runOnUiThread(() -> {
                                        displayView.setLayoutParams(lp);
                                    });
                                    setControlSocket(displayView, remoteWidth, remoteHeight);

                                    break;
                                case DATA:
                                    int flag = videoPacket.getFlags();
                                    byte[] data = videoPacket.getData();
                                    if ((MediaCodec.BUFFER_FLAG_CODEC_CONFIG & flag) != 0) {
                                        // 提取csd0 csd1
                                        final StreamSetting streamSetting = H264Util.getStreamSetting(data);
                                        Logger.i("calc sps pps => " + HexUtil.encodeHexStr(streamSetting.sps.array()) +
                                                " " + HexUtil.encodeHexStr(streamSetting.pps.array()));
                                        videoDecoder.configure(remoteWidth, remoteHeight, streamSetting.sps, streamSetting.pps, surface);
                                    } else {
                                        videoDecoder.handle(flag, data);
                                    }
                                    break;
                            }
                        } catch (EOFException e) {
                            Logger.e(e, "读取视频流出错 eof");
                        } catch (IOException e) {
                            Logger.e(e, "读取视频流出错");
                            break;
                        }
                    }
                }).start();

                // 读取音频流
                new Thread(() -> {
                    final Socket audioSocket = GlobalItem.getInstance().getAudioSocket();
                    final AudioTrack audio = new AudioTrack.Builder().setAudioAttributes(
                            new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .build()
                    ).setAudioFormat(
                            new AudioFormat.Builder()
                                    .setSampleRate(48000)
                                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                    .build()
                    ).build();
                    audio.play();

                    while (true) {
                        try {
                            byte[] packet = SocketUtil.readPacket(audioSocket.getInputStream());
                            audio.write(packet, 0, packet.length);
                        } catch (EOFException e) {
                            Logger.e(e, "读取音频流出错 eof");
                        } catch (IOException e) {
                            Logger.e(e, "读取音频流出错");
                            break;
                        }
                    }
                }).start();
            }

            @SuppressLint("ClickableViewAccessibility")

            private static void setControlSocket(SurfaceView displayView, int remoteWidth, int remoteHeight) {
                // 传输控制流
                final DatagramSocket controlSocket = GlobalItem.getInstance().getControlSocket();
                AtomicLong lastEventTime = new AtomicLong(System.currentTimeMillis());
                displayView.setOnTouchListener((v, event) -> {
                    long now = System.currentTimeMillis();
//                    if (MotionEvent.ACTION_MOVE == event.getAction() && (now - lastEventTime.get() < 50)) {
//                        return true;
//                    }
                    lastEventTime.set(now);
                    new Thread(() -> {
                        ControlPacket controlPacket = new ControlPacket().setAction(event.getAction())
                                .setX(event.getX() * remoteWidth / displayView.getWidth())
                                .setY(event.getY() * remoteHeight / displayView.getHeight());
                        try {
                            InetAddress remoteAddr = InetAddress.getByName(GlobalItem.getInstance().getRemoteAddr());
//                            Logger.i("发送控制流:" + controlPacket);
                            byte[] packet = controlPacket.toByte();
                            controlSocket.send(new DatagramPacket(packet, packet.length, remoteAddr, Port.CONTROL));
                        } catch (IOException e) {
                            Logger.e(e, "发送控制流出错");
                        }
                    }).start();
                    return true;
                });
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                videoDecoder.destroy();
            }
        });
    }
}
