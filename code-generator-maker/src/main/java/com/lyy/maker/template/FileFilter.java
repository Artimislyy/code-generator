package com.lyy.maker.template;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.lyy.maker.template.enums.FileFilterRangeEnum;
import com.lyy.maker.template.enums.FileFilterRuleEnum;
import com.lyy.maker.template.model.TemplateMakerFileConfig.FileInfoConfig.FileFilterConfig;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件过滤器
 */
public class FileFilter {

    /**
     * 对某个文件或目录进行过滤，返回文件列表
     *
     * @param filePath
     * @param fileFilterConfigList
     * @return
     */
    public static List<File> doFilter(String filePath, List<FileFilterConfig> fileFilterConfigList) {
        // 根据路径获取所有文件
        List<File> fileList = FileUtil.loopFiles(filePath);
        return fileList.stream()
                .filter(file -> doSingleFileFilter(fileFilterConfigList, file))
                .collect(Collectors.toList());
    }

    /**
     * 单个文件过滤
     *
     * @param fileFilterConfigList 过滤规则
     * @param file                 单个文件
     * @return 是否保留
     */
    public static boolean doSingleFileFilter(List<FileFilterConfig> fileFilterConfigList, File file) {
        //获得文件名和文件内容
        String fileName = file.getName();
        String fileContent = FileUtil.readUtf8String(file);

        // 所有过滤器校验结束的结果
        boolean result = true;

        //如果过滤规则为空，则不过滤
        if (CollUtil.isEmpty(fileFilterConfigList)) {
            return true;
        }

        //遍历当前文件的所有过滤规则
        for (FileFilterConfig fileFilterConfig : fileFilterConfigList) {
            //获得过滤规则的范围、规则、值
            String range = fileFilterConfig.getRange();
            String rule = fileFilterConfig.getRule();
            String value = fileFilterConfig.getValue();

            FileFilterRangeEnum fileFilterRangeEnum = FileFilterRangeEnum.getEnumByValue(range);
            if (fileFilterRangeEnum == null) {
                continue;
            }

            // 要过滤的原内容
            String content = null;
            switch (fileFilterRangeEnum) {
                case FILE_NAME:
                    content = fileName;
                    break;
                case FILE_CONTENT:
                    content = fileContent;
                    break;
                default:
            }

            FileFilterRuleEnum filterRuleEnum = FileFilterRuleEnum.getEnumByValue(rule);
            if (filterRuleEnum == null) {
                continue;
            }
            switch (filterRuleEnum) {
                case CONTAINS:
                    result = content.contains(value);
                    break;
                case STARTS_WITH:
                    result = content.startsWith(value);
                    break;
                case ENDS_WITH:
                    result = content.endsWith(value);
                    break;
                case REGEX:
                    result = content.matches(value);
                    break;
                case EQUALS:
                    result = content.equals(value);
                    break;
                default:
            }

            // 有一个不满足，就直接返回
            if (!result) {
                return false;
            }
        }

        // 都满足
        return true;
    }
}