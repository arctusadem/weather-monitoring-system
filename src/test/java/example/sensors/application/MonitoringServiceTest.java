package example.sensors.application;

import example.sensors.application.service.MonitoringService;
import example.sensors.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.time.Instant;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class MonitoringServiceTest {

    @ParameterizedTest
    @CsvSource({"TEMPERATURE,34.9,0", "TEMPERATURE,35,0", "TEMPERATURE,35.1,1",
            "HUMIDITY,49.9,0", "HUMIDITY,50,0", "HUMIDITY,50.1,1"})
    void alarmsOnlyAboveThreshold(SensorType type, double value, int count) {
        var alarms = new ArrayList<Alarm>();
        var service = new MonitoringService(new Thresholds(35, 50), alarms::add);
        var measurement = measurement("wh-01", type, value);
        service.check(measurement);
        assertEquals(count, alarms.size());
        if (count == 1) assertSame(measurement, alarms.get(0).measurement());
    }

    @Test
    void respectsCustomThresholdsAndPreservesWarehouseIdentity() {
        var alarms = new ArrayList<Alarm>();
        var service = new MonitoringService(new Thresholds(40, 60), alarms::add);
        service.check(measurement("wh-01", SensorType.TEMPERATURE, 36));
        service.check(measurement("wh-02", SensorType.TEMPERATURE, 41));
        assertEquals(1, alarms.size());
        assertEquals("wh-02", alarms.get(0).measurement().warehouseId());
        assertEquals(40, alarms.get(0).threshold());
    }

    private Measurement measurement(String warehouse, SensorType type, double value) {
        return new Measurement(warehouse, new SensorReading("sensor-1", type, value), Instant.EPOCH);
    }

}
