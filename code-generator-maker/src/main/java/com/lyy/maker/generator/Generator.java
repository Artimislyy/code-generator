package com.lyy.maker.generator;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import com.lyy.maker.meta.Meta;
import com.lyy.maker.meta.MetaManager;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public class Generator {
    public static void main(String[] args) throws TemplateException, IOException, InterruptedException {
        Meta meta = MetaManager.getMetaObject();

        // 输出根路径
        String projectPath = System.getProperty("user.dir") + File.separator + "code-generator-maker";
        String outputPath = projectPath + File.separator + "generated" + File.separator + meta.getName();

//        // 读取 resources 目录
//        ClassPathResource classPathResource = new ClassPathResource("");
//        String inputResourcePath = classPathResource.getAbsolutePath();
//
//        // Java 包基础路径
//        String outputBasePackage = meta.getBasePackage();
//        String outputBasePackagePath = StrUtil.join("/", StrUtil.split(outputBasePackage, "."));
//        String outputBaseJavaPackagePath = outputPath + File.separator + "src/main/java/" + outputBasePackagePath;
//
//        String inputFilePath;
//        String outputFilePath;
//
//        // model.DataModel
//        inputFilePath = inputResourcePath  + "templates/java/model/DataModel.java.ftl";
//        outputFilePath = outputBaseJavaPackagePath + "/model/DataModel.java";
//        outputFilePath = outputFilePath.replace("\\", "/");
//        DynamicGenerator.doGenerate(inputFilePath , outputFilePath, meta);
//
//        // cli.command.ConfigCommand
//        inputFilePath = inputResourcePath  + "templates/java/cli/command/ConfigCommand.java.ftl";
//        outputFilePath = outputBaseJavaPackagePath + "/cli/command/ConfigCommand.java";
//        outputFilePath = outputFilePath.replace("\\", "/");
//        DynamicGenerator.doGenerate(inputFilePath , outputFilePath, meta);
//
//        // cli.command.GenerateCommand
//        inputFilePath = inputResourcePath +  "templates/java/cli/command/GenerateCommand.java.ftl";
//        outputFilePath = outputBaseJavaPackagePath + "/cli/command/GenerateCommand.java";
//        outputFilePath = outputFilePath.replace("\\", "/");
//        DynamicGenerator.doGenerate(inputFilePath , outputFilePath, meta);
//
//        // cli.command.ListCommand
//        inputFilePath = inputResourcePath +  "templates/java/cli/command/ListCommand.java.ftl";
//        outputFilePath = outputBaseJavaPackagePath + "/cli/command/ListCommand.java";
//        outputFilePath = outputFilePath.replace("\\", "/");
//        DynamicGenerator.doGenerate(inputFilePath , outputFilePath, meta);
//
//        // cli.CommandExecutor
//        inputFilePath = inputResourcePath +  "templates/java/cli/CommandExecutor.java.ftl";
//        outputFilePath = outputBaseJavaPackagePath + "/cli/CommandExecutor.java";
//        outputFilePath = outputFilePath.replace("\\", "/");
//        DynamicGenerator.doGenerate(inputFilePath , outputFilePath, meta);
//
//        // Main
//        inputFilePath = inputResourcePath + "templates/java/Main.java.ftl";
//        outputFilePath = outputBaseJavaPackagePath + "/Main.java";
//        outputFilePath = outputFilePath.replace("\\", "/");
//        DynamicGenerator.doGenerate(inputFilePath , outputFilePath, meta);
//
//        // generator.DynamicGenerator
//        inputFilePath = inputResourcePath + "templates/java/generator/DynamicGenerator.java.ftl";
//        outputFilePath = outputBaseJavaPackagePath + "/generator/DynamicGenerator.java";
//        outputFilePath = outputFilePath.replace("\\", "/");
//        DynamicGenerator.doGenerate(inputFilePath , outputFilePath, meta);
//
//        // generator.MainGenerator
//        inputFilePath = inputResourcePath + "templates/java/generator/MainGenerator.java.ftl";
//        outputFilePath = outputBaseJavaPackagePath + "/generator/MainGenerator.java";
//        outputFilePath = outputFilePath.replace("\\", "/");
//        DynamicGenerator.doGenerate(inputFilePath , outputFilePath, meta);
//
//        // generator.StaticGenerator
//        inputFilePath = inputResourcePath + "templates/java/generator/StaticGenerator.java.ftl";
//        outputFilePath = outputBaseJavaPackagePath + "/generator/StaticGenerator.java";
//        outputFilePath = outputFilePath.replace("\\", "/");
//        DynamicGenerator.doGenerate(inputFilePath , outputFilePath, meta);
//
//        inputFilePath = inputResourcePath + "templates/pom.xml.ftl";
//        outputFilePath = outputPath + File.separator + "pom.xml";
//        outputFilePath = outputFilePath.replace("\\", "/");
//        DynamicGenerator.doGenerate(inputFilePath , outputFilePath, meta);
//
//        //构建 jar 包
//        JarGenerator.doGenerate(outputPath);

        // 封装脚本
        String shellOutputFilePath = outputPath + File.separator + "generator";
        String jarName = String.format("%s-%s-jar-with-dependencies.jar", meta.getName(), meta.getVersion());
        String jarPath = "target/" + jarName;
        ScriptGenerator.doGenerate(shellOutputFilePath, jarPath);
    }
}
