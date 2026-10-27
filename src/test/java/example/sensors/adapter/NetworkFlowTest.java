package example.sensors.adapter;

import example.sensors.adapter.in.http.HttpMonitoringAdapter;
import example.sensors.adapter.in.udp.UdpSensorAdapter;
import example.sensors.adapter.out.http.HttpMeasurementPublisher;
import example.sensors.application.service.MonitoringService;
import example.sensors.application.service.WarehouseService;
import example.sensors.domain.*;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class NetworkFlowTest {

    private Vertx vertx;

    @BeforeEach
    void setUp() {
        vertx = Vertx.vertx(new VertxOptions().setEventLoopPoolSize(2));
    }

    @AfterEach
    void tearDown() throws Exception {
        await(vertx.close().toCompletionStage());
    }

    @Test
    void forwardsBothSensorTypesFromUdpToCentralAlarms() throws Exception {
        var alarms = new LinkedBlockingQueue<Alarm>();
        var monitoring = new MonitoringService(new Thresholds(35, 50), alarms::add);
        var central = new HttpMonitoringAdapter(vertx, monitoring);
        await(central.start("127.0.0.1", 0).toCompletionStage());

        var endpoint = URI.create("http://127.0.0.1:" + central.port() + "/measurements");
        var warehouse = new WarehouseService("wh-01", new HttpMeasurementPublisher(endpoint), Clock.systemUTC());
        var udp = new UdpSensorAdapter(vertx, warehouse, 8);
        await(udp.start("127.0.0.1", 0, 0).toCompletionStage());

        send(udp.temperaturePort(), "sensor_id=t1; value=36");
        var temperatureAlarm = alarms.poll(5, TimeUnit.SECONDS);
        assertNotNull(temperatureAlarm);
        assertEquals("wh-01", temperatureAlarm.measurement().warehouseId());
        assertEquals(new SensorReading("t1", SensorType.TEMPERATURE, 36), temperatureAlarm.measurement().reading());
        assertEquals(35, temperatureAlarm.threshold());

        send(udp.humidityPort(), "sensor_id=h1; value=51");
        var humidityAlarm = alarms.poll(5, TimeUnit.SECONDS);
        assertNotNull(humidityAlarm);
        assertEquals("wh-01", humidityAlarm.measurement().warehouseId());
        assertEquals(new SensorReading("h1", SensorType.HUMIDITY, 51), humidityAlarm.measurement().reading());
        assertEquals(50, humidityAlarm.threshold());
    }

    private static void send(int port, String payload) throws Exception {
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        try (var socket = new DatagramSocket()) {
            socket.send(new DatagramPacket(bytes, bytes.length, InetAddress.getByName("127.0.0.1"), port));
        }
    }

    private static <T> T await(CompletionStage<T> stage) throws Exception {
        return stage.toCompletableFuture().get(5, TimeUnit.SECONDS);
    }
}
