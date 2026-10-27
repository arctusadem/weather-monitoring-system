package example.sensors.adapter;

import example.sensors.adapter.in.udp.SensorPayloadParser;
import example.sensors.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class SensorPayloadParserTest {

    private final SensorPayloadParser parser = new SensorPayloadParser();

    @Test
    void parsesSensorReading() {
        assertEquals(new SensorReading("t1", SensorType.TEMPERATURE, -2.5),
                parser.parse("sensor_id=t1; value=-2.5", SensorType.TEMPERATURE));
    }

    @ParameterizedTest
    @ValueSource(strings = {"sensor_id=t1", "sensor_id=t1;value=x"})
    void rejectsMalformedPackets(String payload) {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(payload, SensorType.HUMIDITY));
    }

}
