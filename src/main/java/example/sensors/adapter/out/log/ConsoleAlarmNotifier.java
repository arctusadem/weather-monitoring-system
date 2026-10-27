package example.sensors.adapter.out.log;

import example.sensors.application.port.out.AlarmNotifier;
import example.sensors.domain.Alarm;
import java.util.Locale;
import java.util.logging.Logger;

public final class ConsoleAlarmNotifier implements AlarmNotifier {

    private static final Logger LOG = Logger.getLogger(ConsoleAlarmNotifier.class.getName());

    @Override
    public void notify(Alarm alarm) {
        var measurement = alarm.measurement();
        var reading = measurement.reading();

        LOG.warning(String.format(Locale.ROOT,
                "ALARM warehouse=%s sensor=%s type=%s value=%.2f threshold=%.2f unit=%s receivedAt=%s",
                measurement.warehouseId(), reading.sensorId(), reading.type(), reading.value(),
                alarm.threshold(), reading.type().unit(), measurement.receivedAt()));
    }
}
