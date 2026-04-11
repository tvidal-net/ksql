package uk.tvidal.data.database

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class H2DBTest {

  @Test
  fun valueTypes() {
    testValueTypes()
  }

  companion object : DatabaseTest(
    db = H2DB().createDatabase("jdbc:h2:mem:temp;DB_CLOSE_DELAY=-1")
  ) {

    @BeforeAll
    @JvmStatic
    fun setUp() = createDatabase()

    @AfterAll
    @JvmStatic
    fun tearDown() = dropDatabase()
  }
}
