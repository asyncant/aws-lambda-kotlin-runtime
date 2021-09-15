package com.asyncant.aws.lambda.runtime

internal interface MessageReceiver {
  fun append(data: ByteArray, size: Int)
  fun completed(): Boolean
}