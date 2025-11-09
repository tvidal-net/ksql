package uk.tvidal.data

import org.junit.jupiter.api.Test
import uk.tvidal.data.TestDialect.assertQuery
import uk.tvidal.data.TestDialect.assertSelect
import uk.tvidal.data.TestDialect.assertSql
import uk.tvidal.data.TestDialect.assertThrows
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
    assertSelect { select(person) }
      .isEqualTo("SELECT [age],[id],[name] FROM [Person]")
  }

  @Test
  fun selectByKeyQuery() {
    assertSelect { select(person, person.keyFilter) }
      .isEqualTo("SELECT [age],[id],[name] FROM [Person] WHERE [id] = ?")
  }

  @Test
  fun deleteWithFilter() {
    val filter = where {
      Person::age.gt(10)
    }
    assertSql { delete(person, filter) }
      .isEqualTo("DELETE FROM [Person] WHERE [age] > ?")
  }

  @Test
  fun deleteQuery() {
    assertQuery { delete(person) }
      .isEqualTo("DELETE FROM [Person] WHERE [id] = ?")
  }

  @Test
  fun updateQuery() {
    assertQuery { update(person) }
      .isEqualTo("UPDATE [Person] SET [age] = ?,[name] = ? WHERE [id] = ?")
  }

  @Test
  fun insertQuery() {
    assertQuery { insert(person) }
      .isEqualTo("INSERT INTO [Person] ([age],[id],[name]) VALUES (?,?,?)")
  }

  @Test
  fun saveFailsOnDefaultDialect() {
    assertThrows { save(person) }
      .isExactlyInstanceOf(NotImplementedError::class.java)
      .hasMessageContaining("save is not implemented for the default Dialect")
  }

  companion object {
    val person = Person::class
  }
}
