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
  --stack-name "kotlin-native-lambda-function-urls-cloudfront" \
  --capabilities CAPABILITY_IAM

url=$(aws cloudformation describe-stacks --stack "kotlin-native-lambda-function-urls-cloudfront" --query "Stacks[0].Outputs[?OutputKey=='lambdaUrl'].OutputValue" --output text)
echo "###################################################################"
echo "Lambda can be invoked through: ${url}"
cloudFrontUrl=$(aws cloudformation describe-stacks --stack "kotlin-native-lambda-function-urls-cloudfront" --query "Stacks[0].Outputs[?OutputKey=='cloudFrontUrl'].OutputValue" --output text)
echo "###################################################################"
echo "CloudFront can be invoked through: https://${cloudFrontUrl}"
