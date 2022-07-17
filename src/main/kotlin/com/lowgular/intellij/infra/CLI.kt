package com.lowgular.intellij.infra

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.diagnostic.Attachment
import org.codehaus.jettison.json.JSONObject
import org.codehaus.jettison.json.JSONTokener


fun makeNodeCommand(cliFilePath: String, action: String, params: JSONObject, userId: String): GeneralCommandLine {
  val moduleExe = "node"
  val context = JSONObject()
    .put("modules", JSONObject()
      .put("dto", JSONObject()
        .put("suffix", "DTO")
        .put("folder", "application/ports/secondary/dto/\$name\$"))
      .put("dto-port", JSONObject()
        .put("suffix", "DtoPort")
        .put("folder", "application/ports/secondary/dto/\$name\$"))
      .put("dto-service", JSONObject()
        .put("suffix", "Service")
        .put("folder", "adapters/secondary/services/\$name\$"))
      .put("smart-component", JSONObject()
        .put("suffix", "Component")
        .put("folder", "adapters/primary/components/\$name\$"))
    )
    .put("workspace", JSONObject()
      .put("rootPath", "/home/ortho/Desktop/projects/gradzio/nx-test")
      .put("tsConfigPath", "tsconfig.base.json")
      .put("configPath", "angular.json"))
    .put("userContext", JSONObject().put("id", userId))
  val commandLine = GeneralCommandLine(
    moduleExe, cliFilePath, action, params.toString(), context.toString()
  )

  return commandLine
}

fun grabCommandOutput(commandLine: GeneralCommandLine): JSONObject {
  LOG.warn("Running command ${commandLine.toString()}")
  val handler = CapturingProcessHandler(commandLine)
  val output = handler.runProcess()
  LOG.warn(output.toString())
  if (output.exitCode == 0) {
    if (output.stderr.trim().isNotEmpty()) {
      val data = JSONTokener(output.stderr).nextValue() as JSONObject
      LOG.error(
        "Command Error: ${data.getString("message")}\n"
          + shortenOutput(output.stderr),
        Attachment("err-output", output.stderr)
      )
      return data
    }
    val data = JSONTokener(output.stdout).nextValue() as JSONObject
    LOG.warn("Command success: ${data.getString("message")}")
    return data
  }

  throw Exception("Invalid exit code")
}
