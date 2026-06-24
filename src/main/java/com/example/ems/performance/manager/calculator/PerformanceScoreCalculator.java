package com.example.ems.performance.manager.calculator;

import com.example.ems.performance.manager.entity.CompetencyRating;
import com.example.ems.performance.manager.entity.PerformanceGoal;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PerformanceScoreCalculator {

    public double calculateGoalScore(List<PerformanceGoal> goals) {
        if (goals == null || goals.isEmpty()) {
            return 0.0;
        }
        double weightedProgressSum = 0.0;
        double weightSum = 0.0;
        for (PerformanceGoal g : goals) {
            double progress = g.getProgress() != null ? g.getProgress() : 0.0;
            double weight = g.getWeight() != null ? g.getWeight() : 0.0;
            weightedProgressSum += progress * weight;
            weightSum += weight;
        }
        if (weightSum == 0.0) {
            double progressSum = 0.0;
            for (PerformanceGoal g : goals) {
                progressSum += g.getProgress() != null ? g.getProgress() : 0.0;
            }
            double score = (progressSum / goals.size() / 100.0) * 5.0;
            return Math.round(score * 100.0) / 100.0;
        }
        double weightedAverageProgress = weightedProgressSum / weightSum;
        double score = (weightedAverageProgress / 100.0) * 5.0;
        return Math.round(score * 100.0) / 100.0;
    }

    public double calculateCompetencyScore(List<CompetencyRating> competencies) {
        if (competencies == null || competencies.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        int count = 0;
        for (CompetencyRating c : competencies) {
            if (c.getManagerScore() != null && c.getManagerScore() > 0) {
                sum += c.getManagerScore();
                count++;
            }
        }
        if (count == 0) {
            return 0.0;
        }
        double score = sum / count;
        return Math.round(score * 100.0) / 100.0;
    }

    public double calculateFinalScore(double goalScore, double competencyScore) {
        double score = (goalScore * 0.4) + (competencyScore * 0.6);
        return Math.round(score * 100.0) / 100.0;
    }

    public String getFinalScoreLabel(double finalScore) {
        if (finalScore >= 4.5) {
            return "Exceptional";
        } else if (finalScore >= 3.5) {
            return "Good";
        } else {
            return "Needs Improvement";
        }
    }
}
