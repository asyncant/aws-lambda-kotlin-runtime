package com.asyncant.json

/** Encodes a [String] into a json string with escaping for special characters. */
internal fun toJsonString(text: String): String {
  val result = StringBuilder()
  result.append("\"")
  for (c in text) {
    when (c.code) {
      10 -> result.append("\\n")
      in 0..31 -> result.append("\\u00${c.code.toString(16).padStart(2, '0')}")
      34 -> result.append("\\\"")
      92 -> result.append("\\\\")
      else -> result.append(c)
    }
  }
  result.append("\"")
  return result.toString()
}
