# Prerequisites

This document lists the software, accounts, and basic verification commands needed before we start building EventCart.

## Software To Install

| Software | Recommended Version | Why We Need It |
| --- | --- | --- |
| Java JDK | Java 25 LTS | Main programming language and runtime |
| IntelliJ IDEA | Community or Ultimate | Java/Spring development IDE |
| Git | Latest stable | Source control and GitHub workflow |
| Maven | Use Maven Wrapper in project | Build, dependency management, multi-module project |
| Docker Desktop | Latest stable | Run MongoDB, Kafka, Redis, Keycloak, Prometheus, Grafana locally |
| Postman or Bruno | Latest stable | Manual API testing |
| MongoDB Compass | Latest stable | Inspect MongoDB databases, collections, documents, and indexes |
| Visual Studio Code | Optional | Markdown docs, YAML, Docker files |
| Windows Terminal | Optional | Better command-line experience on Windows |
| Node.js LTS | Optional | Useful later if we add a frontend/admin UI |
| Kubernetes CLI, `kubectl` | Optional for later phase | Deployment learning |
| Helm | Optional for later phase | Kubernetes package management |

## Accounts To Create

| Account | Required? | Usage |
| --- | --- | --- |
| GitHub | Yes | Store project, practice pull requests, CI/CD with GitHub Actions |
| Docker Hub | Recommended | Push Docker images later |
| MongoDB Atlas | Optional | Cloud MongoDB practice after local development |
| Confluent Cloud | Optional | Managed Kafka practice after local Kafka |
| Postman | Optional | Sync API collections |
| SonarCloud | Optional | Code quality scanning |

For the first local version, only GitHub is strongly required. Docker Hub, MongoDB Atlas, and Confluent Cloud become useful when we move from local development to cloud-style deployment practice.

## Windows Verification Commands

Run these commands in PowerShell after installation:

```powershell
java --version
git --version
docker --version
docker compose version
```

After the Maven wrapper is added to the project, use:

```powershell
.\mvnw --version
```

## Recommended Local Ports

| Tool | Port |
| --- | --- |
| API Gateway | 8080 |
| Catalog Service | 8081 |
| Cart Service | 8082 |
| Order Service | 8083 |
| Inventory Service | 8084 |
| Payment Service | 8085 |
| Notification Service | 8086 |
| MongoDB | 27017 |
| Kafka | 9092 |
| Redis | 6379 |
| Keycloak | 8090 |
| Prometheus | 9090 |
| Grafana | 3000 |

## Version Notes

The project baseline was checked on 2026-08-02:

- Spring Boot 4.1.0 was released on 2026-06-10.
- Apache Kafka 4.3.1 is a supported Kafka release.
- MongoDB 8.3 is the latest minor release line.
- JDK 25 reached general availability on 2025-09-16 and is treated as the current LTS baseline by most vendors.

## Before We Generate Code

Confirm these are working:

1. `java --version` shows Java 25.
2. `docker --version` works.
3. Docker Desktop can start containers.
4. Git is installed and configured:

```powershell
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
```

