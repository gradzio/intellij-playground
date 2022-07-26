package com.lowgular.intellij.infra.ui

import com.intellij.openapi.ui.Messages
import org.codehaus.jettison.json.JSONArray

fun makeMultiOptionModal(message: String, title: String, data: JSONArray, key: String): Int {
  val list: MutableList<String> = ArrayList()
  val numPorts = data.length()
  for (i in 0 until numPorts) {
    list.add(data.getJSONObject(i).getString(key))
  }

  return Messages.showDialog(
    message,
    title, list.toTypedArray(), 0,
    Messages.getQuestionIcon()
  )
}
