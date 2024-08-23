package com.lyy.web.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.lyy.web.config.CosClientConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * Cos 对象存储操作
 *
 * @author <a href="https://github.com/Artimislyy">lyy</a>
 */
@Component
public class CosManager {

    // 注入CosClientConfig对象
    @Resource
    private CosClientConfig cosClientConfig;

    // 注入COSClient对象
    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key           唯一键
     * @param localFilePath 本地文件路径
     * @return
     */
    public PutObjectResult putObject(String key, String localFilePath) {
        // 创建PutObjectRequest对象,对象包含了上传文件所需的所有信息，包括存储桶名称、对象键和本地文件路径。
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, new File(localFilePath));
        // 上传对象,将PutObjectRequest对象中的文件上传到指定的存储桶中。
        //cosClient：这是一个已经初始化的COS客户端对象，用于与COS服务进行交互。
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     * @return
     */
    public PutObjectResult putObject(String key, File file) {//key就是存在对象存储器中的path
        // 创建PutObjectRequest对象
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        // 上传对象
        return cosClient.putObject(putObjectRequest);
    }
    /**
     * 下载对象
     *
     * @param key 唯一键
     * @return
     */
    // 获取指定key的对象
    public COSObject getObject(String key) {//path
            // 创建GetObjectRequest对象，指定bucket和key,定要获取的存储桶名称、对象键等信息。
            GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
            // 调用cosClient的getObject方法，获取指定key的对象
            return cosClient.getObject(getObjectRequest);
    }
}