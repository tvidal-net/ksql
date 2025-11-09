package uk.tvidal.data.query

import java.sql.Connection

class SimpleQuery(
  override val sql: String,
  override val params: Collection<QueryParam> = emptyList(),
) : Query {

  fun execute(cnn: Connection, vararg paramValues: Any?) = cnn.prepareStatement(sql).use { st ->
    setParamValues(st, params, paramValues.toList())
    st.execute()
  }

  override fun toString() = "${this::class.simpleName}[$sql\n] params=$params"
}
