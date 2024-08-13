package com.lyy.maker.template;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.lyy.maker.filegenerator.GenerateTemplate;
import com.lyy.maker.meta.Meta;
import com.lyy.maker.meta.Meta.FileConfig.FileInfo;
import com.lyy.maker.meta.enums.FileGenerateTypeEnum;
import com.lyy.maker.meta.enums.FileTypeEnum;
import com.lyy.maker.template.enums.FileFilterRangeEnum;
import com.lyy.maker.template.enums.FileFilterRuleEnum;
import com.lyy.maker.template.model.TemplateMakerFileConfig;
import com.lyy.maker.template.model.TemplateMakerFileConfig.FileInfoConfig.FileFilterConfig;
import com.lyy.maker.template.model.TemplateMakerModelConfig;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


public class TemplateMaker {

    /**
     * 制作模板
     *
     * @param newMeta
     * @param originProjectPath
     * @param templateMakerFileConfig
     * @param templateMakerModelConfig
     * @param id
     * @return
     */
    //originProjectPath =  D:\java_code\code-generator/code-generator-demo-projects/springboot-init
    public static long makeTemplate(Meta newMeta, String originProjectPath, TemplateMakerFileConfig templateMakerFileConfig, TemplateMakerModelConfig templateMakerModelConfig, Long id) {
        // 没有 id 则生成
        if (id == null) {
            id = IdUtil.getSnowflakeNextId();
        }

        // 需要复制去的目录
        String currentFilePath = GenerateTemplate.class.getProtectionDomain().getCodeSource().getLocation().getPath();// /D:/java_code/code-generator/code-generator-maker/target/classes/
        String projectPath = new File(currentFilePath).getParentFile().getParentFile().getAbsolutePath();// D:\java_code\code-generator\code-generator-maker

        String tempDirPath = projectPath + File.separator + ".temp";
        String templatePath = tempDirPath + File.separator + id;// D:\java_code\code-generator\code-generator-maker\.temp\1

        // 是否为首次制作模板
        // 目录不存在，则是首次制作
        if (!FileUtil.exist(templatePath)) {
            FileUtil.mkdir(templatePath);
            FileUtil.copy(originProjectPath, templatePath, true);
        }

        // 一、输入信息
        // 需要复制去的目录
        // D:\java_code\code-generator\code-generator-maker\.temp\1\springboot-init
        String sourceRootPath = templatePath + File.separator + FileUtil.getLastPathEle(Paths.get(originProjectPath)).toString();
        List<TemplateMakerFileConfig.FileInfoConfig> fileConfigInfoList = templateMakerFileConfig.getFiles();//配一个文件，包含path,过滤规则

        // 二、生成文件模板,处理文件信息newFileInfoList,同时获得每个文件的fileInfo,然后加入总的newFileInfoList
        // 遍历输入文件
        List<FileInfo> newFileInfoList = new ArrayList<>();
        for (TemplateMakerFileConfig.FileInfoConfig fileInfoConfig : fileConfigInfoList) {
            String inputFilePath = fileInfoConfig.getPath();

            // 如果填的是相对路径，要改为绝对路径
            if (!inputFilePath.startsWith(sourceRootPath)) {
                inputFilePath = sourceRootPath + File.separator + inputFilePath;
            }

            // 获取过滤后的文件列表（不会存在目录）
            List<File> fileList = FileFilter.doFilter(inputFilePath, fileInfoConfig.getFilterConfigList());
            for (File file : fileList) {
                FileInfo fileInfo = makeFileTemplate(templateMakerModelConfig, sourceRootPath, file);
                newFileInfoList.add(fileInfo);
            }
        }

        // 如果是文件组
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = templateMakerFileConfig.getFileGroupConfig();
        if (fileGroupConfig != null) {
            String condition = fileGroupConfig.getCondition();
            String groupKey = fileGroupConfig.getGroupKey();
            String groupName = fileGroupConfig.getGroupName();

            // 新增分组配置
            FileInfo groupFileInfo = new FileInfo();
            groupFileInfo.setType(FileTypeEnum.GROUP.getValue());
            groupFileInfo.setCondition(condition);
            groupFileInfo.setGroupKey(groupKey);
            groupFileInfo.setGroupName(groupName);
            // 文件全放到一个分组内
            groupFileInfo.setFiles(newFileInfoList);
            newFileInfoList = new ArrayList<>();
            newFileInfoList.add(groupFileInfo);
        }

        // 处理模型信息
        List<TemplateMakerModelConfig.ModelInfoConfig> models = templateMakerModelConfig.getModels();
        // - TemplateMakerModelConfig.ModelInfoConfig 转换为 Meta接受的 ModelInfo 对象
        List<Meta.ModelConfig.ModelInfo> inputModelInfoList = models.stream().map(modelInfoConfig -> {
            Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
            BeanUtil.copyProperties(modelInfoConfig, modelInfo);
            return modelInfo;
        }).collect(Collectors.toList());

        // - 本次新增的模型配置列表
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>();

        // - 如果是模型组
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        if (modelGroupConfig != null) {
            String condition = modelGroupConfig.getCondition();
            String groupKey = modelGroupConfig.getGroupKey();
            String groupName = modelGroupConfig.getGroupName();
            Meta.ModelConfig.ModelInfo groupModelInfo = new Meta.ModelConfig.ModelInfo();
            groupModelInfo.setGroupKey(groupKey);
            groupModelInfo.setGroupName(groupName);
            groupModelInfo.setCondition(condition);

            // 模型全放到一个分组内
            groupModelInfo.setModels(inputModelInfoList);
            newModelInfoList.add(groupModelInfo);
        } else {
            // 不分组，添加所有的模型信息到列表
            newModelInfoList.addAll(inputModelInfoList);
        }

        // 三、生成配置文件
        // D:\java_code\code-generator\code-generator-maker\.temp\1\springboot-init\meta.json
        String metaOutputPath = sourceRootPath + File.separator + "meta.json";

        // 如果已有 meta 文件，说明不是第一次制作，则在 meta 基础上进行修改
        if (FileUtil.exist(metaOutputPath)) {
            Meta oldMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaOutputPath), Meta.class);//从指定的路径metaOutputPath中读取一个UTF-8编码的字符串,将这个JSON字符串转换成Meta类型的Java对象。
            BeanUtil.copyProperties(newMeta, oldMeta, CopyOptions.create().ignoreNullValue());//将newMeta对象中的属性复制到oldMeta对象中。忽略newMeta中那些值为null的属性
            newMeta = oldMeta;

            // 1. 追加配置参数
            List<FileInfo> fileInfoList = newMeta.getFileConfig().getFiles();
            fileInfoList.addAll(newFileInfoList);
            List<Meta.ModelConfig.ModelInfo> modelInfoList = newMeta.getModelConfig().getModels();
            modelInfoList.addAll(newModelInfoList);

            // 配置去重
            newMeta.getFileConfig().setFiles(distinctFiles(fileInfoList));
            newMeta.getModelConfig().setModels(distinctModels(modelInfoList));
        } else {
            // 1. 构造配置参数
            Meta.FileConfig fileConfig = new Meta.FileConfig();
            newMeta.setFileConfig(fileConfig);
            sourceRootPath = sourceRootPath.replace("\\", "/");
            fileConfig.setSourceRootPath(sourceRootPath);
            List<FileInfo> fileInfoList = new ArrayList<>();
            fileConfig.setFiles(fileInfoList);
            fileInfoList.addAll(newFileInfoList);

            Meta.ModelConfig modelConfig = new Meta.ModelConfig();
            newMeta.setModelConfig(modelConfig);
            List<Meta.ModelConfig.ModelInfo> modelInfoList = new ArrayList<>();
            modelConfig.setModels(modelInfoList);
            modelInfoList.addAll(newModelInfoList);
        }

        // 2. 输出元信息文件
        FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(newMeta), metaOutputPath);
        return id;
    }

    /**
     * 制作文件模板
     *
     * @param templateMakerModelConfig
     * @param sourceRootPath
     * @param inputFile
     * @return
     */
    //sourceRootPath = D:\java_code\code-generator\code-generator-maker\.temp\1\springboot-init
    //inputFile =
    private static FileInfo makeFileTemplate(TemplateMakerModelConfig templateMakerModelConfig, String sourceRootPath, File inputFile) {
        // 获取输入文件路径
        String fileInputPath = inputFile.getAbsolutePath().replace(sourceRootPath + "\\", "");
        // 获取输出文件路径
        String fileOutputPath = fileInputPath + ".ftl";




        // 使用字符串替换，生成模板文件
        String fileInputAbsolutePath = inputFile.getAbsolutePath();
        String fileOutputAbsolutePath = inputFile.getAbsolutePath() + ".ftl";

        String fileContent;
        // 如果已有模板文件，说明不是第一次制作，则在模板基础上再次挖坑
        if (FileUtil.exist(fileOutputAbsolutePath)) {
            fileContent = FileUtil.readUtf8String(fileOutputAbsolutePath);
        } else {
            fileContent = FileUtil.readUtf8String(fileInputAbsolutePath);
        }

        // 支持多个模型：对同一个文件的内容，遍历模型进行多轮替换
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        String newFileContent = fileContent;
        String replacement;
        for (TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig : templateMakerModelConfig.getModels()) {
            // 不是分组
            if (modelGroupConfig == null) {
                replacement = String.format("${%s}", modelInfoConfig.getFieldName());
            } else {
                // 是分组
                String groupKey = modelGroupConfig.getGroupKey();
                // 注意挖坑要多一个层级
                replacement = String.format("${%s.%s}", groupKey, modelInfoConfig.getFieldName());
            }
            // 多次替换
            newFileContent = StrUtil.replace(newFileContent, modelInfoConfig.getReplaceText(), replacement);
        }

        // 文件配置信息

        fileInputPath = fileInputPath.replace("\\","/");
        fileOutputPath =fileOutputPath.replace("\\","/");
        FileInfo fileInfo = new FileInfo();
        fileInfo.setInputPath(fileInputPath);
        fileInfo.setOutputPath(fileOutputPath);
        fileInfo.setType(FileTypeEnum.FILE.getValue());



        // 和原文件一致，没有挖坑，则为静态生成
        if (newFileContent.equals(fileContent)) {
            // 输出路径 = 输入路径
            fileInfo.setOutputPath(fileInputPath);
            fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
        } else {
            // 生成模板文件
            fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());
            FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
        }
        return fileInfo;
    }

    public static void main(String[] args) {
        // 创建Meta对象
        Meta meta = new Meta();
        // 设置Meta对象的名称
        meta.setName("springboot-init");
        // 设置Meta对象的描述
        meta.setDescription("springboot-init模板生成器");

        // 获取当前文件路径
        String currentFilePath = GenerateTemplate.class.getProtectionDomain().getCodeSource().getLocation().getPath();// /D:/java_code/code-generator/code-generator-maker/target/classes/
        // 获取项目路径
        String projectPath = new File(currentFilePath).getParentFile().getParentFile().getAbsolutePath();// D:\java_code\code-generator\code-generator-maker

        // 原始模板位置 inputFilePath
        // 获取原始项目路径
        String originProjectPath = new File(projectPath).getParent() + "/code-generator-demo-projects/springboot-init";// D:\java_code\code-generator/code-generator-demo-projects/springboot-init
        // 将路径中的反斜杠替换为正斜杠
        originProjectPath = originProjectPath.replace("\\", "/");

        //------设置输入文件路径----------------
        String inputFilePath1 = "src/main/java/com/lyy/springbootinit/common";
        String inputFilePath2 = "src/main/resources/application.yml";
        //----------------------------------

        // 文件过滤
        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        //---------设置第一组过滤-------------------------
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        // 设置文件路径
        fileInfoConfig1.setPath(inputFilePath1);
        // 创建文件过滤配置列表
        List<FileFilterConfig> fileFilterConfigList = new ArrayList<>();
        // 创建文件过滤配置
        FileFilterConfig fileFilterConfig = FileFilterConfig.builder()
                // 设置过滤范围
                .range(FileFilterRangeEnum.FILE_NAME.getValue())
                // 设置过滤规则
                .rule(FileFilterRuleEnum.CONTAINS.getValue())
                // 设置过滤值
                .value("Base")
                // 构建文件过滤配置
                .build();
        // 将文件过滤配置添加到文件过滤配置列表中
        fileFilterConfigList.add(fileFilterConfig);
        // 将文件过滤配置列表添加到文件信息配置中
        fileInfoConfig1.setFilterConfigList(fileFilterConfigList);
        //-----------设置第二组过滤------------------------
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig2 = new TemplateMakerFileConfig.FileInfoConfig();
        // 设置文件路径
        fileInfoConfig2.setPath(inputFilePath2);
        // 将文件信息配置添加到模板生成器文件配置中
        templateMakerFileConfig.setFiles(Arrays.asList(fileInfoConfig1, fileInfoConfig2));

        // 分组配置
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = new TemplateMakerFileConfig.FileGroupConfig();
        // 设置分组条件
        fileGroupConfig.setCondition("outputText");
        // 设置分组关键字
        fileGroupConfig.setGroupKey("test");
        // 设置分组名称
        fileGroupConfig.setGroupName("测试分组");
        // 将分组配置添加到模板生成器文件配置中
        templateMakerFileConfig.setFileGroupConfig(fileGroupConfig);

        // 模型参数配置
        TemplateMakerModelConfig templateMakerModelConfig = new TemplateMakerModelConfig();

        // - 模型组配置
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = new TemplateMakerModelConfig.ModelGroupConfig();
        // 设置模型组的关键字
        modelGroupConfig.setGroupKey("mysql");
        // 设置模型组的名称
        modelGroupConfig.setGroupName("数据库配置");
        // 将模型组配置添加到模板生成器模型配置中
        templateMakerModelConfig.setModelGroupConfig(modelGroupConfig);

        // - 模型配置
        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig1 = new TemplateMakerModelConfig.ModelInfoConfig();
        // 设置模型字段的名称
        modelInfoConfig1.setFieldName("url");//设置模板的时候用的$url$
        // 设置模型字段的类型
        modelInfoConfig1.setType("String");
        // 设置模型字段的默认值
        modelInfoConfig1.setDefaultValue("jdbc:mysql://localhost:3306/my_db");//如果没有replaceText，则使用默认值
        // 设置模型字段的替换文本
        modelInfoConfig1.setReplaceText("jdbc:mysql://localhost:3306/my_db");//替换变量,searchStr,也就是要把jdbc:mysql://localhost:3306/my_db替换成FiledName:url

        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig2 = new TemplateMakerModelConfig.ModelInfoConfig();
        // 设置模型字段的名称
        modelInfoConfig2.setFieldName("username");
        // 设置模型字段的类型
        modelInfoConfig2.setType("String");
        // 设置模型字段的默认值
        modelInfoConfig2.setDefaultValue("root");
        // 设置模型字段的替换文本
        modelInfoConfig2.setReplaceText("root");

        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig3 = new TemplateMakerModelConfig.ModelInfoConfig();
        // 设置模型字段的名称
        modelInfoConfig3.setFieldName("BaseNew");
        // 设置模型字段的类型
        modelInfoConfig3.setType("String");
        // 设置模型字段的默认值
        modelInfoConfig3.setDefaultValue("Base");
        // 设置模型字段的替换文本
        modelInfoConfig3.setReplaceText("Base");

        // 将模型配置添加到模型配置列表中
        List<TemplateMakerModelConfig.ModelInfoConfig> modelInfoConfigList = Arrays.asList(modelInfoConfig1, modelInfoConfig2,modelInfoConfig3);
        // 将模型配置列表添加到模板生成器模型配置中
        templateMakerModelConfig.setModels(modelInfoConfigList);

        // 替换变量（首次）
        //String searchStr = "Sum: ";
        // 替换变量（第二次）
        String searchStr = "BaseResponse";
        //originProjectPath =  D:\java_code\code-generator/code-generator-demo-projects/springboot-init
        // 调用makeTemplate方法生成模板
        long id = makeTemplate(meta, originProjectPath, templateMakerFileConfig, templateMakerModelConfig, 1735281524670181380L);
        // 打印生成的模板ID
        System.out.println(id);
    }

    /**
     * 模型去重
     *
     * @param modelInfoList
     * @return
     */
    private static List<Meta.ModelConfig.ModelInfo> distinctModels(List<Meta.ModelConfig.ModelInfo> modelInfoList) {
        // 策略：同分组内模型 merge，不同分组保留

        // 1. 有分组的，以组为单位划分
        Map<String, List<Meta.ModelConfig.ModelInfo>> groupKeyModelInfoListMap = modelInfoList
                .stream()
                .filter(modelInfo -> StrUtil.isNotBlank(modelInfo.getGroupKey()))
                .collect(
                        Collectors.groupingBy(Meta.ModelConfig.ModelInfo::getGroupKey)
                );


        // 2. 同组内的模型配置合并        // 保存每个组对应的合并后的对象 map
        Map<String, Meta.ModelConfig.ModelInfo> groupKeyMergedModelInfoMap = new HashMap<>();
        for (Map.Entry<String, List<Meta.ModelConfig.ModelInfo>> entry : groupKeyModelInfoListMap.entrySet()) {
            List<Meta.ModelConfig.ModelInfo> tempModelInfoList = entry.getValue();
            List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>(tempModelInfoList.stream()
                    .flatMap(modelInfo -> modelInfo.getModels().stream())
                    .collect(
                            Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r)
                    ).values());

            // 使用新的 group 配置
            Meta.ModelConfig.ModelInfo newModelInfo = CollUtil.getLast(tempModelInfoList);
            newModelInfo.setModels(newModelInfoList);
            String groupKey = entry.getKey();
            groupKeyMergedModelInfoMap.put(groupKey, newModelInfo);
        }

        // 3. 将模型分组添加到结果列表
        List<Meta.ModelConfig.ModelInfo> resultList = new ArrayList<>(groupKeyMergedModelInfoMap.values());

        // 4. 将未分组的模型添加到结果列表
        List<Meta.ModelConfig.ModelInfo> noGroupModelInfoList = modelInfoList.stream().filter(modelInfo -> StrUtil.isBlank(modelInfo.getGroupKey()))
                .collect(Collectors.toList());
        resultList.addAll(new ArrayList<>(noGroupModelInfoList.stream()
                .collect(
                        Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r)
                ).values()));
        return resultList;
    }

    /**
     * 文件去重
     *
     * @param fileInfoList
     * @return
     */
    private static List<FileInfo> distinctFiles(List<FileInfo> fileInfoList) {
        // 策略：同分组内文件 merge，不同分组保留

        // 1. 有分组的，以组为单位划分
        // 过滤掉 groupKey 为空白的 FileInfo 对象，确保只有 groupKey 不为空的对象参与后续的分组操作。
        //素按 groupKey 进行分组。
        Map<String, List<FileInfo>> groupKeyFileInfoListMap = fileInfoList
                .stream()
                .filter(fileInfo -> StrUtil.isNotBlank(fileInfo.getGroupKey()))
                .collect(
                        Collectors.groupingBy(FileInfo::getGroupKey)
                );


        // 2. 同组内的文件配置合并
        // 保存每个组对应的合并后的对象 map
        Map<String, FileInfo> groupKeyMergedFileInfoMap = new HashMap<>();
        for (Map.Entry<String, List<FileInfo>> entry : groupKeyFileInfoListMap.entrySet()) {
            List<FileInfo> tempFileInfoList = entry.getValue();
            //每个 FileInfo 中的 files 列表展开成一个流。也就是说，把 List<List<File>> 展平为 List<File>。
            List<FileInfo> newFileInfoList = new ArrayList<>(tempFileInfoList.stream()
                    .flatMap(fileInfo -> fileInfo.getFiles().stream())
                    .collect(
                            Collectors.toMap(FileInfo::getInputPath, o -> o, (e, r) -> r)
                    ).values());

            // 使用新的 group 配置
            FileInfo newFileInfo = CollUtil.getLast(tempFileInfoList);
            newFileInfo.setFiles(newFileInfoList);
            String groupKey = entry.getKey();
            groupKeyMergedFileInfoMap.put(groupKey, newFileInfo);
        }

        // 3. 将文件分组添加到结果列表
        List<FileInfo> resultList = new ArrayList<>(groupKeyMergedFileInfoMap.values());

        // 4. 将未分组的文件添加到结果列表
        List<FileInfo> noGroupFileInfoList = fileInfoList.stream().filter(fileInfo -> StrUtil.isBlank(fileInfo.getGroupKey()))
                .collect(Collectors.toList());
        resultList.addAll(new ArrayList<>(noGroupFileInfoList.stream()
                .collect(
                        Collectors.toMap(FileInfo::getInputPath, o -> o, (e, r) -> r)
                ).values()));
        return resultList;
    }
}