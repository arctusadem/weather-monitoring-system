package example.sensors.application.service;

import example.sensors.application.port.in.MonitorMeasurement;
import example.sensors.application.port.out.AlarmNotifier;
import example.sensors.domain.Alarm;
import example.sensors.domain.Measurement;
import example.sensors.domain.Thresholds;

public final class MonitoringService implements MonitorMeasurement {

    private final Thresholds thresholds;
    private final AlarmNotifier alarms;

    public MonitoringService(Thresholds thresholds, AlarmNotifier alarms) {
        this.thresholds = thresholds;
        this.alarms = alarms;
    }

    @Override
    public void check(Measurement measurement) {
        double threshold = thresholds.forType(measurement.reading().type());
        if (measurement.reading().value() > threshold) {
            alarms.notify(new Alarm(measurement, threshold));
        }
    }
}
