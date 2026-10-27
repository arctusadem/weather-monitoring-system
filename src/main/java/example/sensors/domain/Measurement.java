package example.sensors.domain;

import java.time.Instant;
import java.util.Objects;

public record Measurement(String warehouseId, SensorReading reading, Instant receivedAt) {

    public Measurement {
        SensorReading.requireIdentifier(warehouseId);

        Objects.requireNonNull(reading, "reading is required");
        Objects.requireNonNull(receivedAt, "receipt time is required");
    }

}
