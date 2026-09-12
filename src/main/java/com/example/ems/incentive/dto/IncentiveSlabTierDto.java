package com.example.ems.incentive.dto;

import java.math.BigDecimal;

public class IncentiveSlabTierDto {

    private BigDecimal minAchievementPercentage;
    private BigDecimal maxAchievementPercentage;
    private BigDecimal incentiveAmount;
    private BigDecimal percentage;

    public IncentiveSlabTierDto() {}

    public IncentiveSlabTierDto(BigDecimal minAchievementPercentage, BigDecimal maxAchievementPercentage,
                                BigDecimal incentiveAmount, BigDecimal percentage) {
        this.minAchievementPercentage = minAchievementPercentage;
        this.maxAchievementPercentage = maxAchievementPercentage;
        this.incentiveAmount = incentiveAmount;
        this.percentage = percentage;
    }

    public BigDecimal getMinAchievementPercentage() { return minAchievementPercentage; }
    public void setMinAchievementPercentage(BigDecimal minAchievementPercentage) { this.minAchievementPercentage = minAchievementPercentage; }

    public BigDecimal getMaxAchievementPercentage() { return maxAchievementPercentage; }
    public void setMaxAchievementPercentage(BigDecimal maxAchievementPercentage) { this.maxAchievementPercentage = maxAchievementPercentage; }

    public BigDecimal getIncentiveAmount() { return incentiveAmount; }
    public void setIncentiveAmount(BigDecimal incentiveAmount) { this.incentiveAmount = incentiveAmount; }

    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
}
