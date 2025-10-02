package uk.tvidal.data.mariadb

import org.junit.jupiter.api.Test
import uk.tvidal.data.RandomUUID
import uk.tvidal.data.TestDialect.SqlAssertions.assertQuery
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
    assertQuery { dialect.save(Person::class) }
      .isEqualTo("INSERT INTO `person` (`name`,`id`) VALUES (?,?) ON DUPLICATE KEY UPDATE `name`=VALUES(`name`)")
  }

  companion object {

  }
}
