#!/usr/bin/env bash

set -eu

script_dir="$(realpath "$(dirname "${BASH_SOURCE[0]}")")"

"${script_dir}"/../../gradlew build

mkdir -p "${script_dir}/build/lambda"
rm "${script_dir}/build/lambda/lambda.zip" > /dev/null 2>&1 || true

cp "${script_dir}/build/bin/linuxArm64/releaseExecutable/hello-world-arm64.kexe" "${script_dir}/build/lambda/bootstrap"

# Create reproducible zip.
touch -t 200001010000 "${script_dir}/build/lambda/bootstrap"
(cd "${script_dir}/build/lambda" && zip -qoX lambda.zip bootstrap)
