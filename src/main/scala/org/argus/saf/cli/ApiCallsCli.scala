package org.argus.saf.cli

import java.io.File

import org.argus.amandroid.alir.componentSummary.ApkYard
import org.argus.amandroid.core.{AndroidGlobalConfig, ApkGlobal}
import org.argus.amandroid.core.decompile.{DecompileLayout, DecompileStrategy, DecompilerSettings}
import org.argus.amandroid.core.util.ApkFileUtil
import org.argus.amandroid.plugin.APICalls.APICalls
import org.argus.amandroid.plugin.ApiMisuseModules
import org.argus.jawa.core.io.{FileReporter, MsgLevel, NoReporter, PrintReporter}
import org.argus.jawa.core.util.{FileResourceUri, FileUtil, MSet, msetEmpty}
import org.argus.jawa.flow.Context
import org.argus.saf.cli.ApiMisuse.{apiMisuse, getOutputDirUri}

object ApiCallsCli {

  def apply(debug: Boolean, sourcePath: String, outputPath: String, forceDelete: Boolean, guessPackage: Boolean, args : String) {
    val apkFileUris: MSet[FileResourceUri] = msetEmpty
    val fileOrDir = new File(sourcePath)
    fileOrDir match {
      case dir if dir.isDirectory =>
        apkFileUris ++= ApkFileUtil.getApks(FileUtil.toUri(dir))
      case file =>
        if(ApkGlobal.isValidApk(FileUtil.toUri(file)))
          apkFileUris += FileUtil.toUri(file)
        else println(file + " is not decompilable.")
    }

    apiCalls(apkFileUris.toSet, outputPath, debug, forceDelete, guessPackage,args)
  }





  def apiCalls(apkFileUris: Set[FileResourceUri], outputPath: String, debug: Boolean, forceDelete: Boolean, guessPackage: Boolean,args : String): Unit = {
    Context.init_context_length(AndroidGlobalConfig.settings.k_context)

    println("Total apks: " + apkFileUris.size)

    try {
      var i: Int = 0
      apkFileUris.foreach { fileUri =>
        i += 1
        try {
          println("Analyzing #" + i + ":" + fileUri)
          val reporter =
            if (debug) new PrintReporter(MsgLevel.INFO)
            else new NoReporter
          val yard = new ApkYard(reporter)
          val outputUri = FileUtil.toUri(outputPath)
          val layout = DecompileLayout(outputUri)
          val strategy = DecompileStrategy(layout)
          val settings = DecompilerSettings(debugMode = false, forceDelete = forceDelete, strategy, reporter)
          val apk = yard.loadApk(fileUri, settings, collectInfo = false, resolveCallBack = false)
          val programmarmethods = new APICalls()
          programmarmethods.getProgrammarDefinedMethods(apk,apk.model.getAppName,args, None)
        }
      }
    }
  }

}
