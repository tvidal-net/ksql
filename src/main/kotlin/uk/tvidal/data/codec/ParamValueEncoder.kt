package uk.tvidal.data.codec

import java.sql.PreparedStatement

@FunctionalInterface
interface ParamValueEncoder<T> {
  fun setParamValue(ps: PreparedStatement, parameterIndex: Int, value: T?)
}
