package com.asyncant.http

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class HttpResponseBuilderTest {
  private val response =
    "HTTP/1.1 200 OK\r\n" +
      "Content-Type: application/json\r\n" +
      "Lambda-Runtime-Aws-Request-Id: abc-123-def\r\n" +
      "Lambda-Runtime-Deadline-Ms: 123\r\n" +
      "Lambda-Runtime-Invoked-Function-Arn: arn:aws:lambda:eu-central-1:123:function:kotlin-native-hello-world\r\n" +
      "Lambda-Runtime-Trace-Id: Root=1-123-abc;Parent=456;Sampled=0\r\n" +
      "Date: Wed, 10 Aug 2021 20:00:00 GMT\r\n" +
      "Content-Length: 49\r\n" +
      "\r\n" +
      """{"key1":"value1","key2":"value2","key3":"value3"}"""

  private val expected = HttpResponse(
    """{"key1":"value1","key2":"value2","key3":"value3"}""".encodeToByteArray(),
    mapOf(
      "Content-Type" to listOf("application/json"),
      "Lambda-Runtime-Aws-Request-Id" to listOf("abc-123-def"),
      "Lambda-Runtime-Deadline-Ms" to listOf("123"),
      "Lambda-Runtime-Invoked-Function-Arn" to listOf("arn:aws:lambda:eu-central-1:123:function:kotlin-native-hello-world"),
      "Lambda-Runtime-Trace-Id" to listOf("Root=1-123-abc;Parent=456;Sampled=0"),
      "Date" to listOf("Wed, 10 Aug 2021 20:00:00 GMT"),
      "Content-Length" to listOf("49")
    ),
    200
  )

  @Test
  fun singlePacketParse() {
    val builder = HttpResponseBuilder()

    builder.append(response.encodeToByteArray(), response.length)

    assertTrue(builder.completed())
    assertEquals(expected, builder.build())
  }

  @Test
  fun multiplePacketParse() {
    val builder = HttpResponseBuilder()

    builder.append(response.encodeToByteArray().copyOf(32), 32)
    assertFalse(builder.completed())
    builder.append(response.encodeToByteArray().copyOfRange(32, 128), 96)
    assertFalse(builder.completed())
    builder.append(response.encodeToByteArray().copyOfRange(128, response.length), response.length - 128)

    assertTrue(builder.completed())
    assertEquals(expected, builder.build())
  }

  @Test
  fun largerBody() {
    val builder = HttpResponseBuilder()
    val body = "#".repeat(99999)

    val newResponse = response.replace("Content-Length:.*\r\n".toRegex(), "Content-Length: ${body.length}\r\n")
      .replaceAfter("\r\n\r\n", body)

    // When the first 32 bytes are received
    builder.append(newResponse.encodeToByteArray().copyOf(32), 32)
    // then the response is not complete yet.
    assertFalse(builder.completed())

    // When the rest of the headers and part of the body is received
    val half = 128 + newResponse.length / 2
    builder.append(newResponse.encodeToByteArray().copyOfRange(32, half), half - 32)
    // then the response is still not complete.
    assertFalse(builder.completed())

    // When the remainder is received
    builder.append(newResponse.encodeToByteArray().copyOfRange(half, newResponse.length), newResponse.length - half)
    // then the response is complete
    assertTrue(builder.completed())
    // and a filled in HttpResponse is returned.
    val expectedHeaders = expected.headers + ("Content-Length" to listOf(body.length.toString()))
    assertEquals(expected.copy(body.encodeToByteArray(), expectedHeaders), builder.build())
  }
}