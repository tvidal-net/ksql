package uk.tvidal.data.query

import uk.tvidal.data.codec.ParamValueEncoder
import uk.tvidal.data.fieldName
import java.sql.Connection
import java.sql.PreparedStatement
import kotlin.reflect.KProperty1

class EntityQuery<in E>(
  val sql: String,
  val params: Collection<Param<E>>,
) {
  fun execute(cnn: Connection, entity: E): Int = cnn.prepareStatement(sql).use { st ->
    setParamValues(st, entity)
    st.executeUpdate()
  }

  fun execute(cnn: Connection, entities: Iterable<E>): IntArray = cnn.prepareStatement(sql).use { st ->
    for (entity in entities) {
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

  class Param<in E>(
    val index: Int,
    val encoder: ParamValueEncoder,
    val property: KProperty1<in E, *>,
  ) {

    val name: String
      get() = property.fieldName

    fun setParamValue(st: PreparedStatement, entity: E) {
      val value = property(entity)
      encoder.setParamValue(st, index, value)
    }

    override fun toString() = "$index:$name"
  }
}
