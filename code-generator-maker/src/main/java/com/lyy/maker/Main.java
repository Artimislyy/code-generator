package com.lyy.maker;


import com.lyy.maker.filegenerator.Generator;
import freemarker.template.TemplateException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws TemplateException, IOException, InterruptedException {
        Generator generator = new Generator();
        generator.doGenerate();
    }
}






