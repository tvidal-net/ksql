package uk.tvidal.data.logging

import uk.tvidal.data.logging.KLogger.Companion.TEMPLATE_EXCEPTION

abstract class KLogging {

  val log = KLogger(this::class)

  fun error(e: Throwable) =
    log.error(TEMPLATE_EXCEPTION, e::class.simpleName, e.message, e)

  inline fun error(vararg args: Any?, message: () -> Any?) =
    log.error(*args) { message() }

  fun warn(e: Throwable) =
    log.warn(TEMPLATE_EXCEPTION, e::class.simpleName, e.message, e)

  inline fun warn(vararg args: Any?, message: () -> Any?) =
    log.warn(*args) { message() }

  inline fun info(vararg args: Any?, message: () -> Any?) {
    if (log.isInfoEnabled) log.info(*args) { message() }
  }

  inline fun debug(vararg args: Any?, message: () -> Any?) {
    if (log.isDebugEnabled) log.debug(*args) { message() }
  }

  inline fun trace(vararg args: Any?, message: () -> Any?) {
    if (log.isTraceEnabled) log.trace(*args) { message() }
  }
}
