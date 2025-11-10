package uk.tvidal.data.schema

sealed interface FieldReference {

  val name: String

  data class Ascending(override val name: String) : FieldReference {
    override fun toString() = name
  }

  data class Descending(override val name: String) : FieldReference {
    override fun toString() = "$name DESC"
  }

  companion object Factory {
    fun asc(name: String): FieldReference = Ascending(name)
    fun desc(name: String): FieldReference = Descending(name)
  }
}
