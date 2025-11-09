package uk.tvidal.data.query

import uk.tvidal.data.codec.EntityDecoder
import java.sql.Connection

class SelectQuery<out E>(
  override val sql: String,
  val decode: EntityDecoder<E>,
  override val params: Collection<QueryParam> = emptyList(),
) : Query {

  fun all(
    cnn: Connection,
    paramValues: Collection<Any?> = emptyList(),
  ): Sequence<E> = cnn.prepareStatement(sql).use { st ->
    setParamValues(st, params, paramValues)
    val rs = st.executeQuery()
    return object : Sequence<E> {
      override fun iterator(): Iterator<E> = object : Iterator<E> {
        override fun next(): E = decode(rs)
        override fun hasNext() = rs.next()
          .also { if (!it && !rs.isClosed) rs.close() }
      }
    }
  }

  fun one(
    cnn: Connection,
    paramValues: Collection<Any?> = emptyList(),
  ): E? = cnn.prepareStatement(sql).use { st ->
    setParamValues(st, params, paramValues)
    st.executeQuery().use { rs ->
      if (!rs.next()) null
      else decode(rs)
    }
  }

  override fun toString() = "${this::class.simpleName}[$sql\n] params=$params"
}
