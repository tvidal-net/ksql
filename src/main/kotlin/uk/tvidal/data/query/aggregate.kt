package uk.tvidal.data.query

import uk.tvidal.data.fields
import uk.tvidal.data.findAnyAnnotation
import uk.tvidal.data.hasAnyAnnotation
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

enum class AggregateType {
  COUNT, SUM, MAX, MIN, AVG
}

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Aggregate(val type: AggregateType)

@Aggregate(AggregateType.COUNT)
@Target(AnnotationTarget.PROPERTY)
annotation class Count

@Aggregate(AggregateType.SUM)
@Target(AnnotationTarget.PROPERTY)
annotation class Sum

@Aggregate(AggregateType.MAX)
@Target(AnnotationTarget.PROPERTY)
annotation class Max

@Aggregate(AggregateType.MIN)
@Target(AnnotationTarget.PROPERTY)
annotation class Min

@Aggregate(AggregateType.AVG)
@Target(AnnotationTarget.PROPERTY)
annotation class Average

internal val KProperty<*>.aggregateType: AggregateType?
  get() = findAnyAnnotation { it.annotationClass.findAnnotation<Aggregate>()?.type }

internal val KProperty<*>.isAggregate: Boolean
  get() = hasAnyAnnotation { it.annotationClass.hasAnnotation<Aggregate>() }

internal val <E : Any> KClass<E>.groupBy: Collection<KProperty1<E, *>>
  get() = fields.filterNot(KProperty<*>::isAggregate)
