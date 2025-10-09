package uk.tvidal.data.test

import uk.tvidal.data.allTables
import uk.tvidal.data.database.H2DB

private val h2 = H2DB.Default

private val db = H2DB.createDatabase(
  url = "jdbc:h2:mem:test",
  dialect = h2,
)

fun main() {
  db {
    allTables.forEach { table ->
      db.execute(h2.create(table))
    }
    allTables.reversed().forEach { table ->
      db.execute(h2.drop(table))
    }
  }
}
