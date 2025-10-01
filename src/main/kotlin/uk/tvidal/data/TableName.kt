package uk.tvidal.data

data class TableName(
  val name: String,
  val schema: String? = null
) {
  override fun toString(): String = if (!schema.isNullOrBlank()) {
    "$schema.$name"
  } else {
    name
  }
}
