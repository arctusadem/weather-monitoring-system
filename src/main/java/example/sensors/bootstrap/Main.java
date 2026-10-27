package example.sensors.bootstrap;

import example.sensors.adapter.in.http.HttpMonitoringAdapter;
import example.sensors.adapter.in.udp.UdpSensorAdapter;
import example.sensors.adapter.out.http.HttpMeasurementPublisher;
import example.sensors.adapter.out.log.ConsoleAlarmNotifier;
import example.sensors.application.service.MonitoringService;
import example.sensors.application.service.WarehouseService;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import java.time.Clock;
import java.util.concurrent.TimeUnit;

public final class Main {

    private Main() {}

    public static void main(String[] args) {

        String mode = args.length == 0 ? "demo" : args[0];

        if (mode.equals("send")) {
            SensorSimulator.send(args);
            return;
        }

        if (!mode.equals("demo") && !mode.equals("central") && !mode.equals("warehouse")) {
            System.err.println("Usage: java -jar weather-monitoring-system.jar [demo|central|warehouse|send TYPE VALUE [ID]]");
            System.exit(1);
        }

        Vertx vertx = Vertx.vertx(new VertxOptions().setEventLoopPoolSize(2));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // Waiting is restricted to shutdown; request handlers never wait on a future.
                vertx.close().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
            } catch (Exception error) {
                System.err.println("Shutdown did not complete: " + error.getMessage());
            }
        }, "sensor-shutdown"));

        Future<Void> started;
        try {

            started = switch (mode) {
                case "central" -> startCentral(vertx);
                case "warehouse" -> startWarehouse(vertx);
                default -> startCentral(vertx).compose(ignored -> startWarehouse(vertx));
            };

        } catch (RuntimeException error) {
            started = Future.failedFuture(error);
        }

        started.onSuccess(ignored -> System.out.println("Ready (" + mode + "). Press Ctrl+C to stop."))
                .onFailure(error -> {
                    System.err.println("Startup failed: " + error.getMessage());
                    // System.exit invokes the shutdown hook, which releases sockets and event loops.
                    new Thread(() -> System.exit(1), "startup-failure").start();
                });
    }

    private static Future<Void> startCentral(Vertx vertx) {

        var service = new MonitoringService(Configuration.thresholds(), new ConsoleAlarmNotifier());
        var adapter = new HttpMonitoringAdapter(vertx, service);

        String host = Configuration.text("BIND_HOST", "127.0.0.1");

        return adapter.start(host, Configuration.port("CENTRAL_PORT", 8080))
                .onSuccess(ignored -> System.out.println("Central HTTP: " + host + ":" + adapter.port()));

    }

    private static Future<Void> startWarehouse(Vertx vertx) {

        var publisher = new HttpMeasurementPublisher(Configuration.centralEndpoint());
        var service = new WarehouseService(Configuration.text("WAREHOUSE_ID", "wh-01"),
                publisher, Clock.systemUTC());
        var adapter = new UdpSensorAdapter(vertx, service, Configuration.positiveInt("MAX_IN_FLIGHT", 64));

        String host = Configuration.text("BIND_HOST", "127.0.0.1");

        return adapter.start(host, Configuration.port("TEMPERATURE_PORT", 3344),
                        Configuration.port("HUMIDITY_PORT", 3355))
                .onSuccess(ignored -> System.out.printf("Warehouse UDP: %s:%d (temperature), %s:%d (humidity)%n",
                        host, adapter.temperaturePort(), host, adapter.humidityPort()));
    }
}
