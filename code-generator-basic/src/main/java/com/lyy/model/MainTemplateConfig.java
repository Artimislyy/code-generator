package com.lyy.model;

import lombok.Data;

/**
 * 模板配置对象，接受传递给模板的参数，比hashmap更清晰、更规范
 */
@Data
public class MainTemplateConfig {
    /**
     * 是否生成循环
     */
    private boolean loop = false;

    /**
     * 作者注释
     */
    private String author = "lyy";

    /**
     * 输出信息
     */
    private String outputText = "sum = ";
}
