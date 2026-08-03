# EventCart Docker Images

Each service has its own Dockerfile under `services/<service-name>/Dockerfile`.

Build from the repository root so each Dockerfile can package shared Maven modules:

```bash
docker build -f services/catalog-service/Dockerfile -t eventcart/catalog-service:local .
docker build -f services/api-gateway/Dockerfile -t eventcart/api-gateway:local .
```

The final runtime images use Java 21 JRE images and run as numeric non-root user `10001`.
