package com.lyy.maker.filegenerator;

import java.io.*;

public class JarGenerator {
    public static void doGenerate(String projectDir) throws IOException, InterruptedException {
        // 清理之前的构建并打包
        // 注意不同操作系统，执行的命令不同
        // 定义要执行的命令
        String winMavenCommand = "mvn.cmd clean package -DskipTests=true";
        String otherMavenCommand = "mvn clean package -DskipTests=true";
        String mavenCommand = winMavenCommand;

        // 这里一定要拆分！
        // ProcessBuilder用于构建和启动操作系统进程
        ProcessBuilder processBuilder = new ProcessBuilder(mavenCommand.split(" "));
        processBuilder.directory(new File(projectDir));
        Process process = processBuilder.start();

//        // 读取命令的输出
        InputStream inputStream = process.getInputStream();
        //inputStream是字节流，InputStreamReader 是一个桥梁，将字节流转换为字符流,这里使用BufferedReader来读取字符流
        //BufferedReader 提供了缓冲功能，以提高读取字符、数组和行的效率,通过使用缓冲区，减少了读取的次数，从而提高了性能。
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        // 等待命令执行完成
        int exitCode = process.waitFor();
        System.out.println("命令执行结束，退出码：" + exitCode);
    }
}
