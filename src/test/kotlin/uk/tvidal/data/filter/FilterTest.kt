package uk.tvidal.data.filter

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.tvidal.data.equalsFilter
import uk.tvidal.data.keyFilter
import uk.tvidal.data.logging.KLogging
import uk.tvidal.data.whereClause
import javax.persistence.Id
import kotlin.test.assertEquals

class FilterTest {

  private data class TestClass(
    val name: String,
    @Id val key: Int,
  )

  @Test
  fun testSingleKeyFilter() {
    assertEquals(
      SqlPropertyParamFilter.Equals(TestClass::key),
      TestClass::class.keyFilter
    )
  }

  private data class MultiKeyTable(
    val name: String,
    @Id val firstKey: Int,
    @Id val secondKey: Int,
  )

  @Test
  fun testMultiKeyFilter() {
    val expected = whereClause {
      MultiKeyTable::firstKey.eq()
      MultiKeyTable::secondKey.eq()
    }
    val actual = MultiKeyTable::class.keyFilter as SqlMultiFilter.And
    assertEquals(expected, actual)
  }

  @Test
  fun testFailEmptyKeyFields() {
    assertThrows<IllegalArgumentException> {
      equalsFilter(emptyList())
    }
  }

  @Test
  fun testOrFilter() {
    val expected = SqlMultiFilter.Or(
      listOf(
        SqlPropertyParamFilter.GreaterThan(TestClass::key),
        SqlPropertyParamFilter.LessThan(TestClass::key)
      )
    )
    val actual = whereClause {
      TestClass::key.gt().or(TestClass::key.lt())
    }
    assertEquals(expected, actual)
  }

  companion object : KLogging()
}
