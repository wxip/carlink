package top.wxip.carlink.server.wrapper;

import android.os.IInterface;

import lombok.Getter;
import top.wxip.carlink.server.util.Ln;

public final class DisplayManager {
    @Getter
    private static final DisplayManager instance = new DisplayManager();
    private final IInterface manager;

    private DisplayManager() {
        manager = ServiceManager.getInstance().getService("display", "android.hardware.display.IDisplayManager");
    }

    public DisplayInfo getDisplayInfo() {
        try {
            Object displayInfo = manager.getClass().getMethod("getDisplayInfo", int.class).invoke(manager, 0);
            if (null == displayInfo) {
                Ln.e("getDisplayInfo失败", null);
                throw new RuntimeException();
            }
            Class<?> cls = displayInfo.getClass();
            int width = cls.getDeclaredField("logicalWidth").getInt(displayInfo);
            int height = cls.getDeclaredField("logicalHeight").getInt(displayInfo);
            int rotation = cls.getDeclaredField("rotation").getInt(displayInfo);
            return new DisplayInfo(width, height, rotation);
        } catch (Exception e) {
            Ln.e("获取显示数据失败", e);
            throw new RuntimeException(e);
        }
    }
}
