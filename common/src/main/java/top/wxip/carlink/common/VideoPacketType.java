package top.wxip.carlink.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VideoPacketType {
    UNKNOWN(0), INFO(1), DATA(2);
    private final int code;

    public static VideoPacketType from(int code) {
        for (VideoPacketType value : VideoPacketType.values()) {
            if (code == value.code) {
                return value;
            }
        }
        return UNKNOWN;
    }
}
