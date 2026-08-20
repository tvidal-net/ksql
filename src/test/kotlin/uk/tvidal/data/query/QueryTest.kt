package uk.tvidal.data.query

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.tvidal.data.query.SelectFrom.Join
import uk.tvidal.data.query.SelectFrom.Table
import java.util.UUID
import jakarta.persistence.Id

@Suppress("UNCHECKED_CAST")
class QueryTest {

  @Test
  fun testSimpleSelectQuery() {
    val query = from(Parent::class)
    assertThat(query).hasSize(1)

    val from = query.single() as Table<Parent>
    assertThat(from.type).isSameAs(Parent::class)
    assertThat(from.name).isEqualTo("Parent")
    assertThat(from.alias).isNull()
    assertThat(from.groupBy).containsExactlyElementsOf(from.fields)
    assertThat(from.fields).containsExactlyInAnyOrder(
      Parent::name,
      Parent::id,
    )
  }

  @Test
  fun testSimpleAggregateQuery() {
    class SimpleAggregate(
      @Count val count: Int,
      val name: String,
    )

    val from = from(SimpleAggregate::class).single() as Table<SimpleAggregate>
    assertThat(from.type).isSameAs(SimpleAggregate::class)
    assertThat(from.name).isEqualTo("SimpleAggregate")
    assertThat(from.alias).isNull()
    assertThat(from.groupBy).containsExactlyInAnyOrder(
      SimpleAggregate::name,
    )
    assertThat(from.fields).containsExactlyInAnyOrder(
      SimpleAggregate::count,
      SimpleAggregate::name,
    )
  }

  @Test
  fun testQueryInnerJoin() {
    class InnerChild(
      val name: String,
      val parent: Parent,
      @Id val id: UUID,
    )

    val from = from(InnerChild::class)
    assertThat(from).hasSize(2)

    val child = from[0] as Table<InnerChild>
    assertThat(child.fields).containsExactlyInAnyOrder(
      InnerChild::name,
      InnerChild::parent,
      InnerChild::id,
    )
    assertThat(child.alias).isNull()
    assertThat(child.name).isEqualTo("InnerChild")
    assertThat(child.groupBy).containsExactlyElementsOf(child.fields)

    val join = from[1] as Join
    assertThat(join.from).isInstanceOf(Table::class.java)
    assertThat(join.type).isEqualTo(Join.Type.Inner)
    assertThat(join.on).isEqualTo(Parent::id eq InnerChild::parent)
    assertThat(join.alias).isEqualTo("parent")

    val parent = join.from as Table<Parent>
    assertThat(parent.alias).isEqualTo("parent")
    assertThat(parent.name).isEqualTo("Parent")
    assertThat(parent.groupBy).containsExactlyElementsOf(parent.fields)
    assertThat(parent.fields).containsExactlyInAnyOrder(
      Parent::name,
      Parent::id,
    )
  }

  @Test
  fun testQueryLeftJoin() {
    class LeftChild(
      val name: String,
      val parent: Parent?,
      @Id val id: UUID,
    )

    val from = from(LeftChild::class, "child")
    assertThat(from).hasSize(2)

    val child = from[0] as Table<LeftChild>
    assertThat(child.fields).containsExactlyInAnyOrder(
      LeftChild::name,
      LeftChild::parent,
      LeftChild::id,
    )
    assertThat(child.alias).isEqualTo("child")
    assertThat(child.name).isEqualTo("LeftChild")
    assertThat(child.groupBy).containsExactlyElementsOf(child.fields)

    val join = from[1] as Join
    assertThat(join.from).isInstanceOf(Table::class.java)
    assertThat(join.type).isEqualTo(Join.Type.Left)
    assertThat(join.on).isEqualTo(Parent::id.eq(LeftChild::parent, "child"))
    assertThat(join.alias).isEqualTo("parent")

    val parent = join.from as Table<Parent>
    assertThat(parent.alias).isEqualTo("parent")
    assertThat(parent.name).isEqualTo("Parent")
    assertThat(parent.groupBy).containsExactlyElementsOf(parent.fields)
    assertThat(parent.fields).containsExactlyInAnyOrder(
      Parent::name,
      Parent::id,
    )
  }

  private class Parent(val name: String, @Id val id: UUID)
}
