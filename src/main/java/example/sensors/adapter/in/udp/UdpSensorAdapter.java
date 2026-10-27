package example.sensors.adapter.in.udp;

import example.sensors.application.port.in.CollectReading;
import example.sensors.domain.SensorType;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.datagram.DatagramSocket;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

public final class UdpSensorAdapter {

    private static final Logger LOG = Logger.getLogger(UdpSensorAdapter.class.getName());
    private static final int MAX_PAYLOAD_BYTES = 512;
    private final DatagramSocket temperature;
    private final DatagramSocket humidity;
    private final SensorPayloadParser parser = new SensorPayloadParser();
    private final CollectReading useCase;
    private final Semaphore available;

    public UdpSensorAdapter(Vertx vertx, CollectReading useCase, int maxInFlight) {

        if (maxInFlight < 1) throw new IllegalArgumentException("maxInFlight must be positive");

        this.useCase = useCase;
        this.available = new Semaphore(maxInFlight);
        this.temperature = vertx.createDatagramSocket();
        this.humidity = vertx.createDatagramSocket();
    }

    public Future<Void> start(String host, int temperaturePort, int humidityPort) {

        temperature.handler(packet -> receive(packet, SensorType.TEMPERATURE));
        humidity.handler(packet -> receive(packet, SensorType.HUMIDITY));
        temperature.exceptionHandler(error -> LOG.warning("Temperature UDP error: " + error.getMessage()));
        humidity.exceptionHandler(error -> LOG.warning("Humidity UDP error: " + error.getMessage()));

        return temperature.listen(temperaturePort, host)
                .compose(ignored -> humidity.listen(humidityPort, host))
                .mapEmpty();
    }

    public int temperaturePort() { return temperature.localAddress().port(); }

    public int humidityPort() { return humidity.localAddress().port(); }

    private void receive(DatagramPacket packet, SensorType type) {

        try {

            if (packet.data().length() > MAX_PAYLOAD_BYTES) {
                throw new IllegalArgumentException("payload exceeds 512 bytes");
            }

            var reading = parser.parse(packet.data().toString("UTF-8"), type);

            // UDP cannot slow down its sender. Bound outstanding HTTP requests instead.
            if (!available.tryAcquire()) {
                LOG.warning("Dropped reading: all forwarding slots are busy");
                return;
            }

            try {

                useCase.collect(reading).whenComplete((ignored, error) -> {
                    available.release();
                    if (error != null) LOG.warning("Forwarding failed: " + error.getMessage());
                });

            } catch (RuntimeException error) {
                available.release();
                throw error;
            }

        } catch (RuntimeException error) {
            LOG.warning("Rejected reading: " + error.getMessage());
        }
    }
}
