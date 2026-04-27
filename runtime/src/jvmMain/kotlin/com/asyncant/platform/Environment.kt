package com.asyncant.platform

actual fun requireEnv(name: String): String = requireNotNull(System.getenv(name)) {
  "Missing environment variable '$name'."
}
