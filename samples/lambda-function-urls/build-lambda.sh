#!/usr/bin/env bash

set -eu

script_dir="$(realpath "$(dirname "${BASH_SOURCE[0]}")")"

"${script_dir}"/../../gradlew build

mkdir -p "${script_dir}/build/lambda"
cp "${script_dir}/build/bin/native/releaseExecutable/lambda-function-urls.kexe" "${script_dir}/build/lambda/bootstrap"
# Create reproducible zip.
touch -t 200001010000 "${script_dir}/build/lambda/bootstrap"
(cd "${script_dir}/build/lambda" && zip -qoX lambda.zip bootstrap)

