package uk.tvidal.data.query

open class SimpleQuery(
  val sql: String,
  open val parameters: Collection<QueryParam> = emptyList()
) : Iterable<QueryParam> {

  override fun iterator() = parameters.iterator()

  override fun toString() = "params=$parameters\n\t$sql"
}
