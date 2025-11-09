package uk.tvidal.data.codec

import java.sql.PreparedStatement

@FunctionalInterface
interface ParamValueEncoder {
  fun setParamValue(st: PreparedStatement, index: Int, value: Any?)
}
