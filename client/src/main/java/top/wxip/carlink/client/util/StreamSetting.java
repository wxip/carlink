package top.wxip.carlink.client.util;

import java.nio.ByteBuffer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StreamSetting {
    public ByteBuffer pps;
    public ByteBuffer sps;
}