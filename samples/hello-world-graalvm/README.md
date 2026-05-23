## GraalVM sample

JVM + GraalVM example.
It uses the runtime's JVM target and compiles it to a native `bootstrap` executable with the GraalVM Native Build Tools Gradle plugin.

To build the Lambda artifact, install Docker, then run:

```bash
./samples/hello-world-graalvm/build-lambda.sh
```

The script runs `nativeCompile` inside `ghcr.io/graalvm/native-image-community:21` by default and produces `samples/hello-world-graalvm/build/lambda/lambda.zip`, ready to deploy as a `provided.al2023` custom runtime. You can override the container image or target platform with `GRAALVM_DOCKER_IMAGE` and `GRAALVM_DOCKER_PLATFORM`.

To package and deploy it with CloudFormation, run:

```bash
./samples/hello-world-graalvm/deploy-lambda.sh <artifact-s3-bucket>
```