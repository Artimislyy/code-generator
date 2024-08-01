package ${basePackage}.cli.command;

import cn.hutool.core.io.FileUtil;
import picocli.CommandLine.Command;

import java.io.File;
import java.util.List;

/**
 * 原始文件列表信息
 */
@Command(name = "list", description = "查看文件列表", mixinStandardHelpOptions = true)
public class ListCommand implements Runnable {

    public void run() {
        String projectPath = "${fileConfig.inputRootPath}";
        List<File> files = FileUtil.loopFiles(projectPath);
        for (File file : files) {
            System.out.println(file);
        }
    }

}

