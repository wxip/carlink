package top.wxip.carlink.server.wrapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public final class DisplayInfo {
    private final int width;
    private final int height;
    private final int rotation;
}

