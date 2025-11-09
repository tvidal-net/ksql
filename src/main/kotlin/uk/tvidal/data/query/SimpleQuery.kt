package uk.tvidal.data.query

import uk.tvidal.data.codec.ParamValueEncoder
import java.sql.PreparedStatement

open class SimpleQuery(
  val sql: String,
  open val encoders: Collection<ParamValueEncoder<Any>>
) {

  fun setParamValues(st: PreparedStatement) {

  }

  override fun toString() = sql
}
