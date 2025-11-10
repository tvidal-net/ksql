package uk.tvidal.data.query

import uk.tvidal.data.codec.ParamValueEncoder
import uk.tvidal.data.logging.KLogging
import uk.tvidal.data.simpleName
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
    st.executeUpdate().debug {
      "affected: $it, $logMessage"
    }
  }

  fun execute(
    cnn: Connection,
    values: Iterable<E>
  ): IntArray = cnn.prepareStatement(sql).use { st ->
    for (value in values) {
      params.forEach {
        it.setParamValue(st, value)
      }
      trace { "addBatch" }
      st.addBatch()
    }
    st.executeBatch().debug {
      "affected: ${it.sum()}, $logMessage"
    }
  }

  fun setParamValues(st: PreparedStatement, value: E) = params.forEach {
    it.setParamValue(st, value)
  }

  override fun toString() = "$simpleName[params=$params, sql=$sql]"

  class Param<in E>(
    index: Int,
    encoder: ParamValueEncoder<Any>,
    val property: KProperty1<in E, *>,
  ) : QueryParam(index, property.name, encoder) {

    fun setParamValue(st: PreparedStatement, value: E) {
      encoder.setParamValue(st, index, property(value))
    }

    override fun toString() = "$index:$name"
  }

  companion object : KLogging()
}
