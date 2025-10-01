package uk.tvidal.data.schema

sealed interface ColumnReference {

  val name: String

  data class Ascending(override val name: String) : ColumnReference {
    override fun toString() = name
  }

  data class Descending(override val name: String) : ColumnReference {
    override fun toString() = "$name DESC"
  }

  companion object Factory {
    fun asc(name: String): ColumnReference = Ascending(name)
    fun desc(name: String): ColumnReference = Descending(name)
  }
}
