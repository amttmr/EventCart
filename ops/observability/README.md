# EventCart Observability

This folder contains the local observability stack used by Docker Compose.

## Components

| Component | Port | Purpose |
| --- | --- | --- |
| OpenTelemetry Collector | `4317`, `4318`, `8889` | Receives OTLP telemetry and exposes collector metrics |
| Prometheus | `9090` | Scrapes Spring Boot actuator metrics |
| Grafana | `3000` | Shows the EventCart dashboard |

## Local Usage

Start the stack with:

```bash
docker compose up -d mongodb kafka redis keycloak otel-collector prometheus grafana
```

Run the Java services from IntelliJ or Maven. Prometheus scrapes them through `host.docker.internal`.

Grafana login:

| Field | Value |
| --- | --- |
| URL | `http://localhost:3000` |
| Username | `admin` |
| Password | `admin` |

## Interview Angle

Metrics answer "what is happening", traces answer "where did time go", and correlation IDs connect logs across services when tracing is unavailable or incomplete.
