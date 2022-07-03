package com.lowgular.intellij.plugin.intellijplugin.actions

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.diagnostic.Attachment
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.text.StringUtil
import com.lowgular.intellij.plugin.intellijplugin.lang.LowgularLanguage
import org.codehaus.jettison.json.JSONObject
import org.codehaus.jettison.json.JSONTokener
import java.lang.ProcessBuilder.Redirect
import java.util.concurrent.TimeUnit
import java.io.File

private val LOG: Logger = Logger.getInstance("#org.lowgular")
private var myLogErrors: ThreadLocal<Boolean> = ThreadLocal.withInitial { true }

private fun shortenOutput(output: String): String {
  return StringUtil.shortenTextWithEllipsis(
    output.replace('\\', '/')
      .replace("(/[^()/:]+)+(/[^()/:]+)(/[^()/:]+)".toRegex(), "/...$1$2$3"),
    750, 0)
}

fun makeCommand(pluginDir: String): GeneralCommandLine {
  val utilityDirectory = JSLanguageServiceUtil.getPluginDirectory(LowgularLanguage::class.java, "cli")
  LOG.warn(utilityDirectory.absolutePath);
  val moduleExe = "node"
  val commandLine = GeneralCommandLine(moduleExe, "${utilityDirectory}${File.separator}api-client.cli.js", "{\n" +
    "    smartComponent: 'Component',\n" +
    "    resolver: 'Resolver',\n" +
    "    directive: 'Directive',\n" +
    "    guard: 'Guard',\n" +
    "    eventHandler: 'EventHandler',\n" +
    "    dto: 'DTO',\n" +
    "    dtoPort: 'DtoPort',\n" +
    "    command: 'Command',\n" +
    "    commandPort: 'CommandPort',\n" +
    "    query: 'Query',\n" +
    "    queryPort: 'QueryPort',\n" +
    "    context: 'Context',\n" +
    "    contextPort: 'ContextPort',\n" +
    "    dtoService: 'Service',\n" +
    "    responseObject: 'Response',\n" +
    "    storage: 'Storage',\n" +
    "    state: 'State',\n" +
    "    page: 'Page',\n" +
    "  }")

  return commandLine
}

fun grabCommandOutput(commandLine: GeneralCommandLine): String {
//  val moduleExe = "$modulePath${File.separator}bin${File.separator}ng

//  if (filePath != null) {
//    commandLine.withWorkDirectory(filePath)
//  }
  val handler = CapturingProcessHandler(commandLine)
  val output = handler.runProcess()

  if (output.exitCode == 0) {
    if (output.stderr.trim().isNotEmpty()) {
      if (myLogErrors.get()) {
        LOG.error("Error while loading schematics info.\n"
          + shortenOutput(output.stderr),
          Attachment("err-output", output.stderr)
        )
      }
      else {
        LOG.info("Error while loading schematics info.\n"
          + shortenOutput(output.stderr))
      }
    }
    return output.stdout
  }
  else if (myLogErrors.get()) {
    LOG.error("Failed to load schematics info.\n"
      + shortenOutput(output.stderr),
      Attachment("err-output", output.stderr),
      Attachment("std-output", output.stdout)
    )
  }
  else {
    LOG.info("Error while loading schematics info.\n"
      + shortenOutput(output.stderr))
  }
  return ""
}
class CreateDTO : DumbAwareAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val file = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
    LOG.warn("${project.basePath} or ${project.projectFilePath}")
//    val files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
    val txt = Messages.showInputDialog(
      project, "What is the DTO name?",
      "What is the DTO name?", Messages.getQuestionIcon()
    )

//    val result = "node /home/ortho/Desktop/projects/gradzio/nx-test/test.js".runCommand()
    val result = grabCommandOutput(makeCommand("test"));
    val data = JSONTokener(result).nextValue() as JSONObject;
    if (data.getString("status") == "error") {
      Messages.showErrorDialog(project, data.getString("message"), "Error");
    }
    if (data.getString("status") == "ok") {
      Messages.showMessageDialog(project, data.getString("message"), "Done", Messages.getInformationIcon())
    }
  }
}
