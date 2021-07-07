package com.asyncant.platform

import kotlinx.cinterop.toKString
import platform.posix.getenv

actual fun requireEnv(name: String) = requireNotNull(getenv(name)?.toKString()) {
  "Missing environment variable '$name'."
}
