package com.lyy.maker.generator;

import com.lyy.maker.model.DataModel;
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

        String inputRootPath = "D:/java_code/code-generator/code-generator-demo-projects/acm-template-pro";
        String outputRootPath = "D:/java_code/code-generator/generated";

        //动态路径
        String inputDynamicFilePath = new File(inputRootPath, "src/com/lyy/acm/MainTemplate.java.ftl").getAbsolutePath().replace("\\", "/");
        String outputDynamicFilePath =new File(outputRootPath, "src/com/lyy/acm/MainTemplate.java").getAbsolutePath().replace("\\", "/");
        DynamicGenerator.doGenerate(inputDynamicFilePath, outputDynamicFilePath, model);


        // 静态路径
        String inputStaticPath = new File(inputRootPath, ".gitignore").getAbsolutePath().replace("\\", "/");
        String outputStaticPath = new File(outputRootPath, ".gitignore").getAbsolutePath().replace("\\", "/");
        // 生成静态文件
        StaticGenerator.copyFilesByHutool(inputStaticPath, outputStaticPath);

        inputStaticPath = new File(inputRootPath, "README.md").getAbsolutePath().replace("\\", "/");
        outputStaticPath = new File(outputRootPath, "README.md").getAbsolutePath().replace("\\", "/");
        // 生成静态文件
        StaticGenerator.copyFilesByHutool(inputStaticPath, outputStaticPath);
    }

    public static void main(String[] args) throws TemplateException, IOException {
        DataModel dataModel = new DataModel();
        dataModel.setAuthor("lyy");
        dataModel.setLoop(false);
        dataModel.setOutputText("求和结果：");
        doGenerate(dataModel);
    }
}