package uk.tvidal.data.codec

import java.sql.ResultSet

@FunctionalInterface
interface ResultSetDecoder<out T> {
  operator fun invoke(rs: ResultSet, fieldName: String): T?
}
