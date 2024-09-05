package io.github.eskibear.jetbrainscoderunner.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.plugins.terminal.TerminalToolWindowManager
import java.io.IOException
import java.util.*

class RunCodeAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        val virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE)

        if (project != null && virtualFile != null) {
            val getFileTypeId = getFileTypeId(virtualFile)
            val executor = executors[getFileTypeId]
            // not supported type
            if (executor == null) {
                Messages.showMessageDialog(
                    String.format(
                        "File type %s with extension .%s is not supported.",
                        virtualFile.fileType.name,
                        virtualFile.extension
                    ),
                    "Run Code",
                    Messages.getInformationIcon()
                )
                return
            }

            val command =
                if (executor.contains("$"))  // replace pre-defined vars: ${directory} ${fileNameWithoutExt} ${fileName}
                    executor.replace("\\$\\{fileNameWithoutExt}".toRegex(), virtualFile.nameWithoutExtension)
                        .replace("\\$\\{fileName}".toRegex(), virtualFile.name)
                        .replace("\\$\\{directory}".toRegex(), virtualFile.parent.path)
                else String.format("%s %s", executor, virtualFile.canonicalPath)

            val manager = TerminalToolWindowManager.getInstance(project)
            try {
                manager.createLocalShellWidget(
                    null,
                    "Code Runner",
                    true,
                    true
                )
                    .executeCommand(command)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }

    private fun getFileTypeId(virtualFile: VirtualFile): String {
        val fileTypeName = virtualFile.fileType.name
        val extension = virtualFile.extension ?: return fileTypeName

        return when (extension.lowercase(Locale.getDefault())) {
            "cjs", "mjs", "js" -> "javascript"
            "m" -> "objective-c"
            "py" -> "python"
            "pl" -> "perl"
            "p6" -> "perl6"
            "rb" -> "ruby"
            "ps1" -> "powershell"
            "sh" -> "shellscript"
            "fs" -> "fsharp"
            "cs" -> "csharp"
            "vbs" -> "vbscript"
            "coffee" -> "coffeescript"
            "jl" -> "julia"
            "cr" -> "crystal"
            "ml" -> "ocaml"
            "clj" -> "clojure"
            "hx" -> "haxe"
            "rs" -> "rust"
            "rkt" -> "racket"
            "scm" -> "scheme"
            "pas" -> "pascal"
            "hs" -> "haskell"
            "erl" -> "erlang"
            else -> extension
        }
    }

    companion object {
        private val executors: MutableMap<String, String> =
            HashMap()

        init {
            executors["javascript"] = "node"
            executors["java"] =
                "cd \${directory} && javac \${fileName} && java \${fileNameWithoutExt}"
            executors["c"] =
                "cd \${directory} && gcc \${fileName} -o \${fileNameWithoutExt} && \${directory}\${fileNameWithoutExt}"
            executors["zig"] = "zig run"
            executors["cpp"] =
                "cd \${directory} && g++ \${fileName} -o \${fileNameWithoutExt} && \${directory}\${fileNameWithoutExt}"
            executors["objective-c"] =
                "cd \${directory} && gcc -framework Cocoa \${fileName} -o \${fileNameWithoutExt} && \${directory}\${fileNameWithoutExt}"
            executors["php"] = "php"
            executors["python"] = "python -u"
            executors["perl"] = "perl"
            executors["perl6"] = "perl6"
            executors["ruby"] = "ruby"
            executors["go"] = "go run"
            executors["lua"] = "lua"
            executors["groovy"] = "groovy"
            executors["powershell"] = "powershell -ExecutionPolicy ByPass -File"
            executors["bat"] = "cmd /c"
            executors["shellscript"] = "bash"
            executors["fsharp"] = "fsi"
            executors["csharp"] = "scriptcs"
            executors["vbscript"] = "cscript //Nologo"
            executors["typescript"] = "ts-node"
            executors["coffeescript"] = "coffee"
            executors["scala"] = "scala"
            executors["swift"] = "swift"
            executors["julia"] = "julia"
            executors["crystal"] = "crystal"
            executors["ocaml"] = "ocaml"
            executors["r"] = "Rscript"
            executors["applescript"] = "osascript"
            executors["clojure"] = "lein exec"
            executors["haxe"] =
                "haxe --cwd \${directory}WithoutTrailingSlash --run \${fileNameWithoutExt}"
            executors["rust"] =
                "cd \${directory} && rustc \${fileName} && \${directory}\${fileNameWithoutExt}"
            executors["racket"] = "racket"
            executors["scheme"] = "csi -script"
            executors["ahk"] = "autohotkey"
            executors["autoit"] = "autoit3"
            executors["dart"] = "dart"
            executors["pascal"] =
                "cd \${directory} && fpc \${fileName} && \${directory}\${fileNameWithoutExt}"
            executors["d"] =
                "cd \${directory} && dmd \${fileName} && \${directory}\${fileNameWithoutExt}"
            executors["haskell"] = "runghc"
            executors["nim"] = "nim compile --verbosity:0 --hints:off --run"
            executors["lisp"] = "sbcl --script"
            executors["kit"] = "kitc --run"
            executors["v"] = "v run"
            executors["sass"] = "sass --style expanded"
            executors["scss"] = "scss --style expanded"
            executors["less"] =
                "cd \${directory} && lessc \${fileName} \${fileNameWithoutExt}.css"
            executors["FortranFreeForm"] =
                "cd \${directory} && gfortran \${fileName} -o \${fileNameWithoutExt} && \${directory}\${fileNameWithoutExt}"
            executors["fortran-modern"] =
                "cd \${directory} && gfortran \${fileName} -o \${fileNameWithoutExt} && \${directory}\${fileNameWithoutExt}"
            executors["fortran_fixed-form"] =
                "cd \${directory} && gfortran \${fileName} -o \${fileNameWithoutExt} && \${directory}\${fileNameWithoutExt}"
            executors["fortran"] =
                "cd \${directory} && gfortran \${fileName} -o \${fileNameWithoutExt} && \${directory}\${fileNameWithoutExt}"
            executors["sml"] = "cd \${directory} && sml \${fileName}"
            executors["mojo"] = "mojo run"
            executors["erlang"] = "escript"
            executors["spwn"] = "spwn build"
            executors["pkl"] =
                "cd \${directory} && pkl eval -f yaml \${fileName} -o \${fileNameWithoutExt}.yaml"
            executors["gleam"] = "gleam run -m \${fileNameWithoutExt}"
        }
    }
}
