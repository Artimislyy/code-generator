package com.lyy.maker.cli;



import com.lyy.maker.cli.command.GenerateCommand;
import com.lyy.maker.cli.command.ConfigCommand;
import com.lyy.maker.cli.command.ListCommand;
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
        return commandLine.execute(args);
    }
}
