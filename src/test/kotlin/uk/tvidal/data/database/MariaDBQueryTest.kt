package uk.tvidal.data.database

import jakarta.persistence.Id
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.tvidal.data.Config
import uk.tvidal.data.NamingStrategy
import java.util.UUID

class MariaDBQueryTest {

  private val dialect = MariaDB(Config(namingStrategy = NamingStrategy.AsIs))

  private data class Person(
    val name: String,
    val age: Int,
    @Id val id: UUID
  )

  @Test
  fun insertUsesBacktickQuoting() {
    assertThat(dialect.insert(Person::class).sql.normalized).isEqualTo(
      "INSERT INTO `Person` (`age`, `id`, `name`) VALUES (?, ?, ?)"
    )
  }

  @Test
  fun updateUsesBacktickQuoting() {
    assertThat(dialect.update(Person::class).sql.normalized).isEqualTo(
      "UPDATE `Person` SET `age` = ?, `name` = ? WHERE `id` = ?"
    )
  }

  @Test
  fun selectUsesBacktickQuoting() {
    assertThat(dialect.select(Person::class).sql.normalized).isEqualTo(
      "SELECT `age`, `id`, `name` FROM `Person`"
    )
  }

  @Test
  fun saveUpsertsOnDuplicateKey() {
    assertThat(dialect.save(Person::class).sql.normalized).isEqualTo(
      "INSERT INTO `Person` (`age`, `name`, `id`) VALUES (?, ?, ?) " +
        "ON DUPLICATE KEY UPDATE `age`=VALUES(`age`), `name`=VALUES(`name`)"
    )
  }

  private companion object {

    private val newLines = Regex("[\\n\\r]+\\s?")
    private val spaces = Regex("\\s+")

    val String.normalized: String
      get() = trim()
        .replace(newLines, " ")
        .replace(spaces, " ")
  }
}
