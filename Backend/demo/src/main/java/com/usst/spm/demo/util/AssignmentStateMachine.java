package com.usst.spm.demo.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

/**
 * 作业状态机工具类
 * 定义作业状态转换规则和校验逻辑
 */
public class AssignmentStateMachine {

    // 作业状态常量
    public static final String STATUS_DRAFT = "DRAFT";      // 草稿
    public static final String STATUS_PUBLISHED = "PUBLISHED"; // 已发布
    public static final String STATUS_CLOSED = "CLOSED";     // 已截止
    public static final String STATUS_ARCHIVED = "ARCHIVED";  // 已归档

    /**
     * 校验状态转换是否合法
     * 
     * @param currentStatus 当前状态
     * @param targetStatus 目标状态
     * @param assignmentDueAt 作业截止时间
     * @throws ResponseStatusException 如果状态转换不合法
     */
    public static void validateStateTransition(String currentStatus, String targetStatus, LocalDateTime assignmentDueAt) {
        if (currentStatus == null) {
            currentStatus = STATUS_DRAFT;
        }
        
        switch (targetStatus) {
            case STATUS_DRAFT:
                // 只能从PUBLISHED转为DRAFT（撤回发布）
                if (!STATUS_PUBLISHED.equals(currentStatus)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "只有已发布的作业才能撤回为草稿");
                }
                // 已截止的作业不能撤回
                if (assignmentDueAt != null && assignmentDueAt.isBefore(LocalDateTime.now())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "已截止的作业不能撤回发布");
                }
                break;
                
            case STATUS_PUBLISHED:
                // 可以从DRAFT或CLOSED转为PUBLISHED
                if (!STATUS_DRAFT.equals(currentStatus) && !STATUS_CLOSED.equals(currentStatus)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "只有草稿或已截止的作业才能发布");
                }
                break;
                
            case STATUS_CLOSED:
                // 只能从PUBLISHED转为CLOSED（自动或手动截止）
                if (!STATUS_PUBLISHED.equals(currentStatus)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "只有已发布的作业才能截止");
                }
                break;
                
            case STATUS_ARCHIVED:
                // 可以从CLOSED转为ARCHIVED
                if (!STATUS_CLOSED.equals(currentStatus)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "只有已截止的作业才能归档");
                }
                break;
                
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "未知的作业状态: " + targetStatus);
        }
    }

    /**
     * 检查作业是否可以编辑
     * 
     * @param status 作业状态
     * @return true可以编辑，false不可编辑
     */
    public static boolean canEdit(String status) {
        return STATUS_DRAFT.equals(status) || STATUS_PUBLISHED.equals(status);
    }

    /**
     * 检查作业是否可以删除
     * 
     * @param status 作业状态
     * @return true可以删除，false不可删除
     */
    public static boolean canDelete(String status) {
        // 只有草稿和已归档的作业可以删除
        return STATUS_DRAFT.equals(status) || STATUS_ARCHIVED.equals(status);
    }

    /**
     * 检查作业是否可以发布
     * 
     * @param status 作业状态
     * @return true可以发布，false不可发布
     */
    public static boolean canPublish(String status) {
        return STATUS_DRAFT.equals(status) || STATUS_CLOSED.equals(status);
    }

    /**
     * 检查作业是否可以撤回发布
     * 
     * @param status 作业状态
     * @param dueAt 截止时间
     * @return true可以撤回，false不可撤回
     */
    public static boolean canUnpublish(String status, LocalDateTime dueAt) {
        if (!STATUS_PUBLISHED.equals(status)) {
            return false;
        }
        // 已截止的作业不能撤回
        return dueAt == null || !dueAt.isBefore(LocalDateTime.now());
    }

    /**
     * 检查作业是否可以重新发布
     * 
     * @param status 作业状态
     * @return true可以重新发布，false不可重新发布
     */
    public static boolean canRepublish(String status) {
        // PUBLISHED和CLOSED状态的作业可以重新发布
        return STATUS_PUBLISHED.equals(status) || STATUS_CLOSED.equals(status);
    }

    /**
     * 根据截止时间自动判断作业状态
     * 
     * @param dueAt 截止时间
     * @param currentStatus 当前状态
     * @return 应该的状态
     */
    public static String autoDetermineStatus(LocalDateTime dueAt, String currentStatus) {
        if (dueAt == null) {
            return currentStatus != null ? currentStatus : STATUS_DRAFT;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (dueAt.isBefore(now)) {
            // 已截止
            if (STATUS_PUBLISHED.equals(currentStatus)) {
                return STATUS_CLOSED;
            }
            return currentStatus != null ? currentStatus : STATUS_CLOSED;
        } else {
            // 未截止
            if (STATUS_DRAFT.equals(currentStatus)) {
                return STATUS_DRAFT;
            }
            return STATUS_PUBLISHED;
        }
    }
}

