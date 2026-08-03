# Deployment View

This document explains how EventCart runs locally and how the same services map to a Kubernetes-style deployment.

## Local Docker Compose View

Local infrastructure is defined in [../../compose.yaml](../../compose.yaml).

```mermaid
flowchart TD
    Developer["Developer Machine"]
    Services["Spring Boot services<br/>run from IntelliJ or Maven"]
    Compose["Docker Compose"]
    Mongo["eventcart-mongodb<br/>27017"]
    Kafka["eventcart-kafka<br/>9092"]
    Redis["eventcart-redis<br/>6379"]
    Keycloak["eventcart-keycloak<br/>8088"]
    OTel["eventcart-otel-collector<br/>4317/4318/8889"]
    Prometheus["eventcart-prometheus<br/>9090"]
    Grafana["eventcart-grafana<br/>3000"]

    Developer --> Services
    Developer --> Compose
    Compose --> Mongo
    Compose --> Kafka
    Compose --> Redis
    Compose --> Keycloak
    Compose --> OTel
    Compose --> Prometheus
    Compose --> Grafana
    Services --> Mongo
    Services --> Kafka
    Services --> Redis
    Services --> Keycloak
    Services --> OTel
    Prometheus --> OTel
    Grafana --> Prometheus
```

Start local infrastructure:

```powershell
docker compose up -d
```

Run services from IntelliJ or Maven using their configured ports.

## Docker Image View

Each service has its own Dockerfile:

| Service | Dockerfile |
| --- | --- |
| API Gateway | `services/api-gateway/Dockerfile` |
| Catalog Service | `services/catalog-service/Dockerfile` |
| Cart Service | `services/cart-service/Dockerfile` |
| Order Service | `services/order-service/Dockerfile` |
| Inventory Service | `services/inventory-service/Dockerfile` |
| Payment Service | `services/payment-service/Dockerfile` |
| Notification Service | `services/notification-service/Dockerfile` |

Each Dockerfile uses Java 25 base images by default:

```text
eclipse-temurin:25-jdk
eclipse-temurin:25-jre
```

CI builds images through:

```bash
bash .github/scripts/docker-build-service.sh <service-name> <image-tag>
```

The script pre-pulls base images with retry and then builds the service image.

## Kubernetes View

Kubernetes manifests live under [../../ops/k8s](../../ops/k8s).

```mermaid
flowchart TD
    Ingress["Ingress or Load Balancer"]
    GatewaySvc["api-gateway Service"]
    GatewayPod["api-gateway Deployment"]

    CatalogPod["catalog-service Deployment"]
    CartPod["cart-service Deployment"]
    OrderPod["order-service Deployment"]
    InventoryPod["inventory-service Deployment"]
    PaymentPod["payment-service Deployment"]
    NotificationPod["notification-service Deployment"]

    ConfigMap["ConfigMap<br/>non-secret config"]
    Secret["Secret<br/>credentials and tokens"]
    Platform["Platform Dependencies<br/>MongoDB, Kafka, Redis, Keycloak"]
    Observability["Prometheus, Grafana,<br/>OpenTelemetry"]

    Ingress --> GatewaySvc
    GatewaySvc --> GatewayPod
    GatewayPod --> CatalogPod
    GatewayPod --> CartPod
    GatewayPod --> OrderPod
    GatewayPod --> InventoryPod
    GatewayPod --> PaymentPod
    GatewayPod --> NotificationPod

    ConfigMap --> GatewayPod
    ConfigMap --> CatalogPod
    ConfigMap --> CartPod
    ConfigMap --> OrderPod
    ConfigMap --> InventoryPod
    ConfigMap --> PaymentPod
    ConfigMap --> NotificationPod

    Secret --> GatewayPod
    Secret --> CatalogPod
    Secret --> CartPod
    Secret --> OrderPod
    Secret --> InventoryPod
    Secret --> PaymentPod
    Secret --> NotificationPod

    CatalogPod --> Platform
    CartPod --> Platform
    OrderPod --> Platform
    InventoryPod --> Platform
    PaymentPod --> Platform
    NotificationPod --> Platform
    GatewayPod --> Platform

    GatewayPod --> Observability
    CatalogPod --> Observability
    CartPod --> Observability
    OrderPod --> Observability
    InventoryPod --> Observability
    PaymentPod --> Observability
    NotificationPod --> Observability
```

## Current Kubernetes Scope

The current manifests are application templates. They do not install MongoDB, Kafka, Redis, or Keycloak inside the cluster. In a production-style setup, these dependencies are usually managed by a platform team or cloud provider.

## Deployment Responsibilities

| Resource | Responsibility |
| --- | --- |
| Namespace | Groups EventCart resources. |
| ConfigMap | Stores non-secret environment values such as URLs and topic names. |
| Secret | Stores credentials and tokens. |
| Deployment | Runs service pods and controls replicas. |
| Service | Provides stable internal networking for pods. |
| Probes | Help Kubernetes restart or stop routing to unhealthy pods. |

## Operational Checklist

Before deployment:

1. Build and publish service images.
2. Create or update secrets.
3. Confirm MongoDB, Kafka, Redis, and Keycloak endpoints.
4. Apply namespace, config, secrets, deployments, and services.
5. Verify health endpoints.
6. Verify logs, metrics, and traces.

Useful commands:

```powershell
kubectl get pods -n eventcart
kubectl get svc -n eventcart
kubectl logs deployment/order-service -n eventcart
kubectl describe pod <pod-name> -n eventcart
```
