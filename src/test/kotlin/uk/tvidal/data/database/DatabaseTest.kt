package uk.tvidal.data.database

import org.assertj.core.api.Assertions.assertThat
import uk.tvidal.data.Config
import uk.tvidal.data.Database
import uk.tvidal.data.EntityRepository
import uk.tvidal.data.database.ValueTypes.Companion.amountValueType

abstract class DatabaseTest(val db: Database) {

  private object ValueTypesFactory : Factory<ValueTypes> {
    override fun create() = ValueTypes()
    override fun duplicate(value: ValueTypes) = ValueTypes(id = value.id)
    override fun identify(value: ValueTypes) = value.id
  }

  fun createDatabase() = db.create(
    ValueTypes::class,
  )

  fun testValueTypes() {
    with(db.repository<ValueTypes>()) {
      testCrudSingle(ValueTypesFactory)
      testCrudBatch(ValueTypesFactory)
      testSaveSingle(ValueTypesFactory)
      testSaveBatch(ValueTypesFactory)
    }
  }

  private fun <T : Any> EntityRepository<T>.testCrudSingle(factory: Factory<T>) {
    val inserted = factory.create()
    assertThat(insert(inserted)).isEqualTo(ONE)

    val id = factory.identify(inserted)
    assertThat(one(id)).isEqualTo(inserted)

    val updated = factory.duplicate(inserted)
    assertThat(update(updated)).isEqualTo(ONE)
    assertThat(one(id)).isEqualTo(updated)

    assertThat(delete(updated)).isEqualTo(ONE)
    assertThat(one(id)).isNull()
  }

  private fun <T : Any> EntityRepository<T>.testCrudBatch(factory: Factory<T>) {
    val inserted = List(EXPECTED.size) { factory.create() }
    assertThat(insert(inserted)).isEqualTo(EXPECTED)

    assertThat(toList()).containsExactlyInAnyOrderElementsOf(inserted)

    val updated = inserted.map(factory::duplicate)
    assertThat(update(updated)).isEqualTo(EXPECTED)
    assertThat(toList()).containsExactlyInAnyOrderElementsOf(updated)

    assertThat(delete(updated)).isEqualTo(EXPECTED)
    assertThat(toList()).isEmpty()
  }

  private fun <T : Any> EntityRepository<T>.testSaveBatch(factory: Factory<T>) {
    val inserted = List(EXPECTED.size) { factory.create() }
    assertThat(save(inserted)).isEqualTo(EXPECTED)

    assertThat(toList()).containsExactlyInAnyOrderElementsOf(inserted)

    val updated = inserted.map(factory::duplicate)
    assertThat(save(updated)).isEqualTo(EXPECTED)
    assertThat(toList()).containsExactlyInAnyOrderElementsOf(updated)

    assertThat(delete(updated)).isEqualTo(EXPECTED)
    assertThat(toList()).isEmpty()
  }

  private fun <T : Any> EntityRepository<T>.testSaveSingle(factory: Factory<T>) {
    val inserted = factory.create()
    assertThat(save(inserted)).isEqualTo(ONE)

    val id = factory.identify(inserted)
    assertThat(one(id)).isEqualTo(inserted)

    val updated = factory.duplicate(inserted)
    assertThat(save(updated)).isEqualTo(ONE)
    assertThat(one(id)).isEqualTo(updated)

    assertThat(delete(updated)).isEqualTo(ONE)
    assertThat(one(id)).isNull()
  }

  fun dropDatabase() = db.drop(
    ValueTypes::class,
  )

  interface Factory<T : Any> {
    fun create(): T
    fun duplicate(value: T): T
    fun identify(value: T): Any
  }

  companion object {
    private const val ONE = 1
    private val EXPECTED = IntArray(10) { ONE }

    init {
      Config.Default.register { amountValueType }
    }
  }
}
