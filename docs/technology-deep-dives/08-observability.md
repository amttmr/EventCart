# Micrometer, Actuator, Prometheus, Grafana, And OpenTelemetry

The EventCart observability stack helps developers answer three questions:

- Is the service healthy?
- What is happening?
- Where did time go?

## Where It Is Used

Application dependencies and config appear in each service.

Local infrastructure:

- `compose.yaml`
- `ops/observability/prometheus/prometheus.yml`
- `ops/observability/grafana`
- `ops/observability/otel/otel-collector-config.yml`

Local URLs:

| Component | URL |
| --- | --- |
| Service health | `http://localhost:<service-port>/actuator/health` |
| Service metrics | `http://localhost:<service-port>/actuator/metrics` |
| Prometheus scrape output | `http://localhost:<service-port>/actuator/prometheus` |
| Prometheus UI | `http://localhost:9090` |
| Grafana UI | `http://localhost:3000` |
| OTLP HTTP endpoint | `http://localhost:4318/v1/traces` |

## Component Responsibilities

| Component | Role |
| --- | --- |
| Spring Boot Actuator | Exposes health, info, metrics, and Prometheus endpoints. |
| Micrometer | Application metrics facade used by Spring Boot. |
| Prometheus registry | Exposes metrics in Prometheus format. |
| Prometheus | Scrapes and stores time-series metrics. |
| Grafana | Visualizes Prometheus metrics through dashboards. |
| OpenTelemetry | Trace instrumentation and export format. |
| OpenTelemetry Collector | Receives telemetry and can export to other backends. |
| Correlation IDs | Connect logs across service boundaries even when tracing is unavailable. |

## Why It Is Used

Microservices are harder to debug than monoliths because one user action crosses many services:

```text
gateway -> cart -> catalog
order -> cart
order -> Kafka -> inventory -> Kafka -> payment -> Kafka -> order
Kafka -> notification
```

Observability gives visibility into:

- Health status.
- HTTP request counts and latency.
- JVM memory and GC.
- MongoDB and Kafka client metrics.
- Service startup failures.
- Trace/correlation flow.
- Prometheus targets.
- Grafana dashboard panels.

## Best Practices

- Expose health endpoints for every service.
- Keep liveness and readiness concepts separate in production.
- Include correlation IDs in logs and responses.
- Use structured, high-signal logs.
- Monitor latency, error rate, traffic, and saturation.
- Monitor Kafka consumer lag separately from HTTP metrics.
- Use dashboards for common signals and alerts for actionable failures.
- Do not expose actuator endpoints publicly without security.
- Keep `/actuator/prometheus` accessible only to Prometheus in production.
- Use traces for cross-service timing, not as a replacement for logs.

## How To Verify Locally

Start observability stack:

```powershell
docker compose up -d otel-collector prometheus grafana
```

Check service health:

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8083/actuator/health
```

Check Prometheus metrics endpoint:

```bash
curl http://localhost:8083/actuator/prometheus
```

Open Prometheus:

```text
http://localhost:9090
```

Check targets:

```text
http://localhost:9090/targets
```

Open Grafana:

```text
http://localhost:3000
```

Local login:

```text
username: admin
password: admin
```

## Useful Prometheus Queries

Request count:

```promql
sum by (job) (http_server_requests_seconds_count)
```

HTTP error count:

```promql
sum by (job, status) (http_server_requests_seconds_count{status=~"5..|4.."})
```

95th percentile latency:

```promql
histogram_quantile(0.95, sum by (le, job) (rate(http_server_requests_seconds_bucket[5m])))
```

JVM memory:

```promql
jvm_memory_used_bytes
```

Process CPU:

```promql
process_cpu_usage
```

## How To Debug

| Symptom | Check |
| --- | --- |
| Service health DOWN | Open `/actuator/health` and check component details. |
| Prometheus target DOWN | Check service port, metrics path, and `host.docker.internal` reachability. |
| Grafana dashboard empty | Check Prometheus datasource and target status. |
| Trace missing | Check OTLP endpoint and `management.tracing.sampling.probability`. |
| Logs cannot be correlated | Check `X-Correlation-Id` response header and MDC log pattern. |
| High latency | Compare HTTP metrics, service logs, MongoDB, Kafka lag, and trace spans. |

Useful commands:

```powershell
docker compose ps prometheus grafana otel-collector
docker logs eventcart-prometheus
docker logs eventcart-grafana
docker logs eventcart-otel-collector
```

## Developer Verification Points

When adding a new service or endpoint:

1. Add actuator and Prometheus dependencies.
2. Confirm `/actuator/health` works.
3. Confirm `/actuator/prometheus` works.
4. Add Prometheus scrape config if needed.
5. Add dashboard panel only for useful metrics.
6. Confirm correlation ID appears in logs.
7. Confirm errors are logged with enough context.

## Interview Preparation

You should be able to explain:

- Difference between logs, metrics, and traces.
- What Actuator provides.
- What Micrometer does.
- How Prometheus scrapes metrics.
- Why Grafana is used.
- What OpenTelemetry standardizes.
- What a correlation ID is.
- What RED metrics are: rate, errors, duration.
- What USE metrics are: utilization, saturation, errors.

Common interview questions:

| Question | Good Answer |
| --- | --- |
| What is Actuator? | Spring Boot feature exposing operational endpoints such as health, metrics, and info. |
| What is Micrometer? | A metrics facade that lets applications publish metrics to backends such as Prometheus. |
| What is Prometheus? | A time-series metrics system that scrapes endpoints and stores metrics. |
| What is Grafana? | A visualization tool for dashboards and alerts. |
| What is OpenTelemetry? | A standard for telemetry data such as traces, metrics, and logs. |
| Why need correlation IDs if we have traces? | They are simple, visible in logs, and still help when tracing is incomplete or unavailable. |

## EventCart Takeaway

The observability stack makes EventCart easier to operate and explain. It connects health checks, metrics, dashboards, traces, and correlation IDs into one developer workflow.

