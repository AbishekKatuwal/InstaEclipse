package ps.reso.instaeclipse.mods.location;

import android.os.Handler;
import android.os.Looper;

import java.util.Random;

import ps.reso.instaeclipse.utils.feature.FeatureFlags;
import ps.reso.instaeclipse.utils.log.ModuleLog;

public class LiveLocationMover {

    private static final long TICK_MS = 15_000L;
    private static final Random RNG = new Random();

    private static Handler handler;
    private static boolean running = false;

    public static void start() {
        if (running) return;
        running = true;
        handler = new Handler(Looper.getMainLooper());
        handler.post(tick);
        ModuleLog.line("(IE|LiveLocation) ✅ started");
    }

    public static void stop() {
        running = false;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        ModuleLog.line("(IE|LiveLocation) stopped");
    }

    public static void setAnchor(double lat, double lng) {
        FeatureFlags.liveLocationAnchorLat = lat;
        FeatureFlags.liveLocationAnchorLng = lng;
        FeatureFlags.spoofLat = lat;
        FeatureFlags.spoofLng = lng;
    }

    private static final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            if (FeatureFlags.liveLocationEnabled && FeatureFlags.spoofLocation) {
                moveOneStep();
            }
            handler.postDelayed(this, TICK_MS);
        }
    };

    private static void moveOneStep() {
        try {
            double radius = Math.max(10, FeatureFlags.liveLocationRadiusMeters);
            double metersPerDegLat = 111_320.0;
            double metersPerDegLng = 111_320.0 * Math.cos(Math.toRadians(FeatureFlags.liveLocationAnchorLat));
            if (metersPerDegLng < 1) metersPerDegLng = 1;

            double angle = RNG.nextDouble() * Math.PI * 2;
            double dist  = RNG.nextDouble() * radius;
            double dLat  = (dist * Math.cos(angle)) / metersPerDegLat;
            double dLng  = (dist * Math.sin(angle)) / metersPerDegLng;

            FeatureFlags.spoofLat = FeatureFlags.liveLocationAnchorLat + dLat;
            FeatureFlags.spoofLng = FeatureFlags.liveLocationAnchorLng + dLng;

            ModuleLog.line(String.format("(IE|LiveLocation) moved → %.5f, %.5f",
                    FeatureFlags.spoofLat, FeatureFlags.spoofLng));
        } catch (Throwable t) {
            ModuleLog.line("(IE|LiveLocation) ⚠️ " + t.getMessage());
        }
    }
              }
