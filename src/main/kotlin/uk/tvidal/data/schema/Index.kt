package uk.tvidal.data.schema

data class Index(
  val fields: Collection<FieldReference>,
  val name: String? = null,
)
