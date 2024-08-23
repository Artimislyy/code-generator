package com.lyy.web.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyy.web.annotation.AuthCheck;
import com.lyy.web.common.BaseResponse;
import com.lyy.web.common.DeleteRequest;
import com.lyy.web.common.ErrorCode;
import com.lyy.web.common.ResultUtils;
import com.lyy.web.constant.UserConstant;
import com.lyy.web.exception.BusinessException;
import com.lyy.web.exception.ThrowUtils;
import com.lyy.web.meta.Meta;
import com.lyy.web.model.dto.generator.GeneratorAddRequest;
import com.lyy.web.model.dto.generator.GeneratorEditRequest;
import com.lyy.web.model.dto.generator.GeneratorQueryRequest;
import com.lyy.web.model.dto.generator.GeneratorUpdateRequest;
import com.lyy.web.model.entity.Generator;
import com.lyy.web.model.entity.User;
import com.lyy.web.model.vo.GeneratorVO;
import com.lyy.web.service.GeneratorService;
import com.lyy.web.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/Artimislyy">lyy</a>
 */
@RestController
@RequestMapping("/generator")
@Slf4j
public class GeneratorController {

    @Resource
    private GeneratorService generatorService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建生成器
     *
     * @param generatorAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addGenerator(@RequestBody GeneratorAddRequest generatorAddRequest, HttpServletRequest request) {
        // 判断请求参数是否为空
        if (generatorAddRequest == null) {
            // 抛出参数错误异常
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 创建Generator对象
        Generator generator = new Generator();
        // 将请求参数复制到Generator对象中
        BeanUtils.copyProperties(generatorAddRequest, generator);
        // 获取请求参数中的tags
        List<String> tags = generatorAddRequest.getTags();
        // 将tags转换为JSON字符串并设置到Generator对象中
        generator.setTags(JSONUtil.toJsonStr(tags));
        // 获取请求参数中的fileConfig
        Meta.FileConfig fileConfig = generatorAddRequest.getFileConfig();
        // 将fileConfig转换为JSON字符串并设置到Generator对象中
        generator.setFileConfig(JSONUtil.toJsonStr(fileConfig));
        // 获取请求参数中的modelConfig
        Meta.ModelConfig modelConfig = generatorAddRequest.getModelConfig();
        // 将modelConfig转换为JSON字符串并设置到Generator对象中
        generator.setModelConfig(JSONUtil.toJsonStr(modelConfig));

        // 参数校验
        generatorService.validGenerator(generator, true);
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 设置Generator对象的userId为当前登录用户的id
        generator.setUserId(loginUser.getId());
        // 设置Generator对象的status为0
        generator.setStatus(0);
        // 保存Generator对象
        boolean result = generatorService.save(generator);
        // 判断保存是否成功，如果失败则抛出操作错误异常
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 获取新保存的Generator对象的id
        long newGeneratorId = generator.getId();
        // 返回成功结果，包含新保存的Generator对象的id
        return ResultUtils.success(newGeneratorId);
    }

    /**
     * 删除生成器
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteGenerator(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 判断请求参数是否为空或ID是否小于等于0
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前登录用户
        User user = userService.getLoginUser(request);
        // 获取请求参数中的ID
        long id = deleteRequest.getId();
        // 判断是否存在
        Generator oldGenerator = generatorService.getById(id);
        // 如果不存在，抛出异常
        ThrowUtils.throwIf(oldGenerator == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldGenerator.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 删除
        boolean b = generatorService.removeById(id);
        // 返回结果
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）生成器
     *
     * @param generatorUpdateRequest
     * @return
     */
    @PostMapping("/update")
    // 使用AuthCheck注解，表示该方法需要进行权限验证
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateGenerator(@RequestBody GeneratorUpdateRequest generatorUpdateRequest) {
        // 判断请求参数是否为空或ID是否小于等于0
        if (generatorUpdateRequest == null || generatorUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = new Generator();
        // 将请求参数复制到generator对象中
        BeanUtils.copyProperties(generatorUpdateRequest, generator);
        List<String> tags = generatorUpdateRequest.getTags();
        // 将tags转换为JSON字符串并设置到generator对象中
        generator.setTags(JSONUtil.toJsonStr(tags));
        Meta.FileConfig fileConfig = generatorUpdateRequest.getFileConfig();
        // 将fileConfig转换为JSON字符串并设置到generator对象中
        generator.setFileConfig(JSONUtil.toJsonStr(fileConfig));
        Meta.ModelConfig modelConfig = generatorUpdateRequest.getModelConfig();
        // 将modelConfig转换为JSON字符串并设置到generator对象中
        generator.setModelConfig(JSONUtil.toJsonStr(modelConfig));

        // 参数校验
        generatorService.validGenerator(generator, false);
        long id = generatorUpdateRequest.getId();
        // 判断是否存在
        Generator oldGenerator = generatorService.getById(id);
        ThrowUtils.throwIf(oldGenerator == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = generatorService.updateById(generator);
        // 返回更新结果
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取,代码生成器详情页

     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    // 根据id获取GeneratorVO对象
public BaseResponse<GeneratorVO> getGeneratorVOById(long id, HttpServletRequest request) {
        // 判断id是否小于等于0，如果是，则抛出参数错误异常
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 根据id获取Generator对象
        Generator generator = generatorService.getById(id);
        // 判断Generator对象是否为空，如果是，则抛出未找到异常
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 返回GeneratorVO对象
        return ResultUtils.success(generatorService.getGeneratorVO(generator, request));
    }

    /**
     * 分页获取列表（仅管理员），管理员页面,各种搜索
     *
     * @param generatorQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Generator>> listGeneratorByPage(@RequestBody GeneratorQueryRequest generatorQueryRequest) {
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                generatorService.getQueryWrapper(generatorQueryRequest));
        return ResultUtils.success(generatorPage);
    }

    /**
     * 分页获取列表（封装类），主页，各种搜索
     *
     * @param generatorQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<GeneratorVO>> listGeneratorVOByPage(@RequestBody GeneratorQueryRequest generatorQueryRequest,
                                                                 HttpServletRequest request) {
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                generatorService.getQueryWrapper(generatorQueryRequest));
        return ResultUtils.success(generatorService.getGeneratorVOPage(generatorPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param generatorQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<GeneratorVO>> listMyGeneratorVOByPage(@RequestBody GeneratorQueryRequest generatorQueryRequest,
                                                                   HttpServletRequest request) {
        if (generatorQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        generatorQueryRequest.setUserId(loginUser.getId());
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                generatorService.getQueryWrapper(generatorQueryRequest));
        return ResultUtils.success(generatorService.getGeneratorVOPage(generatorPage, request));
    }

    // endregion

    /**
     * 编辑（用户）生成器
     *
     * @param generatorEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editGenerator(@RequestBody GeneratorEditRequest generatorEditRequest, HttpServletRequest request) {
        // 判断请求参数是否为空或ID是否小于等于0
        if (generatorEditRequest == null || generatorEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = new Generator();
        // 将请求参数复制到generator对象中
        BeanUtils.copyProperties(generatorEditRequest, generator);
        List<String> tags = generatorEditRequest.getTags();
        // 将tags转换为JSON字符串并设置到generator对象中
        generator.setTags(JSONUtil.toJsonStr(tags));
        Meta.FileConfig fileConfig = generatorEditRequest.getFileConfig();
        // 将fileConfig转换为JSON字符串并设置到generator对象中
        generator.setFileConfig(JSONUtil.toJsonStr(fileConfig));
        Meta.ModelConfig modelConfig = generatorEditRequest.getModelConfig();
        // 将modelConfig转换为JSON字符串并设置到generator对象中
        generator.setModelConfig(JSONUtil.toJsonStr(modelConfig));

        // 参数校验
        generatorService.validGenerator(generator, false);
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        long id = generatorEditRequest.getId();
        // 判断是否存在
        Generator oldGenerator = generatorService.getById(id);
        ThrowUtils.throwIf(oldGenerator == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldGenerator.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 更新generator对象
        boolean result = generatorService.updateById(generator);
        // 返回更新结果
        return ResultUtils.success(result);
    }

}