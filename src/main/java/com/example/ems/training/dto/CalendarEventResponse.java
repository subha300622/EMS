package com.example.ems.training.dto;

import com.example.ems.training.entity.DeliveryMethod;
import com.example.ems.training.entity.TrainingStatus;

import java.time.LocalDateTime;

public class CalendarEventResponse {
    private Long trainingId;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private DeliveryMethod deliveryMethod;
    private TrainingStatus status;
    private String category;
    private Long trainerId;

    public Long getTrainingId() { return trainingId; }
    public void setTrainingId(Long trainingId) { this.trainingId = trainingId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }

    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }

    public DeliveryMethod getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(DeliveryMethod deliveryMethod) { this.deliveryMethod = deliveryMethod; }

    public TrainingStatus getStatus() { return status; }
    public void setStatus(TrainingStatus status) { this.status = status; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }
}
