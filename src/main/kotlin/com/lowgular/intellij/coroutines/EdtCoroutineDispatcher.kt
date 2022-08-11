package com.lowgular.intellij.coroutines

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.impl.contextModality
import com.intellij.util.ui.EDT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.job
import kotlin.coroutines.CoroutineContext

val Dispatchers.EDT: CoroutineContext get() = EdtCoroutineDispatcher

internal sealed class EdtCoroutineDispatcher : MainCoroutineDispatcher() {

  override val immediate: MainCoroutineDispatcher get() = Immediate

  override fun dispatch(context: CoroutineContext, block: Runnable) {
    val state = context.contextModality()
    val runnable = if (state === ModalityState.any()) {
      block
    }
    else {
      DispatchedRunnable(context.job, block)
    }
    ApplicationManager.getApplication().invokeLater(runnable, state)
  }

  companion object : EdtCoroutineDispatcher() {

    override fun toString() = "EDT"
  }

  object Immediate : EdtCoroutineDispatcher() {

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
      if (!EDT.isCurrentThreadEdt()) {
        return true
      }
      // The current coroutine is executed with the correct modality state
      // (the execution would be postponed otherwise)
      // => there is no need to check modality state here.
      return false
    }

    override fun toString() = "EDT.immediate"
  }
}