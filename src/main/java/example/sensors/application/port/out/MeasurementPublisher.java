package example.sensors.application.port.out;

import example.sensors.domain.Measurement;
import java.util.concurrent.CompletionStage;

@FunctionalInterface
public interface MeasurementPublisher {
    CompletionStage<Void> publish(Measurement measurement);
}
