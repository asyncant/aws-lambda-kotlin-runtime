package com.asyncant.aws.lambda.runtime

import com.asyncant.http.HttpResponse

/**
 * Just enough http/1.1 client to communicate with the AWS Lambda runtime API.
 */
class AwsLambdaRuntimeHttp11Client internal constructor(host: String) {
  private val ip: String = host.substringBefore(':')
  private val port: Int = host.substringAfter(':', "80").toInt()
  private var socket = TcpSocket(ip, port)

  fun get(path: String): HttpResponse {
    sendHttpMessage(
      "GET $path HTTP/1.1\r\n" +
        "Host: ${ip}:${port}\r\n" +
        "Accept: */*\r\n" +
        "Connection: keep-alive\r\n" +
        "\r\n"
    )
    return receiveHttpMessage()
  }

  fun post(path: String, payload: ByteArray, headers: Map<String, String>): HttpResponse {
    val allHeaders = "POST $path HTTP/1.1\r\n" +
      "Host: ${ip}:${port}\r\n" +
      "Accept: */*\r\n" +
      "Content-Length: ${payload.size}\r\n" +
      "Connection: keep-alive\r\n" +
      buildCustomHeaders(headers) +
      "\r\n"
    val data = allHeaders.encodeToByteArray().copyInto(ByteArray(allHeaders.length + payload.size))
    payload.copyInto(data, allHeaders.length)

    sendHttpMessage(data)

    return receiveHttpMessage()
  }

  fun close() = socket.close()

  private fun buildCustomHeaders(headers: Map<String, String>): String {
    if (headers.isEmpty()) return ""
    return buildString { headers.forEach { kv -> append("${kv.key}: ${kv.value}\r\n") } }
  }

  private fun sendHttpMessage(text: String) = sendHttpMessage(text.encodeToByteArray())

  private fun sendHttpMessage(data: ByteArray, retries: Int = 1) {
    try {
      socket.send(data)
    } catch (e: NotConnectedException) {
      if (retries > 0) {
        socket.close()
        socket = TcpSocket(ip, port)
        sendHttpMessage(data, retries - 1)
      } else throw e
    }
  }

  private fun receiveHttpMessage(): HttpResponse {
    val responseBuilder = HttpResponseBuilder()
    socket.receiveMessage(responseBuilder)
    check(responseBuilder.completed()) { "Incomplete http response received." }
    return responseBuilder.build()
  }
}
