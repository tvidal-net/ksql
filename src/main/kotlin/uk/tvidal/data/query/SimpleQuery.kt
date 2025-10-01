package uk.tvidal.data.query

open class SimpleQuery(
  val sql: String,
  open val parameters: Collection<QueryParameter> = emptyList()
) : Iterable<QueryParameter> {

  override fun iterator() = parameters.iterator()

  override fun toString() = "params=$parameters\n\t$sql"
}
