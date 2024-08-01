package com.lyy.generator;

import cn.hutool.core.io.FileUtil;
import com.lyy.model.DataModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;

/**
 * 动态文件生成
 */

public class DynamicGenerator {

    public static void main(String[] args) throws IOException, TemplateException {
        String projectPath = System.getProperty("user.dir")+File.separator+"code-generator-basic";
        String inputPath = projectPath + File.separator + "src/main/resources/templates/MainTemplate.java.ftl";
        String outputPath = projectPath + File.separator + "MainTemplate.java";
        DataModel dataModel = new DataModel();
        dataModel.setAuthor("lyy");
        dataModel.setLoop(false);
        dataModel.setOutputText("求和结果：");
        doGenerate(inputPath, outputPath, dataModel);
    }

    /**
     * 生成文件
     *
     * @param inputPath 模板文件输入路径
     * @param outputPath 输出路径
     * @param model 数据模型
     * @throws IOException
     * @throws TemplateException
     */
    public static void doGenerate(String inputPath, String outputPath, Object model) throws IOException, TemplateException {
        // new 出 Configuration 对象，参数为 FreeMarker 版本号
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);

        // 指定模板文件所在的路径
        File templateDir = new File(inputPath).getParentFile();
        configuration.setDirectoryForTemplateLoading(templateDir);

        // 设置模板文件使用的字符集
        configuration.setDefaultEncoding("utf-8");

        // 创建模板对象，加载指定模板
        String templateName = new File(inputPath).getName();
        Template template = configuration.getTemplate(templateName);

        if (!FileUtil.exist(outputPath)) {
            FileUtil.touch(outputPath);
        }

        // 生成
//        Writer out = new FileWriter(outputPath);
        Writer out = new OutputStreamWriter(new FileOutputStream(new File(outputPath)), "UTF-8");

        //把model中的数据传给已经挖好坑的ftl模板
        template.process(model, out);

        // 生成文件后别忘了关闭哦
        out.close();
    }
}
