package com.lyy;

import com.lyy.cli.CommandExecutor;

public class Main {
    public static void main(String[] args) {

        CommandExecutor commandExecutor = new CommandExecutor();
        commandExecutor.doExecute(args);
    }
}