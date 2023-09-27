package top.wxip.carlink.client;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.LogcatLogStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.tananaev.adblib.AdbConnection;
import com.tananaev.adblib.AdbCrypto;
import com.tananaev.adblib.AdbStream;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import top.wxip.carlink.common.Action;
import top.wxip.carlink.common.Port;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLogger();
        setContentView(R.layout.activity_main);

        final Button btnConnectAdb = findViewById(R.id.btn_connect_adb);
        final EditText etRemoteAddr = findViewById(R.id.et_remote_addr);

        btnConnectAdb.setOnClickListener(v -> startADB(etRemoteAddr.getText().toString()));

    }

    /**
     * 初始化日志框架
     */
    private void initLogger() {
        Logger.addLogAdapter(new AndroidLogAdapter(PrettyFormatStrategy.newBuilder()
                .showThreadInfo(true)
                .methodCount(2)
                .methodOffset(5)
                .logStrategy(new LogcatLogStrategy())
                .tag("top.wxip.carlink.client")
                .build()));
    }

    private void startADB(String remoteAddr) {
        try {
            String privKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCeWZiWzcSTgbadF1b6aDueG/Z9uth/dsn6wEGSIkGiJHXam1ubhBWzX2kUqARLbNdmmZqlwWuH9vUqzFkYbpnuqvUCoy9Z5FiluCFm/QgC2/jeTEt7sgqwYDPEfINdXcO0B2DS2dM37IXmqW4H5He6HwE8OBGOBfgUObgFyQN2aKvHJvYT+wIfhpjtpvpBcB+o8wtKok44hd6WZXbqru6606hLLTB895H/Im/aYY1GQSMqwMvj108x4eZz6kepB7nuaNRE4GpquyW2PWYjLIF/Kgx1p1qJOGLWUxVaudPLteahx7g9RG8D2oyYtDhrJ7wPMhIaB9s/k6j6e8GQg8VlAgMBAAECggEAAcvJBpza3UQcCs1tonV1h6Y64NOHtRpnJr2bgMl8A8IfcWqlsAPgLc1Skctou7pGTyxFgW/KL8abTUWiv7uGv3BCSyoIACBY6Q2c7grHVnuhklPnNJtLsmhvmIuY/ywbFiWnRtJK/61ib2qojCpejpvfawwabDP2dl7ujrzoRHrdi73rQEqrMJmZm83qLX5ETiUgKQ+e+I2TjdI93E20X8H/52zcAFwEvRjFALsnuFAu3HUjOYggBRn++i0po+i8lFZHqX+pk2jPGZfwMdJk3o6oQB9Wpm+I1iFZFKKWEdlKFe1KYyiM7L8zesrIgWV0Awzt5ibcA9yIJEUZ0gwHwQKBgQDNuOYbypyG9jmWl28OChOrIRlGE7w7754GKVPyvfVTGbamKZQi2rvq++uDs+GA2RVsu/nkdQuOyjVSxsuLWedmrcOtf1j8QoQN+5dV/saywCTTQTD6WXAXNzV8qB5ykwcW+zd+3sqpY3Hp1YcvqEh32wOyfOk5xN53Hi7tRAPNSQKBgQDFDNSZso1EiiXn6KKw9GU/z65Ffpur0ynurLys2uir4uD4dOh2I7fx76N8vQ+e3Mm6zdSPRFCc2rJKJWFhtWV+vFTLAmAHY1GM6gwTqzKZYbzaHPSeBp79NwuAIv6NYjsd+7qXWVTHPwQ5YUb9rcN1n4mnyzymgwO3NaT2kpQDPQKBgQC+X1eZ6kI1Zo/eOp7LiCmxWDzSK5sqf9BldUM5Q+5NC2OJmrp6Ep6JkrzcMM8CdGEuMTnL30Blz9vOkSZ0+yp3S1/kIw/OySL46ZSfQNjRO5wYXqCbW76tzSpTBCTA9CxAaRNu9W7nl54nvLCQOpNtTqC6QHt3OSaBZs4YRLOb+QKBgFzRHFuBZVWBlTVVuTyNAhw6oSYO6xjWkIviRcVOIAbHCZ0+xDjRvlVxwZqCG5eC/GrHhXYugaJAyXZvgR1bKoG2CGLPrZgNSl6L6EBjhaQGC8TZmsfM6prvkLU8xpamTJ5k8pFT/MEVh9HXZm8bqFQBX//vlZQBZoCn/ho/FQUpAoGAXVqsyZA9Dfhl/aDtYdYjz3hg8vZTFPJaXRT7KyhA6bVUPj3jmqoY09d+sLqs/KV/XPUKf/7/RO8umvlqk32qGeXFE7Pn8N3+Kp2XGp1p0rIJVfMv/FO3oIGnv5qYwN68PqRHpEjx73bIlhmmcQoX6ISLjubcESvLIkLQyIfkWlw=";
            String pubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnlmYls3Ek4G2nRdW+mg7nhv2fbrYf3bJ+sBBkiJBoiR12ptbm4QVs19pFKgES2zXZpmapcFrh/b1KsxZGG6Z7qr1AqMvWeRYpbghZv0IAtv43kxLe7IKsGAzxHyDXV3DtAdg0tnTN+yF5qluB+R3uh8BPDgRjgX4FDm4BckDdmirxyb2E/sCH4aY7ab6QXAfqPMLSqJOOIXelmV26q7uutOoSy0wfPeR/yJv2mGNRkEjKsDL49dPMeHmc+pHqQe57mjUROBqarsltj1mIyyBfyoMdadaiThi1lMVWrnTy7Xmoce4PURvA9qMmLQ4aye8DzISGgfbP5Oo+nvBkIPFZQIDAQAB";
            FileUtil.writeBytes(Base64.decode(privKey), getFileStreamPath("priv.key"));
            FileUtil.writeBytes(Base64.decode(pubKey), getFileStreamPath("pub.key"));
            AdbCrypto adbCrypto = AdbCrypto.loadAdbKeyPair(Base64::encode, getFileStreamPath("priv.key"), getFileStreamPath("pub.key"));

            ThreadUtil.execute(() -> {
                Socket socket = null;
                AdbConnection adbConnection = null;
                AdbStream adbStream = null;
                Socket videoSocket = null;
                DatagramSocket controlSocket = null;
                Socket audioSocket = null;
                try {
                    try {
                        socket = new Socket(remoteAddr, 5555);
                    } catch (Exception e) {
                        error(e, "adb socket连接失败");
                        return;
                    }
                    info("adb socket连接成功");
                    boolean adbConnectSuccess;
                    try {
                        adbConnection = AdbConnection.create(socket, adbCrypto);
                        adbConnectSuccess = adbConnection.connect(10, TimeUnit.SECONDS, false);
                    } catch (Exception e) {
                        error(e, "adb连接失败");
                        return;
                    }
                    if (!adbConnectSuccess) {
                        error(null, "adb连接超时");
                        return;
                    }
                    info("adb连接成功");
                    // 启动server
                    try {
                        adbStream = adbConnection.open("shell:");
                        adbStream.write("CLASSPATH=/data/local/tmp/carlink-server.jar app_process / top.wxip.carlink.server.Application\n");
                        while (!adbStream.isClosed()) {
                            byte[] read = adbStream.read();
                            final String message = new String(read);
                            Logger.i(message);
                            if (message.endsWith(":/ $ ")) {
                                adbStream.close();
                            }
                            if (message.contains(Action.ESTABLISH_VIDEO)) {
                                // 建立视频连接
                                ThreadUtil.safeSleep(100);
                                try {
                                    videoSocket = new Socket(remoteAddr, Port.VIDEO);
                                } catch (IOException e) {
                                    error(e, "建立视频连接失败,adb退出");
                                    adbStream.close();
                                }
                                info("视频连接建立成功");
                            }
                            if (message.contains(Action.ESTABLISH_CONTROL)) {
                                // 建立控制连接
                                ThreadUtil.safeSleep(100);
                                try {
                                    controlSocket = new DatagramSocket();
                                } catch (IOException e) {
                                    error(e, "建立控制连接失败,adb退出");
                                    adbStream.close();
                                }
                                info("控制连接建立成功");
                            }
                            if (message.contains(Action.ESTABLISH_AUDIO)) {
                                // 建立控制连接
                                ThreadUtil.safeSleep(100);
                                try {
                                    audioSocket = new Socket(remoteAddr, Port.AUDIO);
                                } catch (IOException e) {
                                    error(e, "建立音频连接失败,adb退出");
                                    adbStream.close();
                                }
                                info("音频连接建立成功");
                            }
                            if (message.contains(Action.ESTABLISH_READY)) {
                                GlobalItem.getInstance().setRemoteAddr(remoteAddr);
                                GlobalItem.getInstance().setVideoSocket(videoSocket);
                                GlobalItem.getInstance().setControlSocket(controlSocket);
                                GlobalItem.getInstance().setAudioSocket(audioSocket);
                                // 连接已建立,跳转展示页面
                                Intent intent = new Intent(MainActivity.this, ViewActivity.class);
                                startActivity(intent);
                            }
                        }
                    } catch (IOException | InterruptedException e) {
                        error(e, "启动adb stream失败");
                    }
                } finally {
                    try {
                        if (null != adbStream) {
                            adbStream.close();
                        }
                        if (null != adbConnection) {
                            adbConnection.close();
                        }
                        if (null != socket) {
                            socket.close();
                        }
                        if (null != videoSocket) {
                            videoSocket.close();
                        }
                        if (null != controlSocket) {
                            controlSocket.close();
                        }
                        if (null != audioSocket) {
                            audioSocket.close();
                        }
                    } catch (Exception e) {
                        Logger.e(e, "关闭adb失败");
                    }
                }
                info("adb退出");
            });
        } catch (Exception e) {
            error(e, "adb key初始化失败");
        }
    }

    private void info(String msg) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show());
        Logger.i(msg);
    }

    private void error(Throwable e, String msg) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show());
        Logger.e(e, msg);
    }

}