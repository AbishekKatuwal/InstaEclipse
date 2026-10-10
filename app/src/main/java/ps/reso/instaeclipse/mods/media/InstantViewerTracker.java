package ps.reso.instaeclipse.mods.media;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import ps.reso.instaeclipse.utils.feature.FeatureFlags;
import ps.reso.instaeclipse.utils.log.ModuleLog;

public class InstantViewerTracker {

    public void install(DexKitBridge bridge, ClassLoader classLoader) {
        ModuleLog.line("(IE|InstantTracker) Installing viewer tracker...");

        try {
            // Search for any method that contains the word "quicksnap"
            List<MethodData> methods = bridge.findMethod(FindMethod.create()
                    .matcher(MethodMatcher.create().usingStrings("quicksnap")));

            if (methods.isEmpty()) {
                ModuleLog.line("(IE|InstantTracker) ❌ No methods found with 'quicksnap'");
                return;
            }

            for (MethodData method : methods) {
                // Only hook void methods with arguments
                if (!String.valueOf(method.getReturnType()).contains("void")) continue;
                if (method.getParamTypes().size() == 0) continue;

                Method reflectMethod = method.getMethodInstance(classLoader);
                
                XposedBridge.hookMethod(reflectMethod, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (!FeatureFlags.trackInstantViewers) return;

                        // THIS is what we want to see in the logs!
                        ModuleLog.line("(IE|InstantTracker) 🎯 CAUGHT: " + 
                                method.getClassName() + "." + method.getName() + 
                                " with " + param.args.length + " args");
                    }
                });
                
                ModuleLog.line("(IE|InstantTracker) Hooked: " + method.getClassName() + "." + method.getName());
            }
        } catch (Throwable t) {
            ModuleLog.line("(IE|InstantTracker) Error: " + t.getMessage());
        }
    }
}
