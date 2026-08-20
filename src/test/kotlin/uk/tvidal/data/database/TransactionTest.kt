package uk.tvidal.data.database

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import uk.tvidal.data.Database

class TransactionTest {

  @Test
  fun rollsBackOnFailure() {
    val repository = db.repository<Parent>()
    val parent = Parent(name = "rolled back")

    assertThatThrownBy {
      db {
        repository.insert(parent)
        error("failing inside the transaction")
      }
    }.hasMessage("failing inside the transaction")

    assertThat(repository.one(parent.id)).isNull()
  }

  @Test
  fun commitsOnSuccess() {
    val repository = db.repository<Parent>()
    val parent = Parent(name = "committed")

    db {
      repository.insert(parent)
    }

    assertThat(repository.one(parent.id)).isEqualTo(parent)
  }

  companion object {

    private val db: Database = H2DB().createDatabase(
      "jdbc:h2:mem:transaction;DB_CLOSE_DELAY=-1"
    )

    @BeforeAll
    @JvmStatic
    fun setUp() = db.create(Parent::class)

    @AfterAll
    @JvmStatic
    fun tearDown() = db.drop(Parent::class)
  }
}
