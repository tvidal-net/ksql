package uk.tvidal.data.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import org.slf4j.spi.LoggingEventBuilder
import kotlin.reflect.KClass

class KLogger(val logger: Logger) : Logger by logger {

  constructor(logger: String) : this(LoggerFactory.getLogger(logger))
  constructor(logger: Class<*>) : this(logger.loggerName)
  constructor(logger: KClass<*>) : this(logger.java)

  override fun makeLoggingEventBuilder(level: Level): LoggingEventBuilder = logger.makeLoggingEventBuilder(level)
  override fun atLevel(level: Level): LoggingEventBuilder = logger.atLevel(level)
  override fun isEnabledForLevel(level: Level): Boolean = logger.isEnabledForLevel(level)
  override fun atTrace(): LoggingEventBuilder = logger.atTrace()
  override fun atDebug(): LoggingEventBuilder = logger.atDebug()
  override fun atInfo(): LoggingEventBuilder = logger.atInfo()
  override fun atWarn(): LoggingEventBuilder = logger.atWarn()
  override fun atError(): LoggingEventBuilder = logger.atError()

  fun error(e: Throwable) =
    logger.error(TEMPLATE_EXCEPTION, e::class.simpleName, e.message, e)

  inline fun error(vararg args: Any?, message: () -> Any?) =
    logger.error(message().toString(), *args)

  fun warn(e: Throwable) =
    logger.warn(TEMPLATE_EXCEPTION, e::class.simpleName, e.message, e)

  inline fun warn(vararg args: Any?, message: () -> Any?) =
    logger.warn(message().toString(), *args)

  inline fun info(vararg args: Any?, message: () -> Any?) {
    if (logger.isInfoEnabled) logger.info(message().toString(), *args)
  }

  inline fun debug(vararg args: Any?, message: () -> Any?) {
    if (logger.isDebugEnabled) logger.debug(message().toString(), *args)
  }

  inline fun trace(vararg args: Any?, message: () -> Any?) {
    if (logger.isTraceEnabled) logger.trace(message().toString(), *args)
  }

  companion object {
    const val TEMPLATE_EXCEPTION = "{}: {}"

    val Class<*>.loggerName: String
      get() = name.substringBefore('$')
        .removeSuffix("Kt")
  }
}
