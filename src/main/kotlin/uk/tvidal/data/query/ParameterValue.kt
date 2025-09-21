package uk.tvidal.data.query

class ParameterValue(
  override val index: Int,
  override val name: String,
  val value: Any?
) : QueryParameter {

  override fun toString() = "$index:$name=$value"
}
