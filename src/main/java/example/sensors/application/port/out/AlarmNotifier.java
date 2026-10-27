package example.sensors.application.port.out;

import example.sensors.domain.Alarm;

@FunctionalInterface
public interface AlarmNotifier {
    void notify(Alarm alarm);
}
