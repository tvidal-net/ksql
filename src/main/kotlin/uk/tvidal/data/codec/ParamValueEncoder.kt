package uk.tvidal.data.codec

import java.sql.PreparedStatement

@FunctionalInterface
interface ParamValueEncoder<in T> {
  fun setParamValue(st: PreparedStatement, index: Int, value: T?)
}
