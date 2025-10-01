package uk.tvidal.data.query

sealed interface QueryParam {

  val index: Int
  val name: String

  data class Value(
    override val index: Int,
    override val name: String,
    val value: Any?,
  ) : QueryParam {
    override fun toString() = "$index:$name=$value"
  }
}
