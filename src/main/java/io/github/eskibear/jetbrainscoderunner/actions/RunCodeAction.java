package io.github.eskibear.jetbrainscoderunner.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.terminal.TerminalToolWindowManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class RunCodeAction extends AnAction {

    final private static Map<String, String> executors;

    static {
        executors = new HashMap<>();
        executors.put("javascript", "node");
        executors.put("java", "cd ${directory} && javac ${fileName} && java ${fileNameWithoutExt}");
        executors.put("c", "cd ${directory} && gcc ${fileName} -o ${fileNameWithoutExt} && ${directory}${fileNameWithoutExt}");
        executors.put("zig", "zig run");
        executors.put("cpp", "cd ${directory} && g++ ${fileName} -o ${fileNameWithoutExt} && ${directory}${fileNameWithoutExt}");
        executors.put("objective-c", "cd ${directory} && gcc -framework Cocoa ${fileName} -o ${fileNameWithoutExt} && ${directory}${fileNameWithoutExt}");
        executors.put("php", "php");
        executors.put("python", "python -u");
        executors.put("perl", "perl");
        executors.put("perl6", "perl6");
        executors.put("ruby", "ruby");
        executors.put("go", "go run");
        executors.put("lua", "lua");
        executors.put("groovy", "groovy");
        executors.put("powershell", "powershell -ExecutionPolicy ByPass -File");
        executors.put("bat", "cmd /c");
        executors.put("shellscript", "bash");
        executors.put("fsharp", "fsi");
        executors.put("csharp", "scriptcs");
        executors.put("vbscript", "cscript //Nologo");
        executors.put("typescript", "ts-node");
        executors.put("coffeescript", "coffee");
        executors.put("scala", "scala");
        executors.put("swift", "swift");
        executors.put("julia", "julia");
        executors.put("crystal", "crystal");
        executors.put("ocaml", "ocaml");
        executors.put("r", "Rscript");
        executors.put("applescript", "osascript");
        executors.put("clojure", "lein exec");
        executors.put("haxe", "haxe --cwd ${directory}WithoutTrailingSlash --run ${fileNameWithoutExt}");
        executors.put("rust", "cd ${directory} && rustc ${fileName} && ${directory}${fileNameWithoutExt}");
        executors.put("racket", "racket");
        executors.put("scheme", "csi -script");
        executors.put("ahk", "autohotkey");
        executors.put("autoit", "autoit3");
        executors.put("dart", "dart");
        executors.put("pascal", "cd ${directory} && fpc ${fileName} && ${directory}${fileNameWithoutExt}");
        executors.put("d", "cd ${directory} && dmd ${fileName} && ${directory}${fileNameWithoutExt}");
        executors.put("haskell", "runghc");
        executors.put("nim", "nim compile --verbosity:0 --hints:off --run");
        executors.put("lisp", "sbcl --script");
        executors.put("kit", "kitc --run");
        executors.put("v", "v run");
        executors.put("sass", "sass --style expanded");
        executors.put("scss", "scss --style expanded");
        executors.put("less", "cd ${directory} && lessc ${fileName} ${fileNameWithoutExt}.css");
        executors.put("FortranFreeForm", "cd ${directory} && gfortran ${fileName} -o ${fileNameWithoutExt} && ${directory}${fileNameWithoutExt}");
        executors.put("fortran-modern", "cd ${directory} && gfortran ${fileName} -o ${fileNameWithoutExt} && ${directory}${fileNameWithoutExt}");
        executors.put("fortran_fixed-form", "cd ${directory} && gfortran ${fileName} -o ${fileNameWithoutExt} && ${directory}${fileNameWithoutExt}");
        executors.put("fortran", "cd ${directory} && gfortran ${fileName} -o ${fileNameWithoutExt} && ${directory}${fileNameWithoutExt}");
        executors.put("sml", "cd ${directory} && sml ${fileName}");
        executors.put("mojo", "mojo run");
        executors.put("erlang", "escript");
        executors.put("spwn", "spwn build");
        executors.put("pkl", "cd ${directory} && pkl eval -f yaml ${fileName} -o ${fileNameWithoutExt}.yaml");
        executors.put("gleam", "gleam run -m ${fileNameWithoutExt}");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);

        if (project != null && virtualFile != null) {
            String getFileTypeId = getFileTypeId(virtualFile);
            String executor = executors.get(getFileTypeId);
            // not supported type
            if (executor == null) {
                Messages.showMessageDialog(
                        String.format("File type %s with extension .%s is not supported.",
                                virtualFile.getFileType().getName(),
                                virtualFile.getExtension()
                        ),
                        "Run Code",
                        Messages.getInformationIcon()
                );
                return;
            }

            String command = executor.contains("$") ?
                    // replace pre-defined vars: ${directory} ${fileNameWithoutExt} ${fileName}
                    executor.replaceAll("\\$\\{fileNameWithoutExt}", virtualFile.getNameWithoutExtension())
                            .replaceAll("\\$\\{fileName}", virtualFile.getName())
                            .replaceAll("\\$\\{directory}", virtualFile.getParent().getPath())
                    :
                    String.format("%s %s", executor, virtualFile.getCanonicalPath());

            final TerminalToolWindowManager manager = TerminalToolWindowManager.getInstance(project);
            try {
                manager.createLocalShellWidget(
                            null,
                            "Code Runner",
                            true,
                            true)
                        .executeCommand(command);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String getFileTypeId(VirtualFile virtualFile) {
        String fileTypeName = virtualFile.getFileType().getName();
        String extension = virtualFile.getExtension();

        if (extension == null) {
            return fileTypeName;
        }

        return switch (extension.toLowerCase()) {
            case "cjs", "mjs", "js" -> "javascript";
            case "m" -> "objective-c";
            case "py" -> "python";
            case "pl" -> "perl";
            case "p6" -> "perl6";
            case "rb" -> "ruby";
            case "ps1" -> "powershell";
            case "sh" -> "shellscript";
            case "fs" -> "fsharp";
            case "cs" -> "csharp";
            case "vbs" -> "vbscript";
            case "coffee" -> "coffeescript";
            case "jl" -> "julia";
            case "cr" -> "crystal";
            case "ml" -> "ocaml";
            case "clj" -> "clojure";
            case "hx" -> "haxe";
            case "rs" -> "rust";
            case "rkt" -> "racket";
            case "scm" -> "scheme";
            case "pas" -> "pascal";
            case "hs" -> "haskell";
            case "erl" -> "erlang";
            default -> extension;
        };
    }
}
