package uk.tvidal.data.codec

import java.sql.ResultSet

@FunctionalInterface
fun interface ResultSetDecoder<out T> {
  fun getResultSetValue(resultSet: ResultSet, fieldName: String): T?
}
