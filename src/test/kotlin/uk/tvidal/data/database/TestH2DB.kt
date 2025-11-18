package uk.tvidal.data.database

fun main() {
  System.setProperty("LOG_LEVEL", "DEBUG")
  runTestSuite(
    H2DB.createDatabase(
      url = "jdbc:h2:mem:temp;DB_CLOSE_DELAY=-1"
    )
  )
}
