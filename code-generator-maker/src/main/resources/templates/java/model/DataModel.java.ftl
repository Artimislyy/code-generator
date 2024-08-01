package ${basePackage}.model;

import lombok.Data;

/**
 * 数据模型，模板配置对象，接受传递给模板的参数，比hashmap更清晰、更规范
 */
@Data
public class DataModel {

<#list modelConfig.models as modelInfo>
    <#if modelInfo.description??>
     /**
     * ${modelInfo.description}
     */
    </#if>
    private ${modelInfo.type} ${modelInfo.fieldName}<#if modelInfo.defaultValue??> =${modelInfo.defaultValue?c}</#if>;
</#list>
}
