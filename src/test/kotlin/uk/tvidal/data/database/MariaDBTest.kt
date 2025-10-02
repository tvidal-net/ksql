package uk.tvidal.data.database

import org.junit.jupiter.api.Test
import uk.tvidal.data.RandomUUID
import uk.tvidal.data.TestDialect
import java.util.UUID
import javax.persistence.Id

class MariaDBTest {

  data class Person(
    val name: String?,
    @Id val id: UUID = RandomUUID
  )

  val dialect = MariaDB.Default

  @Test
  fun saveOnDuplicateUpdate() {
    TestDialect.SqlAssertions.assertQuery { dialect.save(Person::class) }
      .isEqualTo("INSERT INTO `person` (`name`,`id`) VALUES (?,?) ON DUPLICATE KEY UPDATE `name`=VALUES(`name`)")
  }

  companion object {

  }
}
