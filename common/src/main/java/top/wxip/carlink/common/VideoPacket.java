package top.wxip.carlink.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class VideoPacket {
    private VideoPacketType type;
    private int width;
    private int height;
    private int flags;
    private byte[] data;

    public VideoPacket(byte[] src) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(src));
        this.type = VideoPacketType.from(dataInputStream.readInt());
        switch (this.type) {
            case INFO:
                this.width = dataInputStream.readInt();
                this.height = dataInputStream.readInt();
                break;
            case DATA:
                this.flags = dataInputStream.readInt();
                byte[] dataTmp = new byte[dataInputStream.available()];
                dataInputStream.readFully(dataTmp);
                this.data = dataTmp;
                break;
        }
    }

    public byte[] toByte() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024000);
        DataOutputStream dataOutputStream = new DataOutputStream(out);
        dataOutputStream.writeInt(type.getCode());
        switch (type) {
            case INFO:
                dataOutputStream.writeInt(width);
                dataOutputStream.writeInt(height);
                break;
            case DATA:
                dataOutputStream.writeInt(flags);
                dataOutputStream.write(data);
        }
        return out.toByteArray();
    }
}
