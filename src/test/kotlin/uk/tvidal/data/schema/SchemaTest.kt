package uk.tvidal.data.schema

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.tvidal.data.TableName
import uk.tvidal.data.database.Account
import uk.tvidal.data.database.Child
import uk.tvidal.data.database.Person

class SchemaTest {
  @Test
  fun noForeignKeys() {
    assertThat(foreignKeys(Person::class)).isEmpty()
  }

  @Test
  fun simpleForeignKey() {
    assertThat(foreignKeys(Child::class)).containsExactly(
      Constraint.ForeignKey(
        table = TableName("Parent"),
        references = listOf(on("parent", "id"))
      )
    )
  }

  @Test
  fun recursiveForeignKey() {
    assertThat(foreignKeys(Account::class)).containsExactly(
      Constraint.ForeignKey(
        table = TableName("Account"),
        references = listOf(on("parent", "id"))
      )
    )
  }
}
