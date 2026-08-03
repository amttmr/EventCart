# Kubernetes Manifests

The Kubernetes manifests in EventCart are production-shaped templates for learning deployment concepts. They are not a full production platform by themselves.

## Where It Is Used

Manifests live in:

```text
ops/k8s
```

Files:

| File | Purpose |
| --- | --- |
| `namespace.yaml` | Creates the `eventcart` namespace. |
| `configmap.yaml` | Stores non-secret environment values. |
| `secret.example.yaml` | Shows required secret keys without real values. |
| `services.yaml` | Defines Deployments and ClusterIP Services for EventCart apps. |
| `README.md` | Explains apply order and secret handling. |

The manifests intentionally do not deploy MongoDB, Kafka, Redis, or Keycloak. In real systems, those are often managed services or separately operated platform components.

## Why Kubernetes Is Included

Kubernetes teaches how Java microservices are packaged and operated:

- Deployments manage application pods.
- Services provide stable network names.
- ConfigMaps hold non-secret config.
- Secrets hold sensitive config.
- Probes check health.
- Resource requests and limits control scheduling and safety.
- Environment variables configure service instances.

## EventCart Deployment Shape

Each service should have:

- Container image.
- Environment variables.
- HTTP port.
- Health/readiness probes.
- Resource requests and limits.
- Service object for internal routing.

External dependencies are injected:

- MongoDB URI
- Kafka bootstrap servers
- Redis host and port
- Keycloak issuer and JWKS URLs
- OTLP endpoint
- Internal service token
- SMTP/SMS credentials

## Best Practices

- Do not commit real secrets.
- Use a secret manager, External Secrets Operator, Sealed Secrets, or CI secret store.
- Keep ConfigMap values non-sensitive.
- Use readiness probes to control traffic.
- Use liveness probes carefully so Kubernetes does not restart slow-but-healthy services.
- Set resource requests and limits.
- Use immutable image tags in real deployments.
- Keep infrastructure dependencies outside app manifests where possible.
- Use namespaces to isolate app resources.
- Add horizontal pod autoscaling only after metrics and load patterns are understood.
- Keep environment-specific values out of source-controlled base manifests.

## How To Verify Locally

If you have a Kubernetes cluster such as Docker Desktop Kubernetes, kind, or minikube:

```powershell
kubectl apply -f ops/k8s/namespace.yaml
kubectl apply -f ops/k8s/configmap.yaml
kubectl apply -f ops/k8s/secret.example.yaml
kubectl apply -f ops/k8s/services.yaml
```

Check resources:

```powershell
kubectl get all -n eventcart
kubectl get configmap -n eventcart
kubectl get secret -n eventcart
```

Describe a deployment:

```powershell
kubectl describe deployment order-service -n eventcart
```

Check pods:

```powershell
kubectl get pods -n eventcart
kubectl logs deployment/order-service -n eventcart
```

Port-forward for local testing:

```powershell
kubectl port-forward service/api-gateway 8080:8080 -n eventcart
```

## How To Debug

| Symptom | Check |
| --- | --- |
| Pod stuck `ImagePullBackOff` | Image name, registry auth, tag exists. |
| Pod `CrashLoopBackOff` | `kubectl logs`, environment variables, dependency connection settings. |
| Readiness failing | `/actuator/health` path, port, dependency health. |
| Service unreachable | Service selector labels and target port. |
| Mongo/Kafka connection failure | ConfigMap and Secret values. |
| JWT validation failure | Keycloak issuer and JWKS URLs. |
| OTLP errors | Collector endpoint and network policy. |

Useful commands:

```powershell
kubectl describe pod <pod-name> -n eventcart
kubectl logs <pod-name> -n eventcart
kubectl logs <pod-name> -n eventcart --previous
kubectl exec -it <pod-name> -n eventcart -- printenv
kubectl get events -n eventcart --sort-by=.lastTimestamp
```

## Real-Time Monitoring

Production Kubernetes monitoring should include:

- Pod restarts.
- CPU and memory usage.
- Deployment availability.
- Readiness and liveness probe failures.
- Node pressure.
- Container logs.
- Application Prometheus metrics.
- Ingress/gateway latency and errors.
- Kafka and MongoDB dependency health.

## Interview Preparation

You should be able to explain:

- Pod vs Deployment vs Service.
- ConfigMap vs Secret.
- Readiness vs liveness probes.
- Why images need tags.
- Why external services are injected through config.
- Why Kubernetes does not automatically solve application-level failures.
- How rolling deployments work.
- How to debug CrashLoopBackOff.
- Why resource requests and limits matter.

Common interview questions:

| Question | Good Answer |
| --- | --- |
| What is a Pod? | Smallest deployable unit in Kubernetes, usually one app container plus sidecars if needed. |
| What is a Deployment? | A controller that manages replica sets and rolling updates for pods. |
| What is a Service? | A stable virtual network endpoint for pods selected by labels. |
| ConfigMap vs Secret? | ConfigMap stores non-sensitive config; Secret stores sensitive values, though real environments should integrate stronger secret management. |
| Readiness vs liveness? | Readiness controls traffic routing. Liveness restarts a container that is considered unhealthy. |

## EventCart Takeaway

The Kubernetes manifests teach how the local microservices map to deployable production-style resources: deployments, services, configuration, secrets, probes, and operational debugging.

