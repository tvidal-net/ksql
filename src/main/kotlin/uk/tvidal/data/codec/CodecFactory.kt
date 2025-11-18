package uk.tvidal.data.codec

import uk.tvidal.data.Config
import uk.tvidal.data.NamingStrategy
import uk.tvidal.data.fieldName
import uk.tvidal.data.fields
import uk.tvidal.data.keyField
import uk.tvidal.data.logging.KLogging
import uk.tvidal.data.returnValueType
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.primaryConstructor

@Suppress("UNCHECKED_CAST")
class CodecFactory(
  val config: Config = Config.Default,
) {

  val databaseName: NamingStrategy
    get() = config.namingStrategy

  fun <T : Any> encoder(property: KProperty<T>): ParamValueEncoder<T> = requireNotNull(
    config.fieldType(property) ?: entityEncoder(property.returnValueType)
  ) {
    "Unable to find a suitable encoder for $property"
  }

  private fun <E : Any> entityEncoder(table: KClass<E>) = table.keyField?.let { keyField ->
    config.fieldType(keyField)?.let { encoder ->
      ParamValueEncoder<E> { st, index, value ->
        encoder.setParamValue(st, index, value?.let { keyField(it) })
      }
    }
  }

  fun <E : Any> decoder(table: KClass<E>, alias: String?): EntityDecoder<E> {
    val constructor = requireNotNull(
      table.primaryConstructor ?: table.noArgsConstructor
    ) {
      "Unable to find a suitable constructor for $table"
    }
    val parameters = constructor.parameters
    val parameterNames = parameters
      .map { it.name }
      .toSet()

    val properties = table.fields
      .filterIsInstance<KMutableProperty1<Any, Any>>()
      .filterNot { it.name in parameterNames }

    val upstreamDecoder: EntityDecoder<E> = if (parameters.isNotEmpty()) {
      EntityDecoder.ByConstructor(
        constructor = constructor.debug {
          "byConstructor ${constructor.description}${asAlias(alias)}"
        },
        parameterDecoders = parameters.map {
          EntityDecoder.ParameterDecoder(
            parameter = it,
            decode = paramDecoder(it, alias)
          )
        }
      )
    } else {
      EntityDecoder { constructor.call() }
    }

    return if (properties.isNotEmpty()) {
      EntityDecoder.ByProperties(
        upstreamDecoder = upstreamDecoder.debug {
          val propertiesDescription = properties.joinToString { it.description }
          "byProperties ($propertiesDescription)${asAlias(alias)}"
        },
        propertyDecoders = properties.map {
          EntityDecoder.PropertyDecoder(
            property = it as KMutableProperty1<in E, Any?>,
            decode = propertyDecoder(it, alias)
          )
        }
      )
    } else {
      upstreamDecoder
    }
  }

  private fun <T : Any> paramDecoder(parameter: KParameter, alias: String?): EntityDecoder<T> =
    (config.paramType(parameter) as? ResultSetDecoder<T>)
      ?.let { fieldDecoder(it, parameter.fieldName, alias) }
      ?: decoder(parameter.returnValueType as KClass<T>, parameter.fieldName)

  private fun <T : Any> propertyDecoder(property: KProperty<T?>, alias: String?): EntityDecoder<T> =
    config.fieldType(property)
      ?.let { fieldDecoder(it, property.fieldName, alias) }
      ?: decoder(property.returnValueType, property.fieldName)

  private fun <T : Any> fieldDecoder(decoder: ResultSetDecoder<T>, fieldName: String, alias: String?): EntityDecoder<T> =
    EntityDecoder.FieldDecoder(decoder, databaseName[fieldName, alias])

  private data class CacheKey(
    val table: KClass<*>,
    val alias: String? = null
  )

  companion object : KLogging()
}
