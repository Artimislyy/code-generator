package com.lyy.cli;


import com.lyy.cli.command.ConfigCommand;
import com.lyy.cli.command.GenerateCommand;
import com.lyy.cli.command.ListCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "executor",subcommands = {ConfigCommand.class, GenerateCommand.class, ListCommand.class},mixinStandardHelpOptions = true,description = "Command executor")
public class CommandExecutor implements Runnable{
    @Override
    public void run() {
        System.out.println("请输入具体命令，或者输入 --help 查看命令提示");
    }
    public Integer doExecute(String[] args) {
        CommandLine commandLine = new CommandLine(this);
        //commandLine.execute(args);处理用户输入的命令，剩下的就交给picocli框架来解析命令并执行业务逻辑
        return commandLine.execute(args);
    }
}
