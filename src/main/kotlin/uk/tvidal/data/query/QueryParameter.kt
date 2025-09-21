package uk.tvidal.data.query

sealed interface QueryParameter {

  val index: Int
  val name: String
}
