package example.sensors.application.service;

import example.sensors.application.port.in.CollectReading;
import example.sensors.application.port.out.MeasurementPublisher;
import example.sensors.domain.Measurement;
import example.sensors.domain.SensorReading;
import java.time.Clock;
import java.util.concurrent.CompletionStage;

public final class WarehouseService implements CollectReading {

    private final String warehouseId;
    private final MeasurementPublisher publisher;
    private final Clock clock;

    public WarehouseService(String warehouseId, MeasurementPublisher publisher, Clock clock) {
        SensorReading.requireIdentifier(warehouseId);
        this.warehouseId = warehouseId;
        this.publisher = publisher;
        this.clock = clock;
    }

    @Override
    public CompletionStage<Void> collect(SensorReading reading) {
        // The use case knows the intent (publish), never the transport (HTTP).
        return publisher.publish(new Measurement(warehouseId, reading, clock.instant()));
    }
}
