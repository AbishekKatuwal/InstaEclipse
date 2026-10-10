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

    // Try all the strings the developer suggested
    private static final String[] SEARCH_STRINGS = {
            "quick_snap",
            "quicksnap",
            "QuickSnap",
            "quick_snap_viewer",
            "quick_snap_viewers",
            "instants",
            "QuickSnapViewerList",
            "QuickSnapSeenBy",
            "QuickSnapViewersQuery"
    };

    public void install(DexKitBridge bridge, ClassLoader classLoader) {
        ModuleLog.line("(IE|InstantTracker) ========== STARTING TRACKER ==========");

        for (String searchString : SEARCH_STRINGS) {
            ModuleLog.line("(IE|InstantTracker) Searching for: '" + searchString + "'");
            try {
                List<MethodData> methods = bridge.findMethod(FindMethod.create()
                        .matcher(MethodMatcher.create().usingStrings(searchString)));

                if (methods.isEmpty()) {
                    ModuleLog.line("(IE|InstantTracker)    -> no methods found");
                    continue;
                }

                ModuleLog.line("(IE|InstantTracker)    -> found " + methods.size() + " method(s)");

                int hooked = 0;
                for (MethodData method : methods) {
                    try {
                        Method reflectMethod = method.getMethodInstance(classLoader);
                        final String className = method.getClassName();
                        final String methodName = method.getName();
                        final String searchTag = searchString;

                        XposedBridge.hookMethod(reflectMethod, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                if (!FeatureFlags.trackInstantViewers) return;

                                ModuleLog.line("(IE|InstantTracker) CAUGHT [" + searchTag + "]: "
                                        + className + "." + methodName
                                        + " with " + (param.args != null ? param.args.length : 0) + " args");

                                if (param.args != null) {
                                    for (int i = 0; i < param.args.length && i < 5; i++) {
                                        Object arg = param.args[i];
                                        if (arg == null) {
                                            ModuleLog.line("(IE|InstantTracker)    arg[" + i + "] = null");
                                        } else if (arg instanceof String) {
                                            ModuleLog.line("(IE|InstantTracker)    arg[" + i + "] = \"" + arg + "\"");
                                        } else {
                                            ModuleLog.line("(IE|InstantTracker)    arg[" + i + "] = " + arg.getClass().getName());
                                        }
                                    }
                                }

                                Object result = param.getResult();
                                if (result != null) {
                                    ModuleLog.line("(IE|InstantTracker)    return = " + result.getClass().getName());
                                }
                            }
                        });
                        hooked++;
                    } catch (Throwable t) {
                        // skip methods we can't reflect
                    }
                }
                ModuleLog.line("(IE|InstantTracker)    -> hooked " + hooked + " method(s)");
            } catch (Throwable t) {
                ModuleLog.line("(IE|InstantTracker)    -> error: " + t.getMessage());
            }
        }

        ModuleLog.line("(IE|InstantTracker) ========== TRACKER READY ==========");
    }
}
