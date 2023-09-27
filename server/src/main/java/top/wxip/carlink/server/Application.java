package top.wxip.carlink.server;

import android.os.IBinder;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

import cn.hutool.core.thread.ThreadUtil;
import top.wxip.carlink.common.Action;
import top.wxip.carlink.common.ControlPacket;
import top.wxip.carlink.common.Port;
import top.wxip.carlink.common.Video;
import top.wxip.carlink.server.util.Ln;
import top.wxip.carlink.server.wrapper.DisplayInfo;
import top.wxip.carlink.server.wrapper.DisplayManager;
import top.wxip.carlink.server.wrapper.InputManager;
import top.wxip.carlink.server.wrapper.SurfaceControl;

public class Application {
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> Ln.e("系统异常 " + thread, e));

        // 获取显示数据
        final DisplayInfo displayInfo = DisplayManager.getInstance().getDisplayInfo();
        Ln.i("设备信息:" + displayInfo);

        // 建立视频通道
        AtomicReference<Socket> videoSocketRaw = new AtomicReference<>();
        try (ServerSocket serverSocket = new ServerSocket(Port.VIDEO)) {
            Ln.action(Action.ESTABLISH_VIDEO);
            videoSocketRaw.set(serverSocket.accept());
        } catch (IOException e) {
            Ln.e("建立视频通道失败,server退出", e);
            System.exit(0);
        }
        Ln.i("建立视频通道成功");

        // 建立控制通道
        AtomicReference<DatagramSocket> controlSocketRaw = new AtomicReference<>();
        try {
            DatagramSocket controlSocket = new DatagramSocket(Port.CONTROL);
            controlSocketRaw.set(controlSocket);
            Ln.action(Action.ESTABLISH_CONTROL);
        } catch (IOException e) {
            Ln.e("建立控制通道失败,server退出", e);
            System.exit(0);
        }
        Ln.i("建立控制通道成功");

        // 建立音频通道
        AtomicReference<Socket> audioSocketRaw = new AtomicReference<>();
        try (ServerSocket serverSocket = new ServerSocket(Port.AUDIO)) {
            Ln.action(Action.ESTABLISH_AUDIO);
            audioSocketRaw.set(serverSocket.accept());
        } catch (IOException e) {
            Ln.e("建立音频通道失败,server退出", e);
            System.exit(0);
        }
        Ln.i("建立音频通道成功");

        Ln.action(Action.ESTABLISH_READY);

        // 启动视频推流
        new Thread(() -> {
            final Socket videoSocket = videoSocketRaw.get();
            VideoEncoder videoEncoder = new VideoEncoder();
            try {
                videoEncoder.stream(displayInfo, videoSocket.getOutputStream());
            } catch (IOException e) {
                Ln.e("视频推流失败,server退出", e);
                System.exit(0);
            }
        }).start();

        // 设置硬件黑屏
        final SurfaceControl surfaceControl = SurfaceControl.getInstance();
        long[] physicalDisplayIds = surfaceControl.getPhysicalDisplayIds();
        for (long physicalDisplayId : physicalDisplayIds) {
            Ln.i("需要黑屏的ID：" + physicalDisplayId);
        }
        new Thread(() -> {
            while (true) {
                // 硬件黑屏
                for (long physicalDisplayId : physicalDisplayIds) {
                    IBinder binder = surfaceControl.getPhysicalDisplayToken(physicalDisplayId);
                    surfaceControl.setDisplayPowerMode(binder, 0);
                }
                ThreadUtil.safeSleep(200);
            }
        }).start();

        // 接受控制通道的数据
        new Thread(() -> {
            final DatagramSocket controlSocket = controlSocketRaw.get();
            long lastTouchDown = SystemClock.uptimeMillis();
            while (true) {
                try {
                    byte[] packet = new byte[1024];
                    final DatagramPacket datagramPacket = new DatagramPacket(packet, packet.length);
                    controlSocket.receive(datagramPacket);
                    final ControlPacket controlPacket = new ControlPacket(packet);

//                    Ln.i("接受控制流:" + controlPacket);

                    long now = SystemClock.uptimeMillis();

                    int action = controlPacket.getAction();
                    float x = controlPacket.getX() * displayInfo.getWidth() / Video.width;
                    float y = controlPacket.getY() * displayInfo.getHeight() / Video.height;

                    MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
                    coords.x = x;
                    coords.y = y;
                    MotionEvent.PointerProperties props = new MotionEvent.PointerProperties();
                    props.id = 0;
                    props.toolType = MotionEvent.TOOL_TYPE_FINGER;
                    coords.orientation = 0;
                    coords.pressure = 1;
                    coords.size = 1;

                    if (0 == action) {
                        lastTouchDown = now;
                    }

                    MotionEvent event = MotionEvent.obtain(lastTouchDown, now, action, 1,
                            new MotionEvent.PointerProperties[]{props},
                            new MotionEvent.PointerCoords[]{coords}, 0, action, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
                    InputManager inputManager = InputManager.getInstance();
                    inputManager.injectInputEvent(event, 0);
                } catch (IOException e) {
                    Ln.e("接受控制流失败,server退出", e);
                    System.exit(0);
                }
            }
        }).start();

        // 发送音频
        try {
            final Socket audioSocket = audioSocketRaw.get();
            AudioCapture audioCapture = new AudioCapture(AudioSource.OUTPUT);
            final AudioRawRecorder audioRawRecorder = new AudioRawRecorder(audioCapture, audioSocket.getOutputStream());
            audioRawRecorder.start();
        } catch (Exception e) {
            Ln.e("发送音频流失败,server退出", e);
            System.exit(0);
        }
    }
}
