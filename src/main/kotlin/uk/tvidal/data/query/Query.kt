package uk.tvidal.data.query

interface Query {
  val sql: String
  val params: Iterable<QueryParam>
}
