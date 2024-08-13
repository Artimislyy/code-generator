package com.lyy.maker.filegenerator;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import com.lyy.maker.generator.DynamicGenerator;
import com.lyy.maker.meta.Meta;
import com.lyy.maker.meta.MetaManager;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public abstract class GenerateTemplate {
    public void doGenerate() throws TemplateException, IOException, InterruptedException {
        Meta meta = MetaManager.getMetaObject();//相当于命令行自己输入的参数 -l -o -a都是在这里解析的

        String originalPath = GenerateTemplate.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        // 输出根路径
        String projectPath = new File(originalPath).getParentFile().getParentFile().getAbsolutePath(); //code-generator\code-generator-maker
        String outputPath = projectPath + File.separator + "generated" + File.separator + meta.getName();//code-generator\code-generator-maker\generated\acm-template-pro-generator
        if (!FileUtil.exist(outputPath)) {
            FileUtil.mkdir(outputPath);
        }

        //1、复制原始模板文件
        String sourceCopyDestPath = copySource(meta, outputPath);//code-generator\code-generator-maker\generated\acm-template-pro-generator\.source

        //2、生成代码
        generateCode(meta, outputPath);

        //3、打包jar
        String jarPath = buildJar(meta, outputPath);//target/acm-template-pro-generator-1.0-jar-with-dependencies.jar

        //4.封装脚本
        String shellOutputFilePath = buildScript(outputPath, jarPath);

        //5、生成精简版
        buildDist(outputPath, jarPath, shellOutputFilePath, sourceCopyDestPath);
    }

    protected void buildDist(String outputPath, String jarPath, String shellOutputFilePath, String sourceCopyDestPath) {
        //生成精简版的程序（产物包）
        String distOutputPath = outputPath + "-dist";
        //拷贝jar包
        String targetAbsolutionPath = distOutputPath + File.separator + "target";
        FileUtil.mkdir(targetAbsolutionPath);
        String jarAbsolutionPath = outputPath + File.separator + jarPath;
        FileUtil.copy(jarAbsolutionPath, targetAbsolutionPath, true);
        //拷贝脚本
        FileUtil.copy(shellOutputFilePath, distOutputPath, true);
        FileUtil.copy(shellOutputFilePath + ".bat", distOutputPath, true);
        //拷贝源模板文件
        FileUtil.copy(sourceCopyDestPath, distOutputPath, true);
    }

    protected String buildScript(String outputPath, String jarPath) throws IOException {
        String shellOutputFilePath = outputPath + File.separator + "generator";//code-generator\code-generator-maker\generated\acm-template-pro-generator\generator
        ScriptGenerator.doGenerate(shellOutputFilePath, jarPath);//target/acm-template-pro-generator-1.0-jar-with-dependencies.jar
        return shellOutputFilePath;
    }

    protected String buildJar(Meta meta, String outputPath) throws IOException, InterruptedException {
        JarGenerator.doGenerate(outputPath);
        String jarName = String.format("%s-%s-jar-with-dependencies.jar", meta.getName(), meta.getVersion());//acm-template-pro-generator-1.0-jar-with-dependencies.jar
        String jarPath = "target/" + jarName;//target/acm-template-pro-generator-1.0-jar-with-dependencies.jar
        return jarPath;
    }

    protected void generateCode(Meta meta, String outputPath) throws IOException, TemplateException {
        // 读取 resources 目录
        ClassPathResource classPathResource = new ClassPathResource("");
        String inputResourcePath = classPathResource.getAbsolutePath();//code-generator-maker\src\main\resources

        // Java 包基础路径
        String outputBasePackage = meta.getBasePackage();//com.lyy
        String outputBasePackagePath = StrUtil.join("/", StrUtil.split(outputBasePackage, "."));//com/lyy
        String outputBaseJavaPackagePath = outputPath + File.separator + "src/main/java/" + outputBasePackagePath;//code-generator\code-generator-maker\generated\acm-template-pro-generator\src\main\java\com\lyy

        String inputFilePath;
        String outputFilePath;

        // model.DataModel
        inputFilePath = inputResourcePath + "templates/java/model/DataModel.java.ftl";//code-generator-maker\src\main\resources\templates\java\model\DataModel.java.ftl
        outputFilePath = outputBaseJavaPackagePath + "/model/DataModel.java";//code-generator\code-generator-maker\generated\acm-template-pro-generator\src\main\java\com\lyy\model\DataModel.java
        outputFilePath = outputFilePath.replace("\\", "/");
        DynamicGenerator.doGenerate(inputFilePath, outputFilePath, meta);


        // cli.command.ConfigCommand
        inputFilePath = inputResourcePath + "templates/java/cli/command/ConfigCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/command/ConfigCommand.java";
        outputFilePath = outputFilePath.replace("\\", "/");
        DynamicGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // cli.command.GenerateCommand
        inputFilePath = inputResourcePath + "templates/java/cli/command/GenerateCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/command/GenerateCommand.java";
        outputFilePath = outputFilePath.replace("\\", "/");
        DynamicGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // cli.command.ListCommand
        inputFilePath = inputResourcePath + "templates/java/cli/command/ListCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/command/ListCommand.java";
        outputFilePath = outputFilePath.replace("\\", "/");
        DynamicGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // cli.CommandExecutor
        inputFilePath = inputResourcePath + "templates/java/cli/CommandExecutor.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/CommandExecutor.java";
        outputFilePath = outputFilePath.replace("\\", "/");
        DynamicGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // generator.MainGenerator
        inputFilePath = inputResourcePath + "templates/java/generator/MainGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/generator/MainGenerator.java";
        outputFilePath = outputFilePath.replace("\\", "/");
        DynamicGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // Main
        inputFilePath = inputResourcePath + "templates/java/Main.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/Main.java";
        outputFilePath = outputFilePath.replace("\\", "/");
        DynamicGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // generator.DynamicGenerator
        inputFilePath = inputResourcePath + "templates/java/generator/DynamicGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/generator/DynamicGenerator.java";
        outputFilePath = outputFilePath.replace("\\", "/");
        DynamicGenerator.doGenerate(inputFilePath, outputFilePath, meta);


        // generator.StaticGenerator
        inputFilePath = inputResourcePath + "templates/java/generator/StaticGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/generator/StaticGenerator.java";
        outputFilePath = outputFilePath.replace("\\", "/");
        DynamicGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // pom.xml
        inputFilePath = inputResourcePath + "templates/pom.xml.ftl";
        outputFilePath = outputPath + File.separator + "pom.xml";
        outputFilePath = outputFilePath.replace("\\", "/");
        DynamicGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // README.md
        inputFilePath = inputResourcePath + "templates/README.md.ftl";
        outputFilePath = outputPath + File.separator + "README.md";
        outputFilePath = outputFilePath.replace("\\", "/");
        DynamicGenerator.doGenerate(inputFilePath, outputFilePath, meta);
    }

    protected String copySource(Meta meta, String outputPath) {
        String sourceRootPath = meta.getFileConfig().getSourceRootPath();
        String sourceCopyDestPath = outputPath + File.separator + ".source";//code-generator\code-generator-maker\generated\acm-template-pro-generator\.source
        FileUtil.copy(sourceRootPath, sourceCopyDestPath, false);
        return sourceCopyDestPath;
    }
}
