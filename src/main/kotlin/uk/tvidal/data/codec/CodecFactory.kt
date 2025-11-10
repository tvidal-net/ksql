package uk.tvidal.data.codec

import uk.tvidal.data.Config
import uk.tvidal.data.NamingStrategy
import uk.tvidal.data.fieldName
import uk.tvidal.data.logging.KLogging
import java.sql.ResultSet
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

@Suppress("UNCHECKED_CAST")
class CodecFactory(
  val config: Config = Config.Default,
) {
  val databaseName: NamingStrategy
    get() = config.namingStrategy

  fun <T : Any> encoder(property: KProperty<T>): ParamValueEncoder<T> =
    config.fieldType(property) as ValueType<*, T>

  fun <T : Any> decoder(
    entity: KClass<T>,
    alias: String? = null,
  ): EntityDecoder<T> = byProperties(
    alias = alias,
    properties = entity.memberProperties
      .filterIsInstance<KMutableProperty1<T, Any?>>(),
    constructor = byConstructor(
      alias = alias,
      constructor = requireNotNull(entity.primaryConstructor) {
        "Entity class $entity has no primary constructor"
      }
    )::invoke
  )

  fun <E> byConstructor(
    constructor: KCallable<E>,
    alias: String? = null,
  ) = EntityDecoder.ByConstructor(
    constructor.debug {
      "byConstructor ${it.name}(\n\t${it.parameters.joinToString("\n\t")}\n): ${it.returnType}"
    },
    parameterDecoders = constructor.parameters.map {
      forParameter(
        parameter = it,
        alias = alias,
        type = (config.paramType(it) as ValueType<*, Any>).debug { type ->
          "paramType ${it.name}=$type"
        },
      )
    }
  )

  fun <T : Any> forParameter(
    parameter: KParameter,
    type: ValueType<*, T>,
    alias: String? = null,
  ) = EntityDecoder.ParameterDecoder(
    parameter,
    decode = decodeWith(
      decoder = type,
      name = databaseName[parameter.fieldName, alias]
    )
  )

  fun <E> byProperties(
    constructor: (ResultSet) -> E,
    properties: Collection<KMutableProperty1<in E, Any?>>,
    alias: String? = null,
  ) = EntityDecoder.ByProperties(
    constructor,
    propertyDecoders = properties.map {
      forProperty(
        property = it,
        alias = alias,
        resultSetDecoder = config.fieldType(it).debug { dataType ->
          "fieldType $it=$dataType"
        } as ValueType<*, Any>,
      )
    }
  )

  fun <E, T : Any> forProperty(
    property: KMutableProperty1<in E, T?>,
    resultSetDecoder: ResultSetDecoder<T>,
    alias: String? = null,
  ) = EntityDecoder.PropertyDecoder(
    property,
    decode = decodeWith(
      decoder = resultSetDecoder,
      name = databaseName[property.fieldName, alias]
    )
  )

  fun <T : Any> decodeWith(decoder: ResultSetDecoder<T>, name: String): FieldDecoder<T> =
    { decoder.getResultSetValue(it, name) }

  companion object : KLogging()
}
