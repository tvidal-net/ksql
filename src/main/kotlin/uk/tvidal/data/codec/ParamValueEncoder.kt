package uk.tvidal.data.codec

import java.sql.PreparedStatement

@FunctionalInterface
fun interface ParamValueEncoder<in T> {
  fun setParamValue(statement: PreparedStatement, index: Int, value: T?)
}
