package com.lyy.maker.filegenerator;

public class Generator extends GenerateTemplate {
    @Override
    protected void buildDist(String outputPath, String jarPath, String shellOutputFilePath, String sourceCopyDestPath) {
        System.out.println("不用精简");
    }
}
