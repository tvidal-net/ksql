package uk.tvidal.data.codec

import kotlin.reflect.KClass

@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.PROPERTY,
  AnnotationTarget.VALUE_PARAMETER,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class DecodeWith<out T>(val value: KClass<out ResultSetDecoder<T>>)
