package com.asyncant.http

import com.asyncant.aws.lambda.runtime.curl.CURLE_OK
import com.asyncant.aws.lambda.runtime.curl.CURLINFO_DATA_IN
import com.asyncant.aws.lambda.runtime.curl.CURLINFO_DATA_OUT
import com.asyncant.aws.lambda.runtime.curl.CURLINFO_HEADER_IN
import com.asyncant.aws.lambda.runtime.curl.CURLINFO_HEADER_OUT
import com.asyncant.aws.lambda.runtime.curl.CURLINFO_RESPONSE_CODE
import com.asyncant.aws.lambda.runtime.curl.CURLINFO_SSL_DATA_IN
import com.asyncant.aws.lambda.runtime.curl.CURLINFO_SSL_DATA_OUT
import com.asyncant.aws.lambda.runtime.curl.CURLINFO_TEXT
import com.asyncant.aws.lambda.runtime.curl.CURLOPT_DEBUGFUNCTION
import com.asyncant.aws.lambda.runtime.curl.CURLOPT_HEADERDATA
import com.asyncant.aws.lambda.runtime.curl.CURLOPT_HEADERFUNCTION
import com.asyncant.aws.lambda.runtime.curl.CURLOPT_HTTPGET
import com.asyncant.aws.lambda.runtime.curl.CURLOPT_HTTPHEADER
import com.asyncant.aws.lambda.runtime.curl.CURLOPT_POST
import com.asyncant.aws.lambda.runtime.curl.CURLOPT_POSTFIELDS
import com.asyncant.aws.lambda.runtime.curl.CURLOPT_POSTFIELDSIZE
import com.asyncant.aws.lambda.runtime.curl.CURLOPT_URL
import com.asyncant.aws.lambda.runtime.curl.CURLOPT_VERBOSE
import com.asyncant.aws.lambda.runtime.curl.CURLOPT_WRITEDATA
import com.asyncant.aws.lambda.runtime.curl.CURLOPT_WRITEFUNCTION
import com.asyncant.aws.lambda.runtime.curl.curl_easy_cleanup
import com.asyncant.aws.lambda.runtime.curl.curl_easy_getinfo
import com.asyncant.aws.lambda.runtime.curl.curl_easy_init
import com.asyncant.aws.lambda.runtime.curl.curl_easy_perform
import com.asyncant.aws.lambda.runtime.curl.curl_easy_setopt
import com.asyncant.aws.lambda.runtime.curl.curl_easy_strerror
import com.asyncant.aws.lambda.runtime.curl.curl_infotype
import com.asyncant.aws.lambda.runtime.curl.curl_slist
import com.asyncant.aws.lambda.runtime.curl.curl_slist_append
import com.asyncant.aws.lambda.runtime.curl.curl_slist_free_all
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.DeferScope
import kotlinx.cinterop.LongVar
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pin
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.posix.gettimeofday
import platform.posix.size_t
import platform.posix.timeval
import kotlin.collections.set


actual class BlockingHttpClient {
  private val curl = checkNotNull(curl_easy_init()) { "Curl initialization failed." }

  init {
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, staticCFunction(::bodyCallback))
    curl_easy_setopt(curl, CURLOPT_HEADERFUNCTION, staticCFunction(::headerCallback))
  }

  actual fun get(uri: String, headers: Map<String, String>): HttpResponse = memScoped {
    val responseBuilder = ResponseBuilder()
    val responseBuilderRef = stableRef(responseBuilder)
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, responseBuilderRef.asCPointer())
    curl_easy_setopt(curl, CURLOPT_HEADERDATA, responseBuilderRef.asCPointer())
    curl_easy_setopt(curl, CURLOPT_URL, uri)
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, toCurlHeaders(headers))

    curl_easy_setopt(curl, CURLOPT_POSTFIELDSIZE, 0)
    curl_easy_setopt(curl, CURLOPT_POSTFIELDS, 0)
    curl_easy_setopt(curl, CURLOPT_HTTPGET, 1L)

    val result = curl_easy_perform(curl)
    if (result != CURLE_OK) throw CurlInvocationException(curl_easy_strerror(result)?.toKString())

    val statusCode = alloc<LongVar>()
    curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, statusCode.ptr)

    HttpResponse(responseBuilder.body(), responseBuilder.headers(), statusCode.value.toInt())
  }

  actual fun post(uri: String, payload: ByteArray, headers: Map<String, String>): HttpResponse = memScoped {
    val responseBuilder = ResponseBuilder()
    val responseBuilderRef = stableRef(responseBuilder)
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, responseBuilderRef.asCPointer())
    curl_easy_setopt(curl, CURLOPT_HEADERDATA, responseBuilderRef.asCPointer())
    curl_easy_setopt(curl, CURLOPT_URL, uri)
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, toCurlHeaders(headers))

    curl_easy_setopt(curl, CURLOPT_POSTFIELDSIZE, payload.size)
    curl_easy_setopt(curl, CURLOPT_POSTFIELDS, pinnedAddress(payload))
    curl_easy_setopt(curl, CURLOPT_POST, 1L)

    val result = curl_easy_perform(curl)
    if (result != CURLE_OK) throw CurlInvocationException(curl_easy_strerror(result)?.toKString())

    val statusCode = alloc<LongVar>()
    curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, statusCode.ptr)

    HttpResponse(responseBuilder.body(), responseBuilder.headers(), statusCode.value.toInt())
  }

  actual fun close() {
    curl_easy_cleanup(curl)
  }

  @Suppress("unused")
  fun enableDebugLogging() {
    curl_easy_setopt(curl, CURLOPT_DEBUGFUNCTION, staticCFunction(::debugCallback))
    curl_easy_setopt(curl, CURLOPT_VERBOSE, 1)
  }
}

class CurlInvocationException(curlErrorMessage: String?) : RuntimeException(curlErrorMessage)

private fun headerCallback(
  buffer: CPointer<ByteVar>?,
  size: size_t,
  nitems: size_t,
  userdata: COpaquePointer?
): size_t {
  if (buffer == null) return 0u
  if (userdata == null) return 0u

  val length = size * nitems
  if (length == 0uL) return 0u

  val headerLine = buffer.toKString(length.toInt()).trim()
  if (headerLine.isEmpty()) return length

  val responseBuilder = userdata.asStableRef<ResponseBuilder>().get()
  if (responseBuilder.setStatusLineReceived()) return length

  val headerName = headerLine.substringBefore(':')
  val headerValue = headerLine.substringAfter(':').trim()
  responseBuilder.appendHeader(headerName, headerValue)
  return length
}

private fun bodyCallback(buffer: CPointer<ByteVar>?, size: size_t, nitems: size_t, userdata: COpaquePointer?): size_t {
  if (buffer == null) return 0u
  if (userdata == null) return 0u
  val responseBuilder = userdata.asStableRef<ResponseBuilder>()
  responseBuilder.get().appendBody(buffer, (size * nitems).toInt())
  return size * nitems
}

private fun DeferScope.toCurlHeaders(headers: Map<String, String>): CPointer<curl_slist>? {
  var curlHeaders: CPointer<curl_slist>? = null
  for (header in headers) curlHeaders = curl_slist_append(curlHeaders, "${header.key}: ${header.value}")
  defer { curl_slist_free_all(curlHeaders) }
  return curlHeaders
}

private fun <T : Any> DeferScope.stableRef(any: T): StableRef<T> {
  val ref = StableRef.create(any)
  defer { ref.dispose() }
  return ref
}

private fun DeferScope.pinnedAddress(arr: ByteArray): CPointer<ByteVar> {
  val ref = arr.pin()
  defer { ref.unpin() }
  return ref.addressOf(0)
}

private fun CPointer<ByteVar>.toKString(length: Int): String {
  val bytes = this.readBytes(length)
  return bytes.decodeToString(0, length)
}

private class ResponseBuilder {
  private var contentReceived: Int = 0
  private var body: ByteArray = byteArrayOf()
  private val headers = mutableMapOf<String, List<String>>()
  private var statusLineReceived = false

  fun body(): ByteArray = if (body.size == contentReceived) body else body.copyOf(contentReceived)
  fun headers(): Map<String, List<String>> = headers

  fun appendBody(part: CPointer<ByteVar>, size: Int) {
    if (contentReceived + size > body.size) body = body.copyOf(body.size + size * 2)
    part.readBytes(size).copyInto(body, contentReceived)
    contentReceived += size
  }

  fun appendHeader(name: String, value: String) {
    val existingValues = headers[name] ?: emptyList()
    if ("content-length".equals(name, ignoreCase = true)) body = ByteArray(value.toInt())
    headers[name] = existingValues + value
  }

  fun setStatusLineReceived(): Boolean {
    if (statusLineReceived) return false
    statusLineReceived = true
    return true
  }
}

@Suppress("unused_parameter")
private fun debugCallback(
  curlHandle: COpaquePointer?,
  type: curl_infotype,
  buffer: CPointer<ByteVar>?,
  size: size_t,
  userdata: COpaquePointer?
): size_t {
  when (type) {
    CURLINFO_TEXT -> printlnMs("== Info: ${buffer?.toKString(size.toInt())}")
    CURLINFO_HEADER_OUT -> printlnMs("=> Send header")
    CURLINFO_DATA_OUT -> printlnMs("=> Send data")
    CURLINFO_SSL_DATA_OUT -> printlnMs("=> Send SSL data")
    CURLINFO_HEADER_IN -> printlnMs("<= Recv header")
    CURLINFO_DATA_IN -> printlnMs("<= Recv data")
    CURLINFO_SSL_DATA_IN -> printlnMs("<= recv SSL data")
  }
  return 0u
}

private fun printlnMs(message: String) = println("${getSystemTimeMillis()}: $message")

private fun getSystemTimeMillis(): Long = memScoped {
  val tv = alloc<timeval>()
  gettimeofday(tv.ptr, null)
  (tv.tv_sec * 1000) + (tv.tv_usec / 1000)
}
