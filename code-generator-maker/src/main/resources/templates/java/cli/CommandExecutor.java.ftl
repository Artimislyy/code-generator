package ${basePackage}.cli;


import ${basePackage}.cli.command.ConfigCommand;
import ${basePackage}.cli.command.GenerateCommand;
import ${basePackage}.cli.command.ListCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "${name}",subcommands = {ConfigCommand.class, GenerateCommand.class, ListCommand.class},mixinStandardHelpOptions = true,description = "Command executor")
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
