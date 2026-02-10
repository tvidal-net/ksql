package uk.tvidal.data.query

import uk.tvidal.data.fields
import uk.tvidal.data.hasAnyAnnotation
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.hasAnnotation

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Aggregate

@Aggregate
@Target(AnnotationTarget.PROPERTY)
annotation class Count

@Aggregate
@Target(AnnotationTarget.PROPERTY)
annotation class Sum

@Aggregate
@Target(AnnotationTarget.PROPERTY)
annotation class Max

@Aggregate
@Target(AnnotationTarget.PROPERTY)
annotation class Min

@Aggregate
@Target(AnnotationTarget.PROPERTY)
annotation class Average

internal val KProperty<*>.isAggregate: Boolean
  get() = hasAnyAnnotation { it.annotationClass.hasAnnotation<Aggregate>() }

internal val <E : Any> KClass<E>.groupBy: Collection<KProperty1<E, *>>
  get() = fields.filterNot(KProperty<*>::isAggregate)
