package top.wxip.carlink.server;

import android.media.MediaCodec;
import android.os.Build;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import top.wxip.carlink.common.SocketUtil;
import top.wxip.carlink.server.util.Ln;


public final class AudioRawRecorder {

    private final AudioCapture capture;
    private final OutputStream streamer;

    private Thread thread;

    private static final int READ_MS = 5; // milliseconds
    private static final int READ_SIZE = AudioCapture.millisToBytes(READ_MS);

    public AudioRawRecorder(AudioCapture capture, OutputStream streamer) {
        this.capture = capture;
        this.streamer = streamer;
    }

    private void record() throws IOException, AudioCaptureForegroundException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Ln.e("Audio disabled: it is not supported before Android 11");
//            streamer.writeDisableStream(false);
            return;
        }

        final ByteBuffer buffer = ByteBuffer.allocateDirect(READ_SIZE);
        final MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        try {
            capture.start();

//            streamer.writeAudioHeader();
            while (!Thread.currentThread().isInterrupted()) {
                buffer.position(0);
                int r = capture.read(buffer, READ_SIZE, bufferInfo);
                if (r < 0) {
                    throw new IOException("Could not read audio: " + r);
                }
                buffer.limit(r);

                byte[] b = new byte[buffer.remaining()];
                buffer.get(b);
                SocketUtil.sendPacket(streamer,b);
            }
        } catch (Throwable e) {
            // Notify the client that the audio could not be captured
//            streamer.writeDisableStream(false);
            throw e;
        } finally {
            capture.stop();
        }
    }

    public void start() throws IOException, AudioCaptureForegroundException {
        record();
//        thread = new Thread(() -> {
//            boolean fatalError = false;
//            try {
//
//            } catch (AudioCaptureForegroundException e) {
//                // Do not print stack trace, a user-friendly error-message has already been logged
//            } catch (IOException e) {
//                Ln.e("Audio recording error", e);
//                fatalError = true;
//            } finally {
//                Ln.d("Audio recorder stopped");
//            }
//        }, "audio-raw");
//        thread.start();
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    public void join() throws InterruptedException {
        if (thread != null) {
            thread.join();
        }
    }
}
