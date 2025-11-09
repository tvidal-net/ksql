package uk.tvidal.data.query

import uk.tvidal.data.codec.ParamValueEncoder

open class QueryParam(
  val index: Int,
  val name: String,
  val encoder: ParamValueEncoder,
) {
  override fun toString() = "$index:$name"
}
