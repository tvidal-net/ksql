package uk.tvidal.data.codec

import uk.tvidal.data.Config
import uk.tvidal.data.NamingStrategy
import uk.tvidal.data.keyField
import uk.tvidal.data.logging.KLogging
import uk.tvidal.data.returnValueType
import java.sql.ResultSet
import java.util.concurrent.ConcurrentHashMap
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
  private val cache = ConcurrentHashMap<CacheKey, EntityDecoder<*>>()

  val databaseName: NamingStrategy
    get() = config.namingStrategy

  fun <T : Any> encoder(property: KProperty<T>): ParamValueEncoder<T> =
    config.fieldType(property) as ValueType<*, T>

  fun <E : Any> decoder(
    entity: KClass<E>,
    alias: String? = null,
  ): EntityDecoder<E> = cache.computeIfAbsent(
    CacheKey(entity, alias),
    { createDecoder(it) }
  ) as EntityDecoder<E>

  private fun createDecoder(key: CacheKey) = byProperties(
    alias = key.alias,
    properties = key.table.memberProperties
      .filterIsInstance<KMutableProperty1<Any, Any?>>(),
    constructor = byConstructor(
      alias = key.alias,
      constructor = requireNotNull(key.table.primaryConstructor) {
        "Entity class ${key.table} has no primary constructor"
      }
    )::invoke
  )

  fun <E> byConstructor(
    constructor: KCallable<E>,
    alias: String? = null,
  ) = EntityDecoder.ByConstructor(
    constructor.debug {
      val logAlias = alias?.let { " AS $it" } ?: ""
      "byConstructor ${constructor.logMessage}$logAlias"
    },
    parameterDecoders = constructor.parameters.map {
      EntityDecoder.ParameterDecoder(
        parameter = it,
        decode = paramDecoder(it, alias)
      )
    }
  )

  fun paramDecoder(parameter: KParameter, alias: String? = null) = parameter.returnValueType.keyField?.let {
    decoder(parameter.returnValueType, parameter.name)
  } ?: fieldDecoder(
    name = databaseName[parameter.fieldName, alias],
    decoder = requireNotNull(config.paramType(parameter)) {
      "Unable to find a suitable ValueType for $parameter"
    }
  )

  fun <E> byProperties(
    constructor: (ResultSet) -> E?,
    properties: Collection<KMutableProperty1<in E, Any?>>,
    alias: String? = null,
  ) = EntityDecoder.ByProperties(
    constructor.debug {
      val logAlias = alias?.let { " AS $it" } ?: ""
      val logMessage = properties.joinToString { it.name }
      "byProperties ($logMessage)$logAlias"
    },
    propertyDecoders = properties.map {
      EntityDecoder.PropertyDecoder(
        property = it,
        decode = propertyDecoder(it, alias)
      )
    }
  )

  fun propertyDecoder(property: KProperty<*>, alias: String? = null) = property.returnValueType.keyField?.let {
    decoder(property.returnValueType, property.name)
  } ?: fieldDecoder(
    name = databaseName[property.name, alias],
    decoder = requireNotNull(config.fieldType(property)) {
      "Unable to find a suitable ValueType for $property"
    }
  )

  fun <T : Any> fieldDecoder(decoder: ResultSetDecoder<T>, name: String): EntityDecoder<T> = object : EntityDecoder<T> {
    override fun invoke(rs: ResultSet) = decoder.getResultSetValue(rs, name)
  }

  private data class CacheKey(
    val table: KClass<*>,
    val alias: String? = null
  )

  companion object : KLogging()
}
