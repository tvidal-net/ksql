package uk.tvidal.data.query

import uk.tvidal.data.codec.EntityDecoder
import uk.tvidal.data.codec.setParamValue
import uk.tvidal.data.logging.KLogging
import java.sql.Connection
import java.sql.PreparedStatement

class Statement(
  val statement: PreparedStatement,
  parameters: Collection<QueryParam.Value> = emptyList()
) : AutoCloseable {

  constructor(cnn: Connection, query: SimpleQuery) : this(
    statement = cnn.prepareStatement(query.sql),
    parameters = query.parameters.filterIsInstance<QueryParam.Value>()
  )

  init {
    setParams(parameters)
  }

  fun setParams(vararg values: Any?) {
    if (values.isNotEmpty()) {
      debug { "setParams: ${values.contentToString()}" }
      for ((i, value) in values.withIndex()) {
        statement.setParamValue(FIRST_PARAM + i, value)
      }
    }
  }

  fun setParams(parameters: Collection<QueryParam.Value>) {
    if (parameters.isNotEmpty()) {
      debug { "setParams: $parameters" }
      for (param in parameters) {
        statement.setParamValue(
          index = param.index,
          value = param.value,
        )
      }
    }
  }

  fun <E> one(decodeResultSet: EntityDecoder<E>): E? {
    trace { "selectOne: $this" }
    return statement.executeQuery().use { rs ->
      if (rs.next()) {
        decodeResultSet(rs)
      } else {
        null
      }
    }
  }

  fun <E> all(decodeResultSet: EntityDecoder<E>): Sequence<E> {
    trace { "selectAll: $this" }
    val rs = statement.executeQuery()
    return object : Sequence<E> {
      override fun iterator(): Iterator<E> = object : Iterator<E> {
        override fun next() = decodeResultSet(rs)
        override fun hasNext() = rs.next()
          .also { if (!it && !rs.isClosed) close() }
      }
    }
  }

  fun executeSingle(): Int {
    trace { "executeSingle: $this" }
    return statement.executeUpdate()
  }

  fun executeBatch(): IntArray {
    trace { "executeBatch: $this" }
    return statement.executeBatch()
  }

  override fun toString() = statement.toString()

  override fun close() {
    statement.close()
  }

  companion object : KLogging() {
    const val FIRST_PARAM = 1
  }
}
