package uk.tvidal.data.database

import org.assertj.core.api.Assertions.assertThat
import uk.tvidal.data.Database

abstract class DatabaseTest(val db: Database) {

  fun createDatabase() = db.create(
    ValueTypes::class,
  )

  fun testValueTypes() {
    val inserted = ValueTypes()
    val updated = ValueTypes(id = inserted.id)

    saveTest(inserted.id, inserted, updated)
    crudTest(inserted.id, inserted, updated)
  }

  inline fun <reified T : Any> saveTest(id: Any, inserted: T, updated: T) {
    val repo = db.repository<T>()
    assertThat(repo.save(inserted)).isEqualTo(1)
    assertThat(repo[id]).isEqualTo(inserted)

    assertThat(repo.save(updated)).isEqualTo(1)
    assertThat(repo[id]).isEqualTo(updated)

    assertThat(repo.delete(updated)).isEqualTo(1)
    assertThat(repo[id]).isNull()
  }

  inline fun <reified T : Any> crudTest(id: Any, inserted: T, updated: T) {
    val repo = db.repository<T>()
    assertThat(repo.insert(inserted)).isEqualTo(1)
    assertThat(repo[id]).isEqualTo(inserted)

    assertThat(repo.update(updated)).isEqualTo(1)
    assertThat(repo[id]).isEqualTo(updated)

    assertThat(repo.delete(updated)).isEqualTo(1)
    assertThat(repo[id]).isNull()
  }

  fun dropDatabase() = db.drop(
    ValueTypes::class,
  )
}
