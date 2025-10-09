package uk.tvidal.data.query

open class SimpleQuery(
  val sql: String,
  open val params: Collection<QueryParam> = emptyList()
) : Iterable<QueryParam> {

  override fun iterator() = params.iterator()

  override fun toString() = "params=$params\n\t$sql"
}
