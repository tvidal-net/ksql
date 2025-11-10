package uk.tvidal.data

data class TableName(
  val name: String,
  val schema: String? = null
) {
  override fun toString(): String = if (schema.isNullOrBlank()) {
    name
  } else {
    "$schema.$name"
  }
}
