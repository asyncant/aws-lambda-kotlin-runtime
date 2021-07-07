package com.asyncant.platform

/** Throws IllegalArgumentException if [name] is not found in the environment variables, returns the value otherwise. */
expect fun requireEnv(name: String): String
