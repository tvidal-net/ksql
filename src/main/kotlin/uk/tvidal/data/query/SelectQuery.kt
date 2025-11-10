package uk.tvidal.data.query

import uk.tvidal.data.codec.EntityDecoder
import uk.tvidal.data.logging.KLogging
import uk.tvidal.data.simpleName
import java.sql.Connection

class SelectQuery<E>(
  override val sql: String,
  val decode: EntityDecoder<E>,
  override val params: Collection<QueryParam> = emptyList(),
) : Query {

  fun all(
    cnn: Connection,
    paramValues: Collection<Any?> = emptyList(),
  ) = cnn.prepareStatement(sql).use { st ->
    setParamValues(st, params, paramValues)
    st.executeQuery().use { rs ->
      debug { "Select ALL params=$params\n$sql" }
      buildList {
        while (rs.next()) add(
          decode(rs)!!.trace {
            "decode $it"
          }
        )
      }
    }
  }

  fun one(
    cnn: Connection,
    paramValues: Collection<Any?> = emptyList(),
  ): E? = cnn.prepareStatement(sql).use { st ->
    setParamValues(st, params, paramValues)
    st.executeQuery().use { rs ->
      debug { "Select ONE params=$params\n$sql" }
      if (!rs.next()) null
      else decode(rs)
    }.trace {
      "decode $it"
    }
  }

  override fun toString() = "$simpleName[params=$params, sql=$sql]"

  companion object : KLogging()
}
