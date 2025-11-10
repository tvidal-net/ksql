package uk.tvidal.data.codec

import java.sql.PreparedStatement

@FunctionalInterface
fun interface ParamValueEncoder<in T> {
  fun setParamValue(st: PreparedStatement, index: Int, value: T?)
}
