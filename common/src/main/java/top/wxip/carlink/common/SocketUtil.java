package top.wxip.carlink.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SocketUtil {
    public static void sendPacket(OutputStream outputStream, byte[] data) throws IOException {
        final DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeInt(data.length);
        outputStream.write(data);
    }

    public static byte[] readPacket(InputStream inputStream) throws IOException {
        final DataInputStream dataInputStream = new DataInputStream(inputStream);
        int length = dataInputStream.readInt();
        byte[] buf = new byte[length];
        dataInputStream.readFully(buf);
        return buf;
    }
}
