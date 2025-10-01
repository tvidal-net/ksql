package uk.tvidal.data.schema

data class Index(
  val columns: Collection<ColumnReference>,
  val name: String? = null,
)
