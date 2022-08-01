package com.lowgular.intellij

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

class LowgularState {
  var accessToken = ""
  var refreshToken = ""
  var userId = ""
}

@State(name = "LowgularSettings", storages = [Storage("lowgular.xml")])
class LowgularSettings(val project: Project) : PersistentStateComponent<LowgularState> {
  companion object {
    fun getService(project: Project): LowgularSettings {
      return project.getService(LowgularSettings::class.java)
    }
  }

  private var state = LowgularState()

  override fun getState(): LowgularState {
    return state
  }

  override fun loadState(state: LowgularState) {
    this.state = state
  }

  fun setAccessToken(token: String) {
    this.state.accessToken = token
  }

  fun setRefreshToken(token: String) {
    this.state.refreshToken = token
  }

  fun setUserId(userId: String) {
    this.state.userId = userId
  }
}
