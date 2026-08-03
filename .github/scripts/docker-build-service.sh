#!/usr/bin/env bash
set -euo pipefail

service="${1:?Usage: docker-build-service.sh <service-name> <image-tag>}"
image_tag="${2:?Usage: docker-build-service.sh <service-name> <image-tag>}"

dockerfile="services/${service}/Dockerfile"

if [[ ! -f "${dockerfile}" ]]; then
  echo "Dockerfile not found: ${dockerfile}" >&2
  exit 1
fi

java_build_image="${JAVA_BUILD_IMAGE:-eclipse-temurin:21-jdk}"
java_runtime_image="${JAVA_RUNTIME_IMAGE:-eclipse-temurin:21-jre}"
pull_attempts="${DOCKER_PULL_ATTEMPTS:-4}"
build_attempts="${DOCKER_BUILD_ATTEMPTS:-3}"

retry_sleep_seconds() {
  local attempt="$1"
  echo $((attempt * 20))
}

pull_with_retry() {
  local base_image="$1"

  for attempt in $(seq 1 "${pull_attempts}"); do
    echo "Pulling base image ${base_image} (attempt ${attempt}/${pull_attempts})"
    if docker pull "${base_image}"; then
      return 0
    fi

    if [[ "${attempt}" -lt "${pull_attempts}" ]]; then
      local sleep_seconds
      sleep_seconds="$(retry_sleep_seconds "${attempt}")"
      echo "Docker pull failed for ${base_image}; retrying in ${sleep_seconds}s"
      sleep "${sleep_seconds}"
    fi
  done

  echo "Docker pull failed after ${pull_attempts} attempts: ${base_image}" >&2
  return 1
}

build_with_retry() {
  for attempt in $(seq 1 "${build_attempts}"); do
    echo "Building ${image_tag} from ${dockerfile} (attempt ${attempt}/${build_attempts})"
    if docker build \
      --build-arg "JAVA_BUILD_IMAGE=${java_build_image}" \
      --build-arg "JAVA_RUNTIME_IMAGE=${java_runtime_image}" \
      -f "${dockerfile}" \
      -t "${image_tag}" \
      .; then
      return 0
    fi

    if [[ "${attempt}" -lt "${build_attempts}" ]]; then
      local sleep_seconds
      sleep_seconds="$(retry_sleep_seconds "${attempt}")"
      echo "Docker build failed for ${service}; retrying in ${sleep_seconds}s"
      sleep "${sleep_seconds}"
    fi
  done

  echo "Docker build failed after ${build_attempts} attempts: ${service}" >&2
  return 1
}

pull_with_retry "${java_build_image}"
pull_with_retry "${java_runtime_image}"
build_with_retry
