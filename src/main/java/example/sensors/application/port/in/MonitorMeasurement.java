package example.sensors.application.port.in;

import example.sensors.domain.Measurement;

@FunctionalInterface
public interface MonitorMeasurement {
    void check(Measurement measurement);
}
