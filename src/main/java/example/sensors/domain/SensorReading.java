package example.sensors.domain;

import java.util.Objects;

public record SensorReading(String sensorId, SensorType type, double value) {

    public SensorReading {

        requireIdentifier(sensorId);
        Objects.requireNonNull(type, "sensor type is required");

        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("value must be a finite number");
        }

    }

    public static void requireIdentifier(String identifier) {
        if (identifier == null || !identifier.matches("[A-Za-z0-9_-]{1,64}")) {
            throw new IllegalArgumentException("IDs must contain 1-64 letters, digits, underscores or hyphens");
        }
    }
}
