package uk.tvidal.data.logging

import uk.tvidal.data.logging.KLogger.Companion.TEMPLATE_EXCEPTION

abstract class KLogging {

  val log = KLogger(this::class)

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

  inline fun <T> T.alsoInfo(message: (T) -> Any?) = also {
    log.info { message(it) }
  }

  inline fun <T> T.alsoDebug(message: (T) -> Any?) = also {
    log.debug { message(it) }
  }

  inline fun <T> T.alsoTrace(message: (T) -> Any?) = also {
    log.trace { message(it) }
  }
}
