package uk.tvidal.data

import uk.tvidal.data.schema.Constraint

class TestDialect : Dialect(NamingStrategy.AsIs) {

  fun constraint(it: Constraint) = buildString {
    constraint(it)
  }.let {
    spaces.replace(it, " ")
  }
}
