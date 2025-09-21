package uk.tvidal.data.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import org.slf4j.spi.LoggingEventBuilder
import kotlin.reflect.KClass

class KLogger(val log: Logger) : Logger by log {

  constructor(logger: String) : this(LoggerFactory.getLogger(logger))
  constructor(logger: Class<*>) : this(logger.name.substringBefore('$'))
  constructor(logger: KClass<*>) : this(logger.java)

  override fun makeLoggingEventBuilder(level: Level): LoggingEventBuilder = log.makeLoggingEventBuilder(level)
  override fun atLevel(level: Level): LoggingEventBuilder = log.atLevel(level)
  override fun isEnabledForLevel(level: Level): Boolean = log.isEnabledForLevel(level)
  override fun atTrace(): LoggingEventBuilder = log.atTrace()
  override fun atDebug(): LoggingEventBuilder = log.atDebug()
  override fun atInfo(): LoggingEventBuilder = log.atInfo()
  override fun atWarn(): LoggingEventBuilder = log.atWarn()
  override fun atError(): LoggingEventBuilder = log.atError()

  fun error(e: Throwable) =
    log.error(TEMPLATE_EXCEPTION, e::class.simpleName, e.message, e)

  inline fun error(vararg args: Any?, message: () -> Any?) =
    log.error(message().toString(), *args)

  fun warn(e: Throwable) =
    log.warn(TEMPLATE_EXCEPTION, e::class.simpleName, e.message, e)

  inline fun warn(vararg args: Any?, message: () -> Any?) =
    log.warn(message().toString(), *args)

  inline fun info(vararg args: Any?, message: () -> Any?) {
    if (log.isInfoEnabled) log.info(message().toString(), *args)
  }

  inline fun debug(vararg args: Any?, message: () -> Any?) {
    if (log.isDebugEnabled) log.debug(message().toString(), *args)
  }

  inline fun trace(vararg args: Any?, message: () -> Any?) {
    if (log.isTraceEnabled) log.trace(message().toString(), *args)
  }

  companion object {
    const val TEMPLATE_EXCEPTION = "{}: {}"
  }
}
