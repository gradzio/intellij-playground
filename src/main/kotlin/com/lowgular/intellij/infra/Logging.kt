package com.lowgular.intellij.infra

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.text.StringUtil

val LOG: Logger = Logger.getInstance("#org.lowgular")
var myLogErrors: ThreadLocal<Boolean> = ThreadLocal.withInitial { true }

fun shortenOutput(output: String): String {
  return StringUtil.shortenTextWithEllipsis(
    output.replace('\\', '/')
      .replace("(/[^()/:]+)+(/[^()/:]+)(/[^()/:]+)".toRegex(), "/...$1$2$3"),
    750, 0
  )
}
