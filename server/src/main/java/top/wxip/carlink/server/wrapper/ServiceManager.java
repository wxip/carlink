package top.wxip.carlink.server.wrapper;

import android.annotation.SuppressLint;
import android.os.IBinder;
import android.os.IInterface;

import java.lang.reflect.Method;

import lombok.Getter;
import top.wxip.carlink.server.util.Ln;

public final class ServiceManager {
    private final Method getServiceMethod;

    @Getter
    private static final ServiceManager instance = new ServiceManager();

    @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
    private ServiceManager() {
        try {
            getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
        } catch (Exception e) {
            Ln.e("初始化getService失败", e);
            throw new RuntimeException(e);
        }
    }

    public IInterface getService(String service, String type) {
        try {
            IBinder binder = (IBinder) getServiceMethod.invoke(null, service);
            Method asInterfaceMethod = Class.forName(type + "$Stub").getMethod("asInterface", IBinder.class);
            return (IInterface) asInterfaceMethod.invoke(null, binder);
        } catch (Exception e) {
            Ln.e("getService失败 " + service + " " + type, e);
            throw new RuntimeException(e);
        }
    }
}
