#!/usr/bin/env bash

set -eu

script_dir="$(realpath "$(dirname "${BASH_SOURCE[0]}")")"
repo_root="$(realpath "${script_dir}/../..")"
docker_image="${GRAALVM_DOCKER_IMAGE:-ghcr.io/graalvm/native-image-community:21}"
docker_platform="${GRAALVM_DOCKER_PLATFORM:-linux/amd64}"
container_gradle_home="/workspace/samples/hello-world-graalvm/build/gradle-user-home"

mkdir -p "${script_dir}/build/gradle-user-home"

if command -v podman > /dev/null; then
  container_command=podman
  container_user=""
else
  container_command=docker
  container_user="--user=$(id -u):$(id -g)"
fi

# shellcheck disable=SC2086
"${container_command}" run --rm \
  --platform "${docker_platform}" \
  ${container_user} \
  --volume "${repo_root}:/workspace" \
  --workdir /workspace \
  --env GRADLE_USER_HOME="${container_gradle_home}" \
  --env HOME=/tmp \
  --entrypoint /bin/bash \
  "${docker_image}" \
  -c "./gradlew --no-daemon :samples:hello-world-graalvm:nativeCompile"

mkdir -p "${script_dir}/build/lambda"
cp "${script_dir}/build/native/nativeCompile/bootstrap" "${script_dir}/build/lambda/bootstrap"
# Create reproducible zip.
touch -t 200001010000 "${script_dir}/build/lambda/bootstrap"
(cd "${script_dir}/build/lambda" && zip -qoX lambda.zip bootstrap)
