package uk.tvidal.data.codec

import java.sql.ResultSet

@FunctionalInterface
interface ResultSetDecoder<out T> {
  fun getResultSetValue(rs: ResultSet, field: String): T?
}
