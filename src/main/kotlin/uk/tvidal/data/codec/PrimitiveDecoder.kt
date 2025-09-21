package uk.tvidal.data.codec

import java.sql.ResultSet

class PrimitiveDecoder<out T>(val getValue: ResultSet.(String) -> T) : ResultSetDecoder<T> {

  override fun invoke(rs: ResultSet, fieldName: String): T? {
    val value = getValue(rs, fieldName)
    return if (rs.wasNull()) null else value
  }
}
