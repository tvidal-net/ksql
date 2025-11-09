package uk.tvidal.data.codec

import uk.tvidal.data.Config
import uk.tvidal.data.NamingStrategy
import uk.tvidal.data.fieldName
import uk.tvidal.data.logging.KLogging
import uk.tvidal.data.valueType
import java.sql.ResultSet
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

@Suppress("UNCHECKED_CAST")
class CodecFactory(
  val config: Config,
  val databaseName: NamingStrategy,
) {

  fun <T : Any> encoder(property: KProperty<T>): ParamValueEncoder<T> = dataType(property)

  fun <T : Any> decoder(
    entity: KClass<T>,
    alias: String? = null,
  ): EntityDecoder<T> = byProperties(
    alias = alias,
    properties = entity.memberProperties
      .filterIsInstance<KMutableProperty1<T, Any>>(),
    constructor = byConstructor(
      alias = alias,
      constructor = requireNotNull(entity.primaryConstructor) {
        "Entity class $entity has no primary constructor"
      }
    )::invoke
  )

  fun dataType(parameter: KParameter): DataType<*, Any> = parameter.run {
    config.dataType(valueType, findAnnotation()) as DataType<*, Any>
  }

  fun <T : Any> dataType(property: KProperty<T>): DataType<*, T> = property.run {
    config.dataType(valueType, findAnnotation()) as DataType<*, T>
  }

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
        dataType = dataType(it).debug { dataType ->
          "dataType ${it.name}=$dataType"
        },
      )
    }
  )

  fun <T : Any> forParameter(
    parameter: KParameter,
    dataType: DataType<*, T>,
    alias: String? = null,
  ) = EntityDecoder.ParameterDecoder(
    parameter,
    decode = decodeWith(
      decoder = dataType,
      field = databaseName[parameter.fieldName, alias]
    )
  )

  fun <E> byProperties(
    constructor: (ResultSet) -> E,
    properties: Collection<KMutableProperty1<in E, Any>>,
    alias: String? = null,
  ) = EntityDecoder.ByProperties(
    constructor,
    propertyDecoders = properties.map {
      forProperty(
        property = it as KMutableProperty1<in E, Any?>,
        alias = alias,
        dataType = dataType(it).debug { dataType ->
          "dataType $it=$dataType"
        },
      )
    }
  )

  fun <E, T : Any> forProperty(
    property: KMutableProperty1<in E, T?>,
    dataType: DataType<*, T>,
    alias: String? = null,
  ) = EntityDecoder.PropertyDecoder(
    property,
    decode = decodeWith(
      decoder = dataType,
      field = databaseName[property.fieldName, alias]
    )
  )

  fun <T : Any> decodeWith(decoder: ResultSetDecoder<T>, field: String): FieldDecoder<T> =
    { decoder.getResultSetValue(it, field) }

  companion object : KLogging()
}
