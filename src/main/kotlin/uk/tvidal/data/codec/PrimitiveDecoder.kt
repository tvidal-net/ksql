package uk.tvidal.data.codec

import java.sql.ResultSet

class PrimitiveDecoder<out T>(val getValue: ResultSet.(String) -> T) : ResultSetDecoder<T> {

  override fun getResultSetValue(rs: ResultSet, columnLabel: String): T? {
    val value = getValue(rs, columnLabel)
    return if (rs.wasNull()) null else value
  }
}
