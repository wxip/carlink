package top.wxip.carlink.client.util;

import java.nio.ByteBuffer;


public class H264Util {
    public static StreamSetting getStreamSetting(byte[] buffer) {
        byte[] sps, pps;

        ByteBuffer spsPpsBuffer = ByteBuffer.wrap(buffer);
        int ppsIndex = 0;
        while (!(spsPpsBuffer.get() == 0x00 && spsPpsBuffer.get() == 0x00 && spsPpsBuffer.get() == 0x00 && spsPpsBuffer.get() == 0x01)) {

        }
        ppsIndex = spsPpsBuffer.position();
        sps = new byte[ppsIndex - 4];
        System.arraycopy(buffer, 0, sps, 0, sps.length);
        ppsIndex -= 4;
        pps = new byte[buffer.length - ppsIndex];
        System.arraycopy(buffer, ppsIndex, pps, 0, pps.length);

        // sps buffer
        ByteBuffer spsBuffer = ByteBuffer.wrap(sps, 0, sps.length);

        // pps buffer
        ByteBuffer ppsBuffer = ByteBuffer.wrap(pps, 0, pps.length);

        StreamSetting streamSettings = new StreamSetting();
        streamSettings.setSps(spsBuffer);
        streamSettings.setPps(ppsBuffer);

        return streamSettings;
    }
}
