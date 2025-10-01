package uk.tvidal.data

import uk.tvidal.data.query.SimpleQuery

val spaces = Regex("\\s+")

val SimpleQuery.actual: String
  get() = spaces.replace(sql, " ")
