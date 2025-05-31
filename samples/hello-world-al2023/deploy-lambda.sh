#!/usr/bin/env bash

set -eu

if [ $# -ne 1 ]; then
  echo "Usage: $0 <artifact-s3-bucket>"
  exit 1
fi

artifact_bucket=$1

script_dir="$(realpath "$(dirname "${BASH_SOURCE[0]}")")"

mkdir -p "${script_dir}/build/deploy"

"${script_dir}"/build-lambda.sh

aws cloudformation package \
  --template-file "${script_dir}/cloudformation-template.yaml" \
  --output-template-file "${script_dir}/build/deploy/template.yaml" \
  --s3-bucket "${artifact_bucket}"

aws cloudformation deploy \
  --template-file "${script_dir}/build/deploy/template.yaml" \
  --stack-name "kotlin-native-hello-world-al2023" \
  --capabilities CAPABILITY_IAM
