package top.wxip.carlink.server.wrapper;

import android.graphics.Rect;
import android.os.IBinder;
import android.view.Surface;

import lombok.Getter;
import top.wxip.carlink.server.util.Ln;

public final class SurfaceControl {

    @Getter
    private static final SurfaceControl instance = new SurfaceControl();
    private final Class<?> clazz;

    private SurfaceControl() {
        try {
            clazz = Class.forName("android.view.SurfaceControl");
        } catch (ClassNotFoundException e) {
            Ln.e("找不到SurfaceControl", e);
            throw new RuntimeException(e);
        }
    }

    public void openTransaction() {
        try {
            clazz.getMethod("openTransaction").invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void closeTransaction() {
        try {
            clazz.getMethod("closeTransaction").invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setDisplayProjection(IBinder displayToken, int orientation, Rect layerStackRect, Rect displayRect) {
        try {
            clazz.getMethod("setDisplayProjection", IBinder.class, int.class, Rect.class, Rect.class)
                    .invoke(null, displayToken, orientation, layerStackRect, displayRect);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setDisplayLayerStack(IBinder displayToken, int layerStack) {
        try {
            clazz.getMethod("setDisplayLayerStack", IBinder.class, int.class).invoke(null, displayToken, layerStack);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setDisplaySurface(IBinder displayToken, Surface surface) {
        try {
            clazz.getMethod("setDisplaySurface", IBinder.class, Surface.class).invoke(null, displayToken, surface);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public IBinder createDisplay(String name, boolean secure) {
        try {
            return (IBinder) clazz.getMethod("createDisplay", String.class, boolean.class).invoke(null, name, secure);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void destroyDisplay(IBinder displayToken) {
        try {
            clazz.getMethod("destroyDisplay", IBinder.class).invoke(null, displayToken);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
