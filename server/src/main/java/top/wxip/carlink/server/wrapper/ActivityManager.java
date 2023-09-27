package top.wxip.carlink.server.wrapper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lombok.Getter;
import top.wxip.carlink.server.FakeContext;
import top.wxip.carlink.server.util.Ln;

@SuppressLint("PrivateApi,DiscouragedPrivateApi")
public class ActivityManager {

    @Getter
    private static final ActivityManager instance = new ActivityManager();

    private final IInterface manager;
    private Method getContentProviderExternalMethod;
    private Method removeContentProviderExternalMethod;
    private Method startActivityAsUserWithFeatureMethod;
    private Method forceStopPackageMethod;

    private ActivityManager() {
        try {
            Class<?> cls = Class.forName("android.app.ActivityManager");
            Method getDefaultMethod = cls.getMethod("getService");
            Ln.i(getDefaultMethod.toGenericString());
            this.manager = (IInterface) getDefaultMethod.invoke(null);
        } catch (Exception e) {
            Ln.e("初始化ActivityManager失败", e);
            throw new RuntimeException(e);
        }
    }

    private Method getRemoveContentProviderExternalMethod() throws NoSuchMethodException {
        if (removeContentProviderExternalMethod == null) {
            removeContentProviderExternalMethod = manager.getClass().getMethod("removeContentProviderExternal", String.class, IBinder.class);
        }
        return removeContentProviderExternalMethod;
    }

    void removeContentProviderExternal(String name, IBinder token) {
        try {
            Method method = getRemoveContentProviderExternalMethod();
            method.invoke(manager, name, token);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            Ln.e("Could not invoke method", e);
        }
    }

    private Method getStartActivityAsUserWithFeatureMethod() throws NoSuchMethodException, ClassNotFoundException {
        if (startActivityAsUserWithFeatureMethod == null) {
            Class<?> iApplicationThreadClass = Class.forName("android.app.IApplicationThread");
            Class<?> profilerInfo = Class.forName("android.app.ProfilerInfo");
            startActivityAsUserWithFeatureMethod = manager.getClass()
                    .getMethod("startActivityAsUserWithFeature", iApplicationThreadClass, String.class, String.class, Intent.class, String.class,
                            IBinder.class, String.class, int.class, int.class, profilerInfo, Bundle.class, int.class);
        }
        return startActivityAsUserWithFeatureMethod;
    }

    @SuppressWarnings("ConstantConditions")
    public int startActivityAsUserWithFeature(Intent intent) {
        try {
            Method method = getStartActivityAsUserWithFeatureMethod();
            return (int) method.invoke(
                    /* this */ manager,
                    /* caller */ null,
                    /* callingPackage */ FakeContext.PACKAGE_NAME,
                    /* callingFeatureId */ null,
                    /* intent */ intent,
                    /* resolvedType */ null,
                    /* resultTo */ null,
                    /* resultWho */ null,
                    /* requestCode */ 0,
                    /* startFlags */ 0,
                    /* profilerInfo */ null,
                    /* bOptions */ null,
                    /* userId */ /* UserHandle.USER_CURRENT */ -2);
        } catch (Throwable e) {
            Ln.e("Could not invoke method", e);
            return 0;
        }
    }

    private Method getForceStopPackageMethod() throws NoSuchMethodException {
        if (forceStopPackageMethod == null) {
            forceStopPackageMethod = manager.getClass().getMethod("forceStopPackage", String.class, int.class);
        }
        return forceStopPackageMethod;
    }

    public void forceStopPackage(String packageName) {
        try {
            Method method = getForceStopPackageMethod();
            method.invoke(manager, packageName, /* userId */ /* UserHandle.USER_CURRENT */ -2);
        } catch (Throwable e) {
            Ln.e("Could not invoke method", e);
        }
    }
}
