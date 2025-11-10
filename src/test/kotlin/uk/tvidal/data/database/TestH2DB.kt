package uk.tvidal.data.database

fun main() {
  runTestSuite(
    H2DB.createDatabase(
      url = "jdbc:h2:mem:temp;DB_CLOSE_DELAY=-1"
    )
  )
}
