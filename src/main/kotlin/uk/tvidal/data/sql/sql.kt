package uk.tvidal.data.sql

import uk.tvidal.data.fields
import uk.tvidal.data.isKeyField
import uk.tvidal.data.column
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal val <E : Any> KClass<E>.insertFields: Collection<KProperty1<E, *>>
  get() = fields.filterNot { it.column?.insertable == false }

internal val <E : Any> KClass<E>.updateFields: Collection<KProperty1<E, *>>
  get() = fields.filterNot { it.isKeyField || it.column?.updatable == false }
