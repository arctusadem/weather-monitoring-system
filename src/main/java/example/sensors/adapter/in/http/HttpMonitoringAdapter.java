package example.sensors.adapter.in.http;

import example.sensors.adapter.http.MeasurementJson;
import example.sensors.application.port.in.MonitorMeasurement;
import example.sensors.domain.Measurement;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import java.util.logging.Logger;

public final class HttpMonitoringAdapter {

    private static final Logger LOG = Logger.getLogger(HttpMonitoringAdapter.class.getName());
    private static final int MAX_BODY_BYTES = 4096;
    private final HttpServer server;
    private final MonitorMeasurement useCase;

    public HttpMonitoringAdapter(Vertx vertx, MonitorMeasurement useCase) {
        this.useCase = useCase;
        this.server = vertx.createHttpServer().requestHandler(this::handle);
    }

    public Future<Void> start(String host, int port) {
        return server.listen(port, host).mapEmpty();
    }

    public int port() { return server.actualPort(); }

    private void handle(HttpServerRequest request) {

        if (request.method() != HttpMethod.POST || !request.path().equals("/measurements")) {
            request.response().setStatusCode(404).end();
            return;
        }

        Buffer body = Buffer.buffer();

        request.exceptionHandler(error -> LOG.warning("HTTP input failed: " + error.getMessage()));
        request.handler(chunk -> {
            if (request.response().ended()) return;

            if (body.length() + chunk.length() > MAX_BODY_BYTES) {
                request.response().setStatusCode(413).putHeader("Connection", "close").end();
            } else {
                body.appendBuffer(chunk);
            }
        });

        request.endHandler(ignored -> {
            if (request.response().ended()) return;

            Measurement measurement;
            try {
                measurement = MeasurementJson.decode(body.toString("UTF-8"));
            } catch (RuntimeException invalidInput) {
                request.response().setStatusCode(400).end("Invalid measurement");
                return;
            }

            try {
                useCase.check(measurement);
                request.response().setStatusCode(204).end();
            } catch (RuntimeException processingFailure) {
                LOG.warning("Monitoring failed: " + processingFailure.getMessage());
                request.response().setStatusCode(500).end("Monitoring failed");
            }
        });
    }
}
