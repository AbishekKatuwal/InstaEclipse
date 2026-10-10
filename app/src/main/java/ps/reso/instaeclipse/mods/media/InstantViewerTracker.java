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

    public void install(ClassLoader classLoader) {
        ModuleLog.line("(IE|InstantTracker) Installing viewer tracker...");

        try {
            DexKitBridge bridge = DexKitBridge.create(classLoader);

            // Search for methods containing "seen_by" (the IG string for viewer lists)
            List<MethodData> methods = bridge.findMethod(FindMethod.create()
                    .matcher(MethodMatcher.create().usingStrings("seen_by")));

            if (methods.isEmpty()) {
                ModuleLog.line("(IE|InstantTracker) ❌ No methods found with 'seen_by'");
                return;
            }

            for (MethodData method : methods) {
                // Hook everything to see what's happening
                Method reflectMethod = method.getMethodInstance(classLoader);
                
                XposedBridge.hookMethod(reflectMethod, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (!FeatureFlags.trackInstantViewers) return;

                        ModuleLog.line("(IE|InstantTracker) 🎯 CAUGHT: " + 
                                method.getClassName() + "." + method.getName() + 
                                " with " + param.args.length + " args");
                        
                        // Dump any String arguments to see if we can spot usernames
                        for (int i = 0; i < param.args.length; i++) {
                            if (param.args[i] instanceof String) {
                                ModuleLog.line("(IE|InstantTracker)    arg[" + i + "] = " + param.args[i]);
                            }
                        }
                    }
                });
                
                ModuleLog.line("(IE|InstantTracker) Hooked: " + method.getClassName() + "." + method.getName());
            }
        } catch (Throwable t) {
            ModuleLog.line("(IE|InstantTracker) ❌ Error: " + t.getMessage());
        }
    }
}
