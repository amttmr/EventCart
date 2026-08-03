# EventCart Kubernetes

These manifests are production-shaped templates for learning and interview prep.

They intentionally do not deploy MongoDB, Kafka, Redis, or Keycloak. In production those are usually managed services or separately operated platform components. EventCart services receive connection details through `ConfigMap` and `Secret` values.

## Files

| File | Purpose |
| --- | --- |
| `namespace.yaml` | Creates the `eventcart` namespace |
| `configmap.yaml` | Non-secret service URLs and observability settings |
| `secret.example.yaml` | Example secret keys; replace values before applying |
| `services.yaml` | Deployments and ClusterIP services for EventCart apps |

## Apply Order

```bash
kubectl apply -f ops/k8s/namespace.yaml
kubectl apply -f ops/k8s/configmap.yaml
kubectl apply -f ops/k8s/secret.example.yaml
kubectl apply -f ops/k8s/services.yaml
```

For real environments, create secrets through your platform secret manager, External Secrets Operator, Sealed Secrets, or your CI/CD secret store instead of committing real secret values.

Important secret keys used by the current manifests include MongoDB URIs, SMTP credentials, Twilio-compatible SMS credentials, and `EVENTCART_INTERNAL_SERVICE_TOKEN` for narrow service-to-service cart cleanup.
