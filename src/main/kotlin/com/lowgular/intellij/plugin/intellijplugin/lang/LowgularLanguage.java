package com.lowgular.intellij.plugin.intellijplugin.lang;

import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.JSLanguageDialect;

public class LowgularLanguage extends JSLanguageDialect {
  public static final LowgularLanguage INSTANCE = new LowgularLanguage();

  protected LowgularLanguage() {
    super("Lowgular", DialectOptionHolder.OTHER);
  }

}
