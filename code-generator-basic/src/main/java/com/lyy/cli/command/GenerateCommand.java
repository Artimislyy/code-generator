package com.lyy.cli.command;

import cn.hutool.core.bean.BeanUtil;
import com.lyy.generator.MainGenerator;
import com.lyy.model.DataModel;
import lombok.Data;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

/**
 * 生成文件
 */
@Command(name = "generate", description = "生成代码", mixinStandardHelpOptions = true)
@Data
public class GenerateCommand implements Callable<Integer> {

    //从命令中解析选项和参数，并填充到对象的属性中
    @Option(names = {"-l", "--loop"}, arity = "0..1", description = "是否循环(True/False)", interactive = true, echo = true)
    private boolean loop=false;

    @Option(names = {"-a", "--author"}, arity = "0..1", description = "作者", interactive = true, echo = true)
    private String author = "lyy";

    @Option(names = {"-o", "--outputText"}, arity = "0..1", description = "输出文本", interactive = true, echo = true)
    private String outputText = "sum = ";

    public Integer call() throws Exception {
        DataModel dataModel = new DataModel();
        //把解析出来的选项和参数，复制给数据模型
        BeanUtil.copyProperties(this, dataModel);
        System.out.println("配置信息：" + dataModel);
        //传给MainGenerator，生成动态代码
        MainGenerator.doGenerate(dataModel);
        return 0;
    }
}
