package com.lyy.maker.template;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.lyy.maker.template.model.TemplateMakerConfig;
import junit.framework.TestCase;
import org.junit.Test;

public class TemplateMakerTest extends TestCase {

    /**
     * 使用 JSON 制作模板
     */
    @Test
    public void testmakeSpringBootTemplate() {

        // 定义根路径
        String rootPath ="example/springboot-init/";
        // 读取templateMaker.json文件内容
        String configStr = ResourceUtil.readUtf8Str(rootPath + "templateMaker.json");
        // 将json字符串转换为TemplateMakerConfig对象
        TemplateMakerConfig templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        // 根据templateMakerConfig对象生成模板
        long id = TemplateMaker.makeTemplate(templateMakerConfig);

        // 读取templateMaker1.json文件，并将其转换为TemplateMakerConfig对象
        configStr = ResourceUtil.readUtf8Str(rootPath + "templateMaker1.json");
        // 将configStr转换为TemplateMakerConfig对象
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        // 使用TemplateMakerConfig对象生成模板
        TemplateMaker.makeTemplate(templateMakerConfig);

        //todo : 把condition变为false，应该是什么结果
        configStr = ResourceUtil.readUtf8Str(rootPath + "templateMaker2.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath + "templateMaker3.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath + "templateMaker4.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath + "templateMaker5.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath + "templateMaker6.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath + "templateMaker7.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath + "templateMaker8.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);


        System.out.println(id);
    }
}