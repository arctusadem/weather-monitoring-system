package example.sensors.bootstrap;

import example.sensors.domain.SensorReading;
import example.sensors.domain.SensorType;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class SensorSimulator {

    private SensorSimulator() {}

    public static void send(String[] args) {

        try {

            if (args.length < 3 || args.length > 4) {
                throw new IllegalArgumentException("Usage: send temperature|humidity VALUE [SENSOR_ID]");
            }

            SensorType type = SensorType.valueOf(args[1].toUpperCase(Locale.ROOT));
            String id = args.length == 4 ? args[3] : type == SensorType.TEMPERATURE ? "t1" : "h1";
            var reading = new SensorReading(id, type, Double.parseDouble(args[2]));

            int port = type == SensorType.TEMPERATURE
                    ? Configuration.port("TEMPERATURE_PORT", 3344)
                    : Configuration.port("HUMIDITY_PORT", 3355);

            var host = InetAddress.getByName(Configuration.text("SENSOR_HOST", "127.0.0.1"));

            byte[] payload = ("sensor_id=" + reading.sensorId() + "; value=" + reading.value())
                    .getBytes(StandardCharsets.UTF_8);

            try (var socket = new DatagramSocket()) {
                socket.send(new DatagramPacket(payload, payload.length, host, port));
            }

            System.out.println("Sent UDP to " + host.getHostAddress() + ":" + port + " (delivery is not acknowledged)");

        } catch (Exception error) {
            System.err.println(error.getMessage());
            System.exit(1);
        }

    }
}
