# weather-monitoring-system

Small Java 17 + Vert.x example. Sensors send UDP to a warehouse, the warehouse forwards readings over HTTP, and central logs alarms. No broker or database needed.

## Run

From this folder in PowerShell. First build needs internet; Maven is downloaded by the wrapper.

```powershell
.\mvnw.cmd clean verify
java -jar target/weather-monitoring-system.jar demo
```

Wait for `Ready (demo)`, then use another terminal:

```powershell
java -jar target/weather-monitoring-system.jar send temperature 36
java -jar target/weather-monitoring-system.jar send humidity 51
```

Both should print an `ALARM` in the first terminal. Temperature must be above 35 C, humidity above 50%. Equal values don't trigger. Ctrl+C stops it.

The sender uses plain UDP text, e.g. `sensor_id=t1; value=36`. Temperature goes to port 3344, humidity to 3355.

To run separately instead of `demo`, use these in two terminals:

```powershell
java -jar target/weather-monitoring-system.jar central
java -jar target/weather-monitoring-system.jar warehouse
```

If Java isn't working, point PowerShell at your JDK (adjust the path if needed):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

On Linux/macOS, build with `sh mvnw clean verify`.

## Structure

Hexagonal, with the wiring in `Main`:

- `domain`: readings, measurements, thresholds and alarms.
- `application`: use cases and port interfaces. No framework dependencies.
- `adapter`: UDP input, HTTP input/output and console logging.
- `bootstrap`: startup, config and the test sender.

Incoming ports (`CollectReading`, `MonitorMeasurement`) are implemented by the services. Outgoing ports (`MeasurementPublisher`, `AlarmNotifier`) are implemented by adapters.

Flow: UDP adapter -> warehouse use case -> HTTP -> central use case -> console alarm.

To add RabbitMQ later, replace the HTTP adapters and update the wiring. The use cases can stay the same.

## Config

Environment variables, set before startup. Example: `$env:WAREHOUSE_ID = 'warehouse-a'`.

| Variable | Default |
| --- | --- |
| `WAREHOUSE_ID` | `wh-01` |
| `BIND_HOST` | `127.0.0.1` |
| `CENTRAL_PORT` | `8080` |
| `CENTRAL_URL` | `http://127.0.0.1:8080/measurements` |
| `TEMPERATURE_PORT` | `3344` |
| `HUMIDITY_PORT` | `3355` |
| `TEMPERATURE_THRESHOLD` | `35` |
| `HUMIDITY_THRESHOLD` | `50` |
| `MAX_IN_FLIGHT` | `64` |
| `SENSOR_HOST` | `127.0.0.1` |

`CENTRAL_URL` overrides the default URL built from `CENTRAL_PORT`. For another warehouse on the same machine, use a different ID and UDP ports. Set matching ports in the sender terminal.

## Notes

- Every above-threshold reading logs an alarm. Invalid packets are logged and skipped.
- Input and forwarding are asynchronous. UDP itself doesn't guarantee delivery.
- At most 64 HTTP requests can be pending by default; extra readings are dropped. Requests time out after 3 seconds.
- No retries, persistence, deduplication or authentication. If central is down, readings can be lost.
- `clean verify` builds the JAR and tests parsing, thresholds, warehouse forwarding and the UDP-to-HTTP flow. The end-to-end test uses real local sockets.
