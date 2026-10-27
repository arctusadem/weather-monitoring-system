package example.sensors.bootstrap;

import example.sensors.domain.Thresholds;
import java.net.URI;

public final class Configuration {

    private Configuration() {}

    public static String text(String name, String fallback) {
        return System.getenv().getOrDefault(name, fallback);
    }

    public static int positiveInt(String name, int fallback) {
        int value = Integer.parseInt(text(name, Integer.toString(fallback)));
        if (value < 1) throw new IllegalArgumentException(name + " must be positive");
        return value;
    }

    public static int port(String name, int fallback) {
        int value = positiveInt(name, fallback);
        if (value > 65535) throw new IllegalArgumentException(name + " must be at most 65535");
        return value;
    }

    public static Thresholds thresholds() {
        return new Thresholds(Double.parseDouble(text("TEMPERATURE_THRESHOLD", "35")),
                Double.parseDouble(text("HUMIDITY_THRESHOLD", "50")));
    }

    public static URI centralEndpoint() {
        return URI.create(text("CENTRAL_URL",
                "http://127.0.0.1:" + port("CENTRAL_PORT", 8080) + "/measurements"));
    }
}
