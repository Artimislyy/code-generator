# ${name}

> ${description}
>
> 作者：${author}
>
> [code-generator](https://github.com/Artimislyy/code-generator) 制作，感谢您的使用！

可以通过命令行交互式输入的方式动态生成想要的项目代码

## 使用说明

执行项目根目录下的脚本文件：

```
generator
<命令>
    <选项参数>
        ```

        示例命令：

        ```
        generator
        generate <#list modelConfig.models as modelInfo><#if modelInfo.abbr??>-${modelInfo.abbr} </#if></#list>
        ```

        ## 参数说明
        <#-- 初始化一个全局索引变量 -->
        <#assign index = 0 />

        <#list modelConfig.models as modelInfo>
        <#if modelInfo.groupKey??>
        <#-- 如果有分组模型，先处理分组 -->
            ${modelInfo.groupName}：
            <#list modelInfo.models as subModelInfo>

                ${subModelInfo?index + 1}）${subModelInfo.fieldName}
                类型：${subModelInfo.type}

                描述：${subModelInfo.description!"无描述"}

                默认值：${subModelInfo.defaultValue?c}

            </#list>
        <#else>
            ${modelInfo?index + 1}）${modelInfo.fieldName}
            类型：${modelInfo.type}

            描述：${modelInfo.description!"无描述"}

            默认值：${modelInfo.defaultValue?c}

        </#if>
</#list>