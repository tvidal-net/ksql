package uk.tvidal.data.codec

import uk.tvidal.data.logging.KLogging
import uk.tvidal.data.simpleName
import java.sql.ResultSet
import kotlin.reflect.KCallable
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter

@Suppress("UNCHECKED_CAST")
fun interface EntityDecoder<E> {

  operator fun invoke(rs: ResultSet): E?

  class ByConstructor<E>(
    val constructor: KCallable<E>,
    val parameterDecoders: Collection<ParameterDecoder<*>>,
    val overrides: Map<String, Any?> = mapOf(),
  ) : EntityDecoder<E> {
    override fun invoke(rs: ResultSet): E? = constructor.callBy(
      parameterDecoders.associate {
        it.parameter to it.parameter.name.let { name ->
          if (name in overrides) overrides[name]
          else it.readValue(rs)
        }
      }
    )

    override fun toString() = "$simpleName(${constructor.description})"
  }

  class ParameterDecoder<T>(
    val parameter: KParameter,
    val decode: EntityDecoder<T>,
  ) {
    fun readValue(rs: ResultSet): T? = decode(rs)
    override fun toString() = "$simpleName($decode, ${parameter.description})"
  }

  class ByProperties<E>(
    val upstreamDecoder: EntityDecoder<E>,
    val propertyDecoders: Collection<PropertyDecoder<E, *>>,
  ) : EntityDecoder<E> {
    override fun invoke(rs: ResultSet): E? = upstreamDecoder(rs)?.also {
      propertyDecoders.forEach { decoder ->
        decoder.setValue(rs, it)
      }
    }

    override fun toString() = "$simpleName$propertyDecoders"
  }

  class PropertyDecoder<in E, T>(
    val property: KMutableProperty1<in E, T?>,
    val decode: EntityDecoder<T>,
  ) {
    fun setValue(rs: ResultSet, receiver: E) {
      val value: T? = decode(rs)
      property.set(receiver, value)
    }

    override fun toString() = "$simpleName(${property.description})"
  }

  class FieldDecoder<T>(val decoder: ResultSetDecoder<T>, val fieldName: String) : EntityDecoder<T> {
    override fun invoke(rs: ResultSet): T? = decoder.getResultSetValue(rs, fieldName)
    override fun toString() = "$simpleName($fieldName=$decoder)"
  }

  companion object : KLogging()
}
