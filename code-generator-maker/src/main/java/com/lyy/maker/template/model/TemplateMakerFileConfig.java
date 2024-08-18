package com.lyy.maker.template.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class TemplateMakerFileConfig {

    private List<FileInfoConfig> files;

    private FileGroupConfig fileGroupConfig;

    @NoArgsConstructor//自动生成一个无参构造函数
    @Data
    public static class FileInfoConfig {//每个文件的过滤规则

        private String path;

        private String condition;

        private List<FileFilterConfig> filterConfigList;//filters，一个文件有多个过滤规则

        @Data
        @Builder
        public static class FileFilterConfig {//每个文件的过滤规则
            /**
             * 过滤范围
             */
            private String range;

            /**
             * 过滤规则
             */
            private String rule;

            /**
             * 过滤值
             */
            private String value;
        }
    }


    @Data
    public static class FileGroupConfig {

        private String condition;

        private String groupKey;

        private String groupName;
    }
}
