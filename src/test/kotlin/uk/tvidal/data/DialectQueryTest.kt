package uk.tvidal.data

import org.junit.jupiter.api.Test
import uk.tvidal.data.TestDialect.SqlAssertions.assertQuery
import uk.tvidal.data.TestDialect.SqlAssertions.assertThrows
import java.util.UUID
import javax.persistence.Id

class DialectQueryTest {

  data class Person(
    val name: String,
    val age: Int,
    @Id val id: UUID = RandomUUID
  )

  @Test
  fun selectAllQuery() {
    assertQuery { select(entity) }
  }

  @Test
  fun selectByKeyQuery() {
    TODO("not implemented")
  }

  @Test
  fun deleteWithFilter() {
    TODO("not implemented")
  }

  @Test
  fun deleteQuery() {
    TODO("not implemented")
  }

  @Test
  fun updateQuery() {
    TODO("not implemented")
  }

  @Test
  fun insertQuery() {
    TODO("not implemented")
  }

  @Test
  fun saveFailsOnDefaultDialect() {
    assertThrows { save(entity) }
      .isExactlyInstanceOf(NotImplementedError::class.java)
      .hasMessageContaining("saveQuery is not implemented")
  }

  companion object {
    val entity = Person::class
  }
}
