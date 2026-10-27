package example.sensors.adapter.out.http;

import example.sensors.adapter.http.MeasurementJson;
import example.sensors.application.port.out.MeasurementPublisher;
import example.sensors.domain.Measurement;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

public final class HttpMeasurementPublisher implements MeasurementPublisher {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    private final URI endpoint;

    public HttpMeasurementPublisher(URI endpoint) {
        if (!"http".equals(endpoint.getScheme()) && !"https".equals(endpoint.getScheme())) {
            throw new IllegalArgumentException("CENTRAL_URL must use http or https");
        }
        this.endpoint = endpoint;
    }

    @Override
    public CompletionStage<Void> publish(Measurement measurement) {

        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(3))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(MeasurementJson.encode(measurement)))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenAccept(response -> {
                    if (response.statusCode() != 204) {
                        throw new IllegalStateException("central returned HTTP " + response.statusCode());
                    }
                });
    }
}
