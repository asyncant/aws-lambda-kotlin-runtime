package com.asyncant.json

import kotlin.test.Test
import kotlin.test.assertEquals

class JsonEncodingTest {
  @Test
  fun encodeToJsonStringTest() {
    val input = "Hi 😀\nfoo\"bar\\baz"
    assertEquals("\"Hi 😀\\nfoo\\\"bar\\\\baz\"", toJsonString(input))
  }
}
