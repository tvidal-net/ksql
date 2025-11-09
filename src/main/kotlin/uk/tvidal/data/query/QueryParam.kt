package uk.tvidal.data.query

import uk.tvidal.data.codec.ParamValueEncoder

sealed interface QueryParam {

  val index: Int
  val name: String
  val encoder: ParamValueEncoder

  class Value(
    override val index: Int,
    override val name: String,
    override val encoder: ParamValueEncoder,
  ) : QueryParam {
    override fun toString() = "$index:$name"
  }
}
