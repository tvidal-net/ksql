package uk.tvidal.data.query

import uk.tvidal.data.logging.KLogging
import uk.tvidal.data.simpleName
import java.sql.Connection

class SimpleQuery(
  override val sql: String,
  override val params: Collection<QueryParam> = emptyList(),
) : Query {

  fun execute(cnn: Connection, vararg paramValues: Any?) = cnn.prepareStatement(sql).use { st ->
    setParamValues(st, params, paramValues.toList())
    st.executeUpdate().debug {
      "affected: $it, $logMessage"
    }
  }

  override fun toString() = "$simpleName[params=$params, sql=$sql]"

  companion object : KLogging()
}
