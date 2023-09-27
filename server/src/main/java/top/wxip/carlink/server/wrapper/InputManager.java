package top.wxip.carlink.server.wrapper;

import android.os.IInterface;
import android.view.InputEvent;

import lombok.Getter;
import top.wxip.carlink.server.util.Ln;

public final class InputManager {
    @Getter
    private static final InputManager instance = new InputManager();
    private final IInterface manager;

    private InputManager() {
        manager = ServiceManager.getInstance().getService("input", "android.hardware.input.IInputManager");
    }

    public boolean injectInputEvent(InputEvent inputEvent, int mode) {
        try {
            return (boolean) manager.getClass().getMethod("injectInputEvent", InputEvent.class, int.class).invoke(manager, inputEvent, mode);
        } catch (Exception e) {
            Ln.e("输入动作失败", e);
            throw new RuntimeException(e);
        }
    }
}
