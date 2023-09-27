package top.wxip.carlink.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@ToString
public class ControlPacket {
    private int action;
    private float x;
    private float y;

    public ControlPacket(byte[] src) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(src));
        this.action = dataInputStream.readInt();
        this.x = dataInputStream.readFloat();
        this.y = dataInputStream.readFloat();
    }

    public byte[] toByte() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(out);
        dataOutputStream.writeInt(action);
        dataOutputStream.writeFloat(x);
        dataOutputStream.writeFloat(y);
        return out.toByteArray();
    }
}
