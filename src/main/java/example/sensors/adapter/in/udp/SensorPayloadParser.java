package example.sensors.adapter.in.udp;

import example.sensors.domain.SensorReading;
import example.sensors.domain.SensorType;
import java.util.HashMap;
import java.util.Map;

public final class SensorPayloadParser {

    public SensorReading parse(String payload, SensorType type) {

        Map<String, String> fields = new HashMap<>();

        for (String field : payload.split(";", -1)) {

            String[] pair = field.split("=", 2);
            if (pair.length != 2 || fields.putIfAbsent(pair[0].trim(), pair[1].trim()) != null) {
                throw new IllegalArgumentException("expected unique key=value fields");
            }

        }

        if (fields.size() != 2 || !fields.containsKey("sensor_id") || !fields.containsKey("value")) {
            throw new IllegalArgumentException("expected sensor_id and value only");
        }

        return new SensorReading(fields.get("sensor_id"), type, Double.parseDouble(fields.get("value")));
    }
}
