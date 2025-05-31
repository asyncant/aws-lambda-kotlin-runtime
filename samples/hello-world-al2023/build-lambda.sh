#!/usr/bin/env bash

set -eu

script_dir="$(realpath "$(dirname "${BASH_SOURCE[0]}")")"

"${script_dir}"/../../gradlew build

mkdir -p "${script_dir}/build/lambda"
rm "${script_dir}/build/lambda/lambda.zip" || true

cp "${script_dir}/build/bin/linuxX64/releaseExecutable/hello-world-al2023.kexe" "${script_dir}/build/lambda/bootstrap"

# Remove Kotlin's unused dependency on libcrypt as it is not present on Amazon Linux 2023.
# * https://youtrack.jetbrains.com/issue/KT-55643
patchelf --remove-needed libcrypt.so.1 "${script_dir}/build/lambda/bootstrap"

# Alternatively, one can copy the required library from Kotlin's konan distribution:
# cp "${HOME}/.konan/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/sysroot/lib/libcrypt-2.19.so" "${script_dir}/build/lambda/libcrypt.so.1"
# and add it to the zip:
# (cd "${script_dir}/build/lambda" && zip -qoX lambda.zip bootstrap libcrypt.so.1)

# Create reproducible zip.
touch -t 200001010000 "${script_dir}/build/lambda/bootstrap"
(cd "${script_dir}/build/lambda" && zip -qoX lambda.zip bootstrap)
