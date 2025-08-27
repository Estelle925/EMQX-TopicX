package com.emqx.topichub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emqx.topichub.dto.PayloadTemplateCreateRequest;
import com.emqx.topichub.dto.PayloadTemplateDTO;
import com.emqx.topichub.dto.PayloadTemplateSearchRequest;
import com.emqx.topichub.dto.PayloadTemplateUpdateRequest;
import com.emqx.topichub.entity.Group;
import com.emqx.topichub.entity.PayloadTemplate;
import com.emqx.topichub.mapper.PayloadTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Payload模板服务类
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class PayloadTemplateService extends ServiceImpl<PayloadTemplateMapper, PayloadTemplate> {

    private final GroupService groupService;

    /**
     * 分页搜索模板
     */
    public IPage<PayloadTemplateDTO> searchTemplates(PayloadTemplateSearchRequest request) {
        // 构建查询条件
        QueryWrapper<PayloadTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);

        // 关键词搜索
        if (StringUtils.hasText(request.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like("name", request.getKeyword())
                    .or()
                    .like("description", request.getKeyword())
                    .or()
                    .like("payload", request.getKeyword())
            );
        }

        // 业务分组过滤
        if (request.getGroupId() != null) {
            queryWrapper.eq("group_id", request.getGroupId());
        }

        // 模板类型过滤
        if (StringUtils.hasText(request.getType())) {
            queryWrapper.eq("type", request.getType());
        }

        // 收藏过滤
        if (request.getOnlyFavorites() != null && request.getOnlyFavorites()) {
            queryWrapper.eq("is_favorite", true);
        }

        // 排序
        String sortBy = request.getSortBy();
        if (StringUtils.hasText(request.getSortBy())) {
            //驼峰转下划线
            sortBy = sortBy.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
            queryWrapper.orderByAsc(sortBy);
        } else {
            sortBy = "updated_at";
        }
        String sortOrder = StringUtils.hasText(request.getSortOrder()) ? request.getSortOrder() : "desc";
        String asc = "asc";
        if (asc.equalsIgnoreCase(sortOrder)) {
            queryWrapper.orderByAsc(sortBy);
        } else {
            queryWrapper.orderByDesc(sortBy);
        }

        // 分页查询 - 优先使用page字段，兼容current字段
        int pageNum = request.getPage() != null ? request.getPage() :
                (request.getCurrent() != null ? request.getCurrent() : 1);
        Page<PayloadTemplate> page = new Page<>(pageNum,
                request.getSize() != null ? request.getSize() : 10);
        IPage<PayloadTemplate> templatePage = this.page(page, queryWrapper);

        // 转换为DTO
        IPage<PayloadTemplateDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(templatePage, dtoPage);
        List<PayloadTemplateDTO> dtoList = templatePage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    /**
     * 根据ID获取模板DTO
     */
    public PayloadTemplateDTO getTemplateDtoById(Long id) {
        PayloadTemplate template = this.getById(id);
        if (template == null || template.getDeleted() != 0) {
            throw new RuntimeException("模板不存在或已删除");
        }
        return convertToDTO(template);
    }

    /**
     * 创建模板
     */
    @Transactional(rollbackFor = Exception.class)
    public PayloadTemplateDTO createTemplate(PayloadTemplateCreateRequest request) {
        // 检查模板名称是否已存在
        QueryWrapper<PayloadTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", request.getName())
                .eq("group_id", request.getGroupId())
                .eq("deleted", 0);
        if (this.count(queryWrapper) > 0) {
            throw new RuntimeException("该业务分组中已存在相同名称的模板");
        }

        // 创建模板
        PayloadTemplate template = new PayloadTemplate();
        BeanUtils.copyProperties(request, template);
        template.setUsageCount(0);
        template.setFavorite(false);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setDeleted(0);
        template.setContent(request.getPayload());

        if (this.save(template)) {
            return convertToDTO(template);
        } else {
            throw new RuntimeException("创建模板失败");
        }
    }

    /**
     * 更新模板
     */
    @Transactional(rollbackFor = Exception.class)
    public PayloadTemplateDTO updateTemplate(Long id, PayloadTemplateUpdateRequest request) {
        PayloadTemplate existingTemplate = this.getById(id);
        if (existingTemplate == null || existingTemplate.getDeleted() != 0) {
            throw new RuntimeException("模板不存在或已删除");
        }

        // 检查模板名称是否与其他模板冲突
        QueryWrapper<PayloadTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", request.getName())
                .eq("group_id", request.getGroupId())
                .ne("id", id)
                .eq("deleted", 0);
        if (this.count(queryWrapper) > 0) {
            throw new RuntimeException("该业务分组中已存在相同名称的模板");
        }

        // 更新模板
        BeanUtils.copyProperties(request, existingTemplate);
        existingTemplate.setUpdatedAt(LocalDateTime.now());

        if (this.updateById(existingTemplate)) {
            return convertToDTO(existingTemplate);
        } else {
            throw new RuntimeException("更新模板失败");
        }
    }

    /**
     * 删除模板
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(Long id) {
        PayloadTemplate template = this.getById(id);
        if (template == null || template.getDeleted() != 0) {
            throw new RuntimeException("模板不存在或已删除");
        }

        // 逻辑删除
        template.setDeleted(1);
        template.setUpdatedAt(LocalDateTime.now());

        if (!this.updateById(template)) {
            throw new RuntimeException("删除模板失败");
        }
    }

    /**
     * 切换收藏状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleFavorite(Long id) {
        PayloadTemplate template = this.getById(id);
        if (template == null || template.getDeleted() != 0) {
            throw new RuntimeException("模板不存在或已删除");
        }

        // 切换收藏状态
        template.setFavorite(!template.getFavorite());
        template.setUpdatedAt(LocalDateTime.now());

        if (!this.updateById(template)) {
            throw new RuntimeException("切换收藏状态失败");
        }
    }

    /**
     * 复制模板
     */
    @Transactional(rollbackFor = Exception.class)
    public PayloadTemplateDTO copyTemplate(Long id) {
        PayloadTemplate originalTemplate = this.getById(id);
        if (originalTemplate == null || originalTemplate.getDeleted() != 0) {
            throw new RuntimeException("模板不存在或已删除");
        }

        // 创建副本
        PayloadTemplate copyTemplate = new PayloadTemplate();
        BeanUtils.copyProperties(originalTemplate, copyTemplate);
        // 清空ID，让数据库自动生成
        copyTemplate.setId(null);
        copyTemplate.setName(originalTemplate.getName() + "_副本");
        copyTemplate.setUsageCount(0);
        copyTemplate.setFavorite(false);
        copyTemplate.setLastUsed(null);
        copyTemplate.setCreatedAt(LocalDateTime.now());
        copyTemplate.setUpdatedAt(LocalDateTime.now());
        copyTemplate.setDeleted(0);

        // 确保副本名称唯一
        String baseName = copyTemplate.getName();
        int counter = 1;
        while (true) {
            QueryWrapper<PayloadTemplate> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("name", copyTemplate.getName())
                    .eq("group_id", copyTemplate.getGroupId())
                    .eq("deleted", 0);
            if (this.count(queryWrapper) == 0) {
                break;
            }
            copyTemplate.setName(baseName + "_" + counter);
            counter++;
        }

        if (this.save(copyTemplate)) {
            return convertToDTO(copyTemplate);
        } else {
            throw new RuntimeException("复制模板失败");
        }
    }

    /**
     * 使用模板
     */
    @Transactional(rollbackFor = Exception.class)
    public void useTemplate(Long id) {
        PayloadTemplate template = this.getById(id);
        if (template == null || template.getDeleted() != 0) {
            throw new RuntimeException("模板不存在或已删除");
        }

        // 增加使用次数并更新最近使用时间
        template.setUsageCount(template.getUsageCount() + 1);
        template.setLastUsed(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());

        if (!this.updateById(template)) {
            throw new RuntimeException("更新模板使用信息失败");
        }
    }

    /**
     * 获取统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>(16);

        // 总模板数
        QueryWrapper<PayloadTemplate> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("deleted", 0);
        long totalTemplates = this.count(totalWrapper);
        statistics.put("totalTemplates", totalTemplates);

        // 业务分组数
        QueryWrapper<Group> groupWrapper = new QueryWrapper<>();
        groupWrapper.eq("deleted", 0);
        long groupCount = groupService.count(groupWrapper);
        statistics.put("groupCount", groupCount);

        // 最近使用的模板数（最近7天内使用过的）
        QueryWrapper<PayloadTemplate> recentWrapper = new QueryWrapper<>();
        recentWrapper.lambda()
                .eq(PayloadTemplate::getDeleted, 0)
                .ge(PayloadTemplate::getLastUsed, LocalDateTime.now().minusDays(7));
        long recentUsedCount = this.count(recentWrapper);
        statistics.put("recentUsedCount", recentUsedCount);

        // 收藏的模板数
        QueryWrapper<PayloadTemplate> favoriteWrapper = new QueryWrapper<>();
        favoriteWrapper.eq("deleted", 0)
                .eq("is_favorite", true);
        long favoriteCount = this.count(favoriteWrapper);
        statistics.put("favoriteCount", favoriteCount);

        return statistics;
    }

    /**
     * 获取按分组组织的模板列表
     */
    public List<Map<String, Object>> getGroupedTemplates() {
        // 获取所有有效的业务分组
        QueryWrapper<Group> groupWrapper = new QueryWrapper<>();
        groupWrapper.eq("deleted", 0).orderByAsc("name");
        List<Group> groups = groupService.list(groupWrapper);

        List<Map<String, Object>> groupedTemplates = new ArrayList<>();

        for (Group group : groups) {
            // 获取该分组下的模板
            QueryWrapper<PayloadTemplate> templateWrapper = new QueryWrapper<>();
            templateWrapper.eq("group_id", group.getId())
                    .eq("deleted", 0)
                    .orderByDesc("updated_at");
            List<PayloadTemplate> templates = this.list(templateWrapper);

            if (!templates.isEmpty()) {
                Map<String, Object> groupData = new HashMap<>(16);
                groupData.put("groupId", group.getId());
                groupData.put("groupName", group.getName());
                groupData.put("templates", templates.stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()));
                groupedTemplates.add(groupData);
            }
        }

        return groupedTemplates;
    }

    /**
     * 批量删除模板
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteTemplates(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 获取要删除的模板
        QueryWrapper<PayloadTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids).eq("deleted", 0);
        List<PayloadTemplate> templates = this.list(queryWrapper);

        if (templates.isEmpty()) {
            throw new RuntimeException("没有找到可删除的模板");
        }

        // 批量逻辑删除
        LocalDateTime now = LocalDateTime.now();
        templates.forEach(template -> {
            template.setDeleted(1);
            template.setUpdatedAt(now);
        });

        if (!this.updateBatchById(templates)) {
            throw new RuntimeException("批量删除模板失败");
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 转换PayloadTemplate实体为DTO
     */
    private PayloadTemplateDTO convertToDTO(PayloadTemplate template) {
        PayloadTemplateDTO dto = new PayloadTemplateDTO();
        BeanUtils.copyProperties(template, dto);
        dto.setPayload(template.getContent());
        // 设置业务分组名称
        if (template.getGroupId() != null) {
            Group group = groupService.getById(template.getGroupId());
            if (group != null && group.getDeleted() == 0) {
                dto.setGroupName(group.getName());
            }
        }

        return dto;
    }
}