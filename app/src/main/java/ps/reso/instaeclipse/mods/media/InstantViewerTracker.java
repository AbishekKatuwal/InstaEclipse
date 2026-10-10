package ps.reso.instaeclipse.mods.media;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import ps.reso.instaeclipse.utils.feature.FeatureFlags;
import ps.reso.instaeclipse.utils.log.ModuleLog;

public class InstantViewerTracker {

    public void install(ClassLoader classLoader) {
        ModuleLog.line("(IE|InstantTracker) Installing viewer tracker...");

        // We already know the camera VM class from InstantUploadHook.
        // Let's hook EVERY method on it and log when they fire.
        final String VM_CLASS = "com.instagram.quicksnap.camera.domain.QuickSnapCameraViewModel";

        try {
            Class<?> vmClass = XposedHelpers.findClass(VM_CLASS, classLoader);
            ModuleLog.line("(IE|InstantTracker) Found class: " + VM_CLASS);

            int count = 0;
            for (Method m : vmClass.getDeclaredMethods()) {
                try {
                    XposedBridge.hookMethod(m, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            if (!FeatureFlags.trackInstantViewers) return;
                            ModuleLog.line("(IE|InstantTracker) 🎯 " + m.getName() 
                                    + " | args=" + (param.args != null ? param.args.length : 0));
                        }
                    });
                    count++;
                } catch (Throwable ignored) {}
            }
            ModuleLog.line("(IE|InstantTracker) ✅ Hooked " + count + " methods");
        } catch (Throwable t) {
            ModuleLog.line("(IE|InstantTracker) ❌ Error: " + t.getMessage());
        }
    }
}
