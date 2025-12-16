package com.usst.spm.demo.dto;

public class InviteCreateRequest {
    /**
     * 邀请码有效天数，默认 30 天
     */
    private Integer expireDays;

    /**
     * 使用上限，0 表示不限制
     */
    private Integer maxUse;

    public Integer getExpireDays() {
        return expireDays;
    }

    public void setExpireDays(Integer expireDays) {
        this.expireDays = expireDays;
    }

    public Integer getMaxUse() {
        return maxUse;
    }

    public void setMaxUse(Integer maxUse) {
        this.maxUse = maxUse;
    }
}

