package uk.tvidal.data.query

import uk.tvidal.data.codec.ParamValueEncoder
import java.sql.Connection
import java.sql.PreparedStatement
import kotlin.reflect.KProperty1

class EntityQuery<in E>(
  override val sql: String,
  override val params: Collection<Param<E>>,
) : Query {

  fun execute(
    cnn: Connection,
    value: E
  ): Int = cnn.prepareStatement(sql).use { st ->
    setParamValues(st, value)
    st.executeUpdate()
  }

  fun execute(
    cnn: Connection,
    values: Iterable<E>
  ): IntArray = cnn.prepareStatement(sql).use { st ->
    for (entity in values) {
      params.forEach {
        it.setParamValue(st, entity)
      }
      st.addBatch()
    }
    st.executeBatch()
  }

  fun setParamValues(st: PreparedStatement, entity: E) = params.forEach {
    it.setParamValue(st, entity)
  }

  override fun toString() = "${this::class.simpleName}[$sql\n] params=$params"

  class Param<in E>(
    index: Int,
    encoder: ParamValueEncoder<Any>,
    val property: KProperty1<in E, *>,
  ) : QueryParam(index, property.name, encoder) {

    fun setParamValue(st: PreparedStatement, entity: E) {
      val value = property(entity)
      encoder.setParamValue(st, index, value)
    }

    override fun toString() = "$index:$name"
  }
}
