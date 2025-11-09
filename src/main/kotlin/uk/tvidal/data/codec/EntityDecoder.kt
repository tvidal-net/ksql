package uk.tvidal.data.codec

import java.sql.ResultSet
import kotlin.reflect.KCallable
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter

@Suppress("UNCHECKED_CAST")
interface EntityDecoder<out E> {

  operator fun invoke(rs: ResultSet): E

  class ByConstructor<E>(
    val constructor: KCallable<E>,
    val parameterDecoders: Collection<ParameterDecoder<*>>,
    val overrides: Map<String, Any?> = mapOf(),
  ) : EntityDecoder<E> {
    override fun invoke(rs: ResultSet): E = constructor.callBy(
      parameterDecoders.associate {
        it.parameter to it.parameter.name.let { name ->
          if (name in overrides) overrides[name]
          else it.readValue(rs)
        }
      }
    )
  }

  class ParameterDecoder<out T>(
    val parameter: KParameter,
    val decode: FieldDecoder<T>,
  ) {
    fun readValue(rs: ResultSet): T? =
      decode(rs)
  }

  class ByProperties<E>(
    val constructor: (ResultSet) -> E,
    val propertyDecoders: Collection<PropertyDecoder<E, *>>,
  ) : EntityDecoder<E> {
    override fun invoke(rs: ResultSet): E = constructor(rs).also { entity ->
      propertyDecoders.forEach {
        it.setValue(rs, entity)
      }
    }
  }

  class PropertyDecoder<in E, T>(
    val property: KMutableProperty1<in E, T?>,
    val decode: FieldDecoder<T>,
  ) {
    fun setValue(rs: ResultSet, receiver: E) {
      val value = decode(rs)
      property.set(receiver, value)
    }
  }
}
