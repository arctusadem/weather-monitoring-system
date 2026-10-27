package example.sensors.application;

import example.sensors.application.service.WarehouseService;
import example.sensors.domain.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class WarehouseServiceTest {

    @Test
    void enrichesAndPublishesReading() {
        var sent = new AtomicReference<Measurement>();
        var time = Instant.parse("2026-01-01T12:00:00Z");
        var service = new WarehouseService("warehouse-a", measurement -> {
            sent.set(measurement);
            return CompletableFuture.completedFuture(null);
        }, Clock.fixed(time, ZoneOffset.UTC));
        var reading = new SensorReading("t1", SensorType.TEMPERATURE, 30);

        service.collect(reading).toCompletableFuture().join();

        assertEquals(new Measurement("warehouse-a", reading, time), sent.get());
    }

}
