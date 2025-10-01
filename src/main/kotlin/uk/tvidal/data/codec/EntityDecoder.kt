package uk.tvidal.data.codec

import uk.tvidal.data.NamingStrategy
import uk.tvidal.data.fieldName
import uk.tvidal.data.fields
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1

interface EntityDecoder<out E> {

  operator fun invoke(rs: ResultSet): E

  class ByConstructor<out E>(
    val constructor: KFunction<E>,
    val namingStrategy: NamingStrategy,
  ) : EntityDecoder<E> {

    private val constructorParams = constructor.parameters
      .associateWith { it.resultSetDecoder }

    override fun invoke(rs: ResultSet): E = constructor.callBy(
      constructorParams.mapValues { (param, decodeValue) ->
        val fieldName = namingStrategy[param.fieldName]
        decodeValue(rs, fieldName)
      }
    )
  }

  class ByProperties<out E>(
    val constructor: KCallable<E>,
    val namingStrategy: NamingStrategy,
    val constructorArgs: Array<out Any?> = emptyArray(),
  ) : EntityDecoder<E> {

    private val fields = constructor.entity.fields.filterIsInstance<KMutableProperty1<in E, Any?>>()
      .associateWith { it.resultSetDecoder }

    private fun ResultSetMetaData.columnNames() = buildSet {
      for (i in 1..columnCount) {
        add(
          getColumnName(i)
        )
      }
    }

    override fun invoke(rs: ResultSet): E = constructor.call(*constructorArgs).also { instance ->
      val existing = rs.metaData.columnNames()
      fields.forEach { (field, decodeValue) ->
        val fieldName = namingStrategy[field.fieldName]
        if (fieldName in existing) {
          val value = decodeValue(rs, fieldName)
          field.set(instance, value)
        }
      }
    }
  }
}
