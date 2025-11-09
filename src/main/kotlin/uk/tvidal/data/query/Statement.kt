package uk.tvidal.data.query

import uk.tvidal.data.codec.EntityDecoder
import uk.tvidal.data.logging.KLogging
import java.sql.Connection
import java.sql.PreparedStatement

class Statement(
  val statement: PreparedStatement,
  parameters: Collection<QueryParam.Value> = emptyList()
) : AutoCloseable {

  constructor(cnn: Connection, sql: String) : this(
    statement = cnn.prepareStatement(sql)
  )

  constructor(cnn: Connection, query: SimpleQuery) : this(
    statement = cnn.prepareStatement(query.sql),
    parameters = query.params.filterIsInstance<QueryParam.Value>()
  )

  init {
    setParams(parameters)
  }

  fun setParams(parameters: Collection<QueryParam.Value>) {
    if (parameters.isNotEmpty()) {
      debug { "setParams: [\n\t${parameters.joinToString("\n\t")}\n]" }
      for (param in parameters) {
        param.setParamValue(statement, param.index)
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

  fun execute(): Boolean {
    trace { "execute: $this" }
    return statement.execute()
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
