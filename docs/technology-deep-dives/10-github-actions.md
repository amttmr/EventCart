# GitHub Actions

GitHub Actions is the CI/CD automation tool used by EventCart to build, test, and package the project.

## Where It Is Used

Workflow file:

```text
.github/workflows/ci.yml
```

Current jobs:

| Job | Purpose |
| --- | --- |
| `build-test` | Checks out code, sets up Java 25, runs unit tests, runs integration tests. |
| `docker-build` | Builds Docker images for each service after tests pass. |
| `docker-publish` | Publishes tagged Docker images to GHCR for version tags. |

## Why It Is Used

CI/CD is included because a microservices project is not complete unless it can be tested and packaged consistently outside a developer laptop.

GitHub Actions gives EventCart:

- Repeatable builds.
- Pull request validation.
- Maven dependency caching.
- Test execution.
- Docker image build verification.
- Release image publishing on tags.
- A real interview story for CI/CD.

## Workflow Summary

Triggers:

```yaml
on:
  pull_request:
  push:
    branches:
      - main
      - master
```

Java setup:

```yaml
uses: actions/setup-java@v4
with:
  distribution: temurin
  java-version: "25"
  cache: maven
```

Main test commands:

```bash
./mvnw test
./mvnw -P integration-tests verify
```

Docker image build:

```bash
bash .github/scripts/docker-build-service.sh "${{ matrix.service }}" "eventcart/${{ matrix.service }}:${{ github.sha }}"
```

Tag publishing:

```bash
ghcr.io/<owner>/eventcart/<service>:<tag>
```

## Best Practices

- Run tests on every pull request.
- Cache Maven dependencies to reduce build time.
- Use a fixed Java version matching production target.
- Keep CI commands close to local developer commands.
- Split build/test and Docker packaging into separate jobs.
- Use matrix builds for repeated service image builds.
- Retry external Docker pulls/builds because public registries can have transient network failures.
- Keep base Docker images configurable with build arguments.
- Publish only from trusted refs such as version tags.
- Store secrets in GitHub Secrets, not in code.
- Avoid pushing images if tests fail.
- Keep workflows simple until the project needs more stages.
- Add status checks to branch protection.

## How To Verify Behavior

Locally, run the same commands as CI:

```powershell
.\mvnw.cmd test
.\mvnw.cmd -P integration-tests verify
```

Build one Docker image locally:

```powershell
docker build -f services/order-service/Dockerfile -t eventcart/order-service:local .
```

Inspect GitHub Actions in the repository:

```text
GitHub repository -> Actions tab -> EventCart CI
```

Expected successful workflow:

- Unit tests pass.
- Integration tests pass.
- Docker images build for all services.
- Images publish only when pushing a tag like `v0.1.0`.

## How To Debug

| Symptom | Check |
| --- | --- |
| Maven dependency failure | Check Maven Central/network issue, dependency version, and cache. |
| Testcontainers failure | Check Docker availability on runner and failing service logs. |
| Unit test failure | Open failing test report and reproduce locally. |
| Docker build failure | Check Dockerfile path, jar packaging, and build context. |
| Docker Hub timeout | Re-run the job; the CI script pre-pulls Java base images with retry before building. |
| Publish failure | Check package permissions and `GITHUB_TOKEN` permissions. |
| Workflow not triggered | Check branch, tag, and `on` trigger rules. |

Useful local commands:

```powershell
.\mvnw.cmd -q test
.\mvnw.cmd -P integration-tests -pl e2e-tests -am verify
docker build -f services/catalog-service/Dockerfile -t eventcart/catalog-service:local .
```

Build with the same retry wrapper used by CI:

```powershell
bash .github/scripts/docker-build-service.sh catalog-service eventcart/catalog-service:local
```

Override Java base images if a registry mirror or hardened internal image is required:

```powershell
$env:JAVA_BUILD_IMAGE = "eclipse-temurin:25-jdk"
$env:JAVA_RUNTIME_IMAGE = "eclipse-temurin:25-jre"
bash .github/scripts/docker-build-service.sh inventory-service eventcart/inventory-service:local
```

## Real-Time Monitoring

GitHub Actions monitoring is mostly workflow-based:

- Watch PR checks.
- Review logs per failed step.
- Track build duration.
- Track flaky test failures.
- Track image publish success.
- Add badges only when workflow is stable.

For a larger production setup, add:

- Code coverage reports.
- Static analysis.
- Dependency vulnerability scanning.
- Container image scanning.
- Deployment environments with approvals.
- Release notes generation.

## Interview Preparation

You should be able to explain:

- Difference between CI and CD.
- Why tests run before Docker publish.
- What a workflow, job, step, action, and runner are.
- Why matrix builds are useful for multiple services.
- How Maven cache improves speed.
- How secrets are injected into workflows.
- Why branch protection should require passing checks.
- Why image tags should be immutable for deployments.

Common interview questions:

| Question | Good Answer |
| --- | --- |
| What is CI? | Automatically building and testing every code change to catch issues early. |
| What is CD? | Automatically delivering or deploying tested artifacts to environments. |
| What is a runner? | A machine that executes workflow jobs. |
| Why use matrix builds? | To run the same job for many services or versions without duplicating YAML. |
| Why publish only after tests pass? | It prevents broken artifacts from entering the registry. |

## EventCart Takeaway

GitHub Actions turns EventCart from a local learning app into a repeatable build-and-package project. It teaches CI, test automation, Docker packaging, release tags, and production delivery discipline.
