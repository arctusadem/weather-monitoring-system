package example.sensors.domain;

public record Thresholds(double temperature, double humidity) {

    public Thresholds {
        if (!Double.isFinite(temperature) || !Double.isFinite(humidity)) {
            throw new IllegalArgumentException("thresholds must be finite numbers");
        }
    }

    public double forType(SensorType type) {

        return switch (type) {
            case TEMPERATURE -> temperature;
            case HUMIDITY -> humidity;
        };

    }
}
