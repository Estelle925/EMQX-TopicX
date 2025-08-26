package com.emqx.topichub.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.emqx.topichub.common.Result;
import com.emqx.topichub.dto.*;
import com.emqx.topichub.service.PayloadTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Payload模板管理控制器
 * 提供Payload模板管理相关的API接口
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/payload-templates")
@RequiredArgsConstructor
public class PayloadTemplateController {

    private final PayloadTemplateService payloadTemplateService;

    /**
     * 分页搜索Payload模板列表
     *
     * @param request 搜索请求参数
     * @return 分页Payload模板列表
     */
    @PostMapping("/search")
    public Result<IPage<PayloadTemplateDTO>> searchTemplates(@RequestBody PayloadTemplateSearchRequest request) {
        try {
            IPage<PayloadTemplateDTO> result = payloadTemplateService.searchTemplates(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("搜索模板失败: {}", e.getMessage());
            return Result.error("搜索模板失败");
        }
    }

    /**
     * 根据ID获取Payload模板详情
     *
     * @param id 模板ID
     * @return 模板详情
     */
    @GetMapping("/{id}")
    public Result<PayloadTemplateDTO> getTemplateById(@PathVariable("id") Long id) {
        try {
            PayloadTemplateDTO result = payloadTemplateService.getTemplateDtoById(id);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建Payload模板
     *
     * @param request 创建请求参数
     * @return 创建的模板信息
     */
    @PostMapping
    public Result<PayloadTemplateDTO> createTemplate(@Valid @RequestBody PayloadTemplateCreateRequest request) {
        try {
            PayloadTemplateDTO result = payloadTemplateService.createTemplate(request);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("创建模板失败: " + e.getMessage());
        }
    }

    /**
     * 更新Payload模板
     *
     * @param id      模板ID
     * @param request 更新请求参数
     * @return 更新后的模板信息
     */
    @PutMapping("/{id}")
    public Result<PayloadTemplateDTO> updateTemplate(@PathVariable("id") Long id,
                                                     @Valid @RequestBody PayloadTemplateUpdateRequest request) {
        try {
            PayloadTemplateDTO result = payloadTemplateService.updateTemplate(id, request);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("更新模板失败: " + e.getMessage());
        }
    }

    /**
     * 删除Payload模板
     *
     * @param id 模板ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTemplate(@PathVariable("id") Long id) {
        try {
            payloadTemplateService.deleteTemplate(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除模板失败: " + e.getMessage());
        }
    }

    /**
     * 切换模板收藏状态
     *
     * @param id 模板ID
     * @return 操作结果
     */
    @PostMapping("/{id}/toggle-favorite")
    public Result<Void> toggleFavorite(@PathVariable("id") Long id) {
        try {
            payloadTemplateService.toggleFavorite(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 复制模板
     *
     * @param id 模板ID
     * @return 复制后的模板信息
     */
    @PostMapping("/{id}/copy")
    public Result<PayloadTemplateDTO> copyTemplate(@PathVariable("id") Long id) {
        try {
            PayloadTemplateDTO result = payloadTemplateService.copyTemplate(id);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("复制模板失败: " + e.getMessage());
        }
    }

    /**
     * 使用模板（增加使用次数）
     *
     * @param id 模板ID
     * @return 操作结果
     */
    @PostMapping("/{id}/use")
    public Result<Void> useTemplate(@PathVariable("id") Long id) {
        try {
            payloadTemplateService.useTemplate(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取模板统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> result = payloadTemplateService.getStatistics();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 按业务分组获取模板列表
     *
     * @return 按分组组织的模板列表
     */
    @GetMapping("/grouped")
    public Result<List<Map<String, Object>>> getGroupedTemplates() {
        try {
            List<Map<String, Object>> result = payloadTemplateService.getGroupedTemplates();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取分组模板失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除模板
     *
     * @param ids 模板ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteTemplates(@RequestBody List<Long> ids) {
        try {
            payloadTemplateService.batchDeleteTemplates(ids);
            return Result.success();
        } catch (Exception e) {
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }
}