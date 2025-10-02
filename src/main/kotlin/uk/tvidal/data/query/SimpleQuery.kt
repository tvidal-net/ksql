package uk.tvidal.data.query

open class SimpleQuery(
  sql: String,
  open val params: Collection<QueryParam> = emptyList()
) : SqlQuery(sql), Iterable<QueryParam> {

  override fun iterator() = params.iterator()

  override fun toString() = "params=$params\n\t$sql"
}
