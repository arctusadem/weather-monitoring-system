package example.sensors.adapter.http;

import example.sensors.domain.Measurement;
import example.sensors.domain.SensorReading;
import example.sensors.domain.SensorType;
import io.vertx.core.json.JsonObject;
import java.time.Instant;

public final class MeasurementJson {

    private MeasurementJson() {}

    public static String encode(Measurement measurement) {
        return new JsonObject()
                .put("warehouseId", measurement.warehouseId())
                .put("sensorId", measurement.reading().sensorId())
                .put("type", measurement.reading().type().name())
                .put("value", measurement.reading().value())
                .put("receivedAt", measurement.receivedAt().toString())
                .encode();
    }

    public static Measurement decode(String body) {

        JsonObject json = new JsonObject(body);
        var reading = new SensorReading(json.getString("sensorId"),
                SensorType.valueOf(json.getString("type")), json.getDouble("value"));

        return new Measurement(json.getString("warehouseId"), reading,
                Instant.parse(json.getString("receivedAt")));
    }
}
