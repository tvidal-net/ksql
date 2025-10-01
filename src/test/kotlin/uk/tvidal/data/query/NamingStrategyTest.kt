package uk.tvidal.data.query

import org.junit.jupiter.api.Test
import uk.tvidal.data.NamingStrategy.AS_IS
import uk.tvidal.data.NamingStrategy.SNAKE_CASE
import kotlin.test.assertEquals

class NamingStrategyTest {

  @Test
  fun testAsIsNaming() {
    assertEquals(
      "NotChangedName",
      AS_IS["NotChangedName"]
    )
  }

  @Test
  fun testSnakeCaseNaming() {
    assertEquals(
      "my_very_long_table_name",
      SNAKE_CASE["MyVeryLongTableName"]
    )
  }
}
