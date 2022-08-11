package com.lowgular.intellij.infra.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.Messages
import com.lowgular.intellij.coroutines.EDT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.codehaus.jettison.json.JSONArray

suspend fun makeMultiOptionModal(message: String, title: String, data: JSONArray, key: String): Int {
    val list: MutableList<String> = ArrayList()
    val numPorts = data.length()
    for (i in 0 until numPorts) {
        list.add(data.getJSONObject(i).getString(key))
    }

    return withContext(Dispatchers.EDT) {
        Messages.showDialog(
            message,
            title, list.toTypedArray(), 0,
            Messages.getQuestionIcon()
        )
    }
}

suspend fun showInputDialog(
    project: Project?,
    message: String,
    title: String,
    initialValue: String? = null,
    validator: InputValidator? = null
): String? =
    withContext(Dispatchers.EDT) {
        Messages.showInputDialog(project, message, title, Messages.getQuestionIcon(), initialValue, validator)
    }

suspend fun showMessageDialog(
    project: Project?,
    message: String,
    title: String
) {
    withContext(Dispatchers.EDT) {
        Messages.showMessageDialog(project, message, title, Messages.getInformationIcon())
    }
}