package com.lyy.maker.meta;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;


/**
 * 将meta.json 文件中的内容加载到Meta对象中
 */
public class MetaManager {

    //保证多线程环境下的内存可见性，meta一旦被修改，其他线程会立即获取到修改后的值
    private static volatile Meta meta;

    private MetaManager() {
        // 私有构造函数，防止外部实例化,被坏单例模式
    }

    //双检锁单例模式，保证对象性能不会被锁影响，并且保证对象只被创建一次
    public static Meta getMetaObject() {
        if (meta == null) {
            synchronized (MetaManager.class) {
                if (meta == null) {
                    meta = initMeta();
                }
            }
        }
        return meta;
    }

    private static Meta initMeta() {
        //读取名为 meta.json 的文件，并将其内容以 UTF-8 编码转换为字符串。
        String metaJson = ResourceUtil.readUtf8Str("meta.json");
        //将 JSON 字符串 metaJson 转换为 Meta 类型的对象。
        Meta newMeta = JSONUtil.toBean(metaJson, Meta.class);
        Meta.FileConfig fileConfig = newMeta.getFileConfig();
        // todo 校验和处理默认值
        return newMeta;
    }
}
