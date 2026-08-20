package uk.tvidal.data

import jakarta.persistence.Id
import org.junit.jupiter.api.Test
import uk.tvidal.data.TestDialect.assertQuery
import uk.tvidal.data.TestDialect.assertSelect
import uk.tvidal.data.TestDialect.assertSql
import uk.tvidal.data.TestDialect.assertThrows
import java.util.UUID

class DialectQueryTest {

  data class Person(
    val name: String,
    val age: Int,
    @Id val id: UUID = RandomUUID
  )

  @Test
  fun selectAllQuery() {
    assertSelect { select(person) }
      .isEqualTo("SELECT [age], [id], [name] FROM [Person]")
  }

  @Test
  fun selectByKeyQuery() {
    assertSelect { select(person, person.keyFilter) }
      .isEqualTo("SELECT [age], [id], [name] FROM [Person] WHERE [id] = ?")
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
  fun deleteWithIsNullFilter() {
    val filter = where {
      Person::name.isNull
    }
    assertSql { delete(person, filter) }
      .isEqualTo("DELETE FROM [Person] WHERE [name] IS NULL")
  }

  @Test
  fun deleteWithIsNotNullFilter() {
    val filter = where {
      Person::name.isNotNull
    }
    assertSql { delete(person, filter) }
      .isEqualTo("DELETE FROM [Person] WHERE [name] IS NOT NULL")
  }

  @Test
  fun deleteWithBetweenFilter() {
    val filter = where {
      Person::age.between(10, 20)
    }
    assertSql { delete(person, filter) }
      .isEqualTo("DELETE FROM [Person] WHERE [age] BETWEEN ? AND ?")
  }

  @Test
  fun deleteWithInFilter() {
    val filter = where {
      Person::age.inValues(1, 2, 3)
    }
    assertSql { delete(person, filter) }
      .isEqualTo("DELETE FROM [Person] WHERE [age] IN (?, ?, ?)")
  }

  @Test
  fun deleteWithLikeFilter() {
    val filter = where {
      Person::name.like("A%")
    }
    assertSql { delete(person, filter) }
      .isEqualTo("DELETE FROM [Person] WHERE [name] LIKE ?")
  }

  @Test
  fun deleteWithNotEqualsFilter() {
    val filter = where {
      Person::age.ne(10)
    }
    assertSql { delete(person, filter) }
      .isEqualTo("DELETE FROM [Person] WHERE [age] != ?")
  }

  @Test
  fun deleteWithGreaterEqualsFilter() {
    val filter = where {
      Person::age.ge(10)
    }
    assertSql { delete(person, filter) }
      .isEqualTo("DELETE FROM [Person] WHERE [age] >= ?")
  }

  @Test
  fun deleteWithLessEqualsFilter() {
    val filter = where {
      Person::age.le(10)
    }
    assertSql { delete(person, filter) }
      .isEqualTo("DELETE FROM [Person] WHERE [age] <= ?")
  }

  @Test
  fun deleteWithMultiFilterAnd() {
    val filter = where {
      Person::age.gt(10)
      Person::name.like("A%")
    }
    assertSql { delete(person, filter) }
      .isEqualTo("DELETE FROM [Person] WHERE ([age] > ? AND [name] LIKE ?)")
  }

  @Test
  fun deleteQuery() {
    assertQuery { delete(person) }
      .isEqualTo("DELETE FROM [Person] WHERE [id] = ?")
  }

  @Test
  fun updateQuery() {
    assertQuery { update(person) }
      .isEqualTo("UPDATE [Person] SET [age] = ?, [name] = ? WHERE [id] = ?")
  }

  @Test
  fun insertQuery() {
    assertQuery { insert(person) }
      .isEqualTo("INSERT INTO [Person] ([age], [id], [name]) VALUES (?, ?, ?)")
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
