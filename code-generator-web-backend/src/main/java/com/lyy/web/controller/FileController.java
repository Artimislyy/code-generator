package com.lyy.web.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.lyy.web.model.entity.Generator;
import com.lyy.web.service.GeneratorService;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.lyy.web.annotation.AuthCheck;
import com.lyy.web.common.BaseResponse;
import com.lyy.web.common.ErrorCode;
import com.lyy.web.common.ResultUtils;
import com.lyy.web.constant.FileConstant;
import com.lyy.web.constant.UserConstant;
import com.lyy.web.exception.BusinessException;
import com.lyy.web.manager.CosManager;
import com.lyy.web.model.dto.file.UploadFileRequest;
import com.lyy.web.model.entity.User;
import com.lyy.web.model.enums.FileUploadBizEnum;
import com.lyy.web.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * 文件接口
 *
 * @author <a href="https://github.com/Artimislyy">lyy</a>
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Resource
    private GeneratorService generatorService;

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile, UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        // 获取业务类型
        String biz = uploadFileRequest.getBiz();
        // 根据业务类型获取枚举
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        // 如果业务类型不存在，抛出参数错误异常
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 验证文件
        validFile(multipartFile, fileUploadBizEnum);
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 文件目录：根据业务、用户来划分
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        // 文件名：随机生成8位字母数字组合 + 原文件名
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        // 文件路径：/业务类型/用户ID/文件名
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            // 返回可访问地址
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            // 上传失败，抛出系统错误异常
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        // 判断文件上传业务类型
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            // 判断文件大小是否超过1M
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            // 判断文件类型是否为jpeg、jpg、svg、png、webp
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }

    /**
     * 根据 id 下载
     *
     * @param id
     * @return
     */
    @GetMapping("/download")
    public void downloadGeneratorById(long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 判断id是否小于等于0
        if (id <= 0) {
            // 抛出参数错误异常
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 根据id获取生成器
        Generator generator = generatorService.getById(id);
        // 判断生成器是否存在
        if (generator == null) {
            // 抛出未找到异常
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 获取生成器的产物包路径
        String filepath = generator.getDistPath();
        // 判断产物包路径是否为空
        if (StrUtil.isBlank(filepath)) {
            // 抛出未找到异常
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "产物包不存在");
        }

        // 追踪事件
        log.info("用户 {} 下载了 {}", loginUser, filepath);

        COSObjectInputStream cosObjectInput = null;
        try {
            // 从cos中获取对象
            COSObject cosObject = cosManager.getObject(filepath);
            // 获取对象内容
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            // 记录错误日志
            log.error("file download error, filepath = " + filepath, e);
            // 抛出系统错误异常
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            // 关闭输入流
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }


    /**
     * 测试文件上传
     *
     * @param multipartFile
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    //@RequestPart("file") : Spring MVC注解，表示这个参数是一个文件，并且文件在请求中的名称为file。
    //MultipartFile : 用于处理HTTP请求中的文件。
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        // 文件目录
        String filename = multipartFile.getOriginalFilename();
        String filepath = String.format("/test/%s", filename);
        File file = null;
        try {
            // 创建一个名称为***的临时文件file，默认情况下会被存储在系统的临时文件目录中，C:\Users\<username>\AppData\Local\Temp
            //File.createTempFile(String prefix, String suffix)，这个方法使用给定的前缀和后缀创建一个临时文件。前缀和后缀的长度必须至少为3个字符。如果前缀或后缀为null，则使用默认值"tmp"。
            file = File.createTempFile(filepath, null);//file的文件名称 ： /test/filename.tmp
            //将上传的文件内容写入临时文件。
            multipartFile.transferTo(file);
            //将临时文件上传到COS服务中。
            cosManager.putObject(filepath, file);
            // 返回可访问地址
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 测试文件下载
     *
     * @param filepath
     * @param response
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE) // 检查用户角色，必须为管理员
    @GetMapping("/test/download/") // 映射到/test/download/路径
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {
            //一个文件作为二进制流发送给客户端
            COSObject cosObject = cosManager.getObject(filepath); // 从cosManager中获取文件
            cosObjectInput = cosObject.getObjectContent(); // 获取文件流
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput); // 将文件流转换为字节数组
            // 设置响应头
            response.setContentType("application/octet-stream"); // 设置响应类型为二进制流
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath); // 设置响应头，告诉浏览器下载文件，并设置文件名
            // 写入响应
            response.getOutputStream().write(bytes); // 将文件写入响应流
            response.getOutputStream().flush(); // 刷新响应流
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e); // 记录错误日志
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败"); // 抛出业务异常
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close(); // 关闭文件流
            }
        }
    }
}
