package uk.tvidal.data.logging

import uk.tvidal.data.logging.KLogger.Companion.TEMPLATE_EXCEPTION

abstract class KLogging {

  val log = KLogger(this::class)

  fun error(e: Throwable) =
    log.error(TEMPLATE_EXCEPTION, e::class.simpleName, e.message, e)

  inline fun error(message: () -> Any?) =
    log.error { message() }

  fun warn(e: Throwable) =
    log.warn(TEMPLATE_EXCEPTION, e::class.simpleName, e.message, e)

  inline fun warn(message: () -> Any?) =
    log.warn { message() }

  inline fun <T> T.info(message: (T) -> Any?) = also {
    log.info { message(it) }
  }

  inline fun info(message: () -> Any?) {
    if (log.isInfoEnabled) log.info { message() }
  }

  inline fun <T> T.debug(message: (T) -> Any?) = also {
    log.debug { message(it) }
  }

  inline fun debug(message: () -> Any?) {
    if (log.isDebugEnabled) log.debug { message() }
  }

  inline fun trace(message: () -> Any?) {
    if (log.isTraceEnabled) log.trace { message() }
  }
}
