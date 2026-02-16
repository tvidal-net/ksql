package uk.tvidal.data.schema

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.tvidal.data.TableName
import java.util.UUID
import javax.persistence.Id

class SchemaTest {
  @Test
  fun noForeignKeys() {
    data class Person(val name: String, @Id val id: UUID)
    assertThat(foreignKeys(Person::class)).isEmpty()
  }

  @Test
  fun simpleForeignKey() {
    data class Parent(@Id val id: UUID)
    data class Child(val parent: Parent)
    assertThat(foreignKeys(Child::class)).containsExactly(
      Constraint.ForeignKey(
        table = TableName("Parent"),
        references = listOf(on("parent", "id"))
      )
    )
  }

  @Test
  fun recursiveForeignKey() {
    data class Account(
      @References(Account::class) val parent: UUID?,
      @Id val id: UUID
    )
    assertThat(foreignKeys(Account::class)).containsExactly(
      Constraint.ForeignKey(
        table = TableName("Account"),
        references = listOf(on("parent", "id"))
      )
    )
  }
}
