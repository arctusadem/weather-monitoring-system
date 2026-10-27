package example.sensors.application.port.in;

import example.sensors.domain.SensorReading;
import java.util.concurrent.CompletionStage;

@FunctionalInterface
public interface CollectReading {
    CompletionStage<Void> collect(SensorReading reading);
}
