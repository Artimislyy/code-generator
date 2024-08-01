package ${basePackage}.generator;



import ${basePackage}.Main;
import ${basePackage}.model.DataModel;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 * 核心生成器
 */
public class MainGenerator {

    /**
     * 生成
     *
     * @param model 数据模型
     * @throws TemplateException
     * @throws IOException
     *
     */

    public static void doGenerate(Object model) throws TemplateException, IOException {

        String inputRootPath = "${fileConfig.inputRootPath}";
        String outputRootPath = "${fileConfig.outputRootPath}";

        String inputPath;
        String outputPath;
<#list fileConfig.files as fileInfo>

        inputPath = new File(inputRootPath, "${fileInfo.inputPath}").getAbsolutePath().replace("\\", "/");;
        outputPath = new File(outputRootPath, "${fileInfo.outputPath}").getAbsolutePath().replace("\\", "/");;
    <#if fileInfo.generateType == "dynamic">
        // 生成动态文件
        DynamicGenerator.doGenerate(inputPath, outputPath, model);

    <#else>
        // 生成静态文件
        StaticGenerator.copyFilesByHutool(inputPath, outputPath);
    </#if>
</#list>
    }
}