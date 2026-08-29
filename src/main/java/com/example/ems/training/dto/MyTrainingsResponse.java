package com.example.ems.training.dto;

import com.example.ems.training.entity.Training;
import com.example.ems.training.entity.ParticipationStatus;

import java.util.ArrayList;
import java.util.List;

public class MyTrainingsResponse {

    public static class MyTrainingItem {
        private Training training;
        private ParticipationStatus participationStatus;

        public MyTrainingItem(Training training, ParticipationStatus participationStatus) {
            this.training = training;
            this.participationStatus = participationStatus;
        }

        public Training getTraining() { return training; }
        public void setTraining(Training training) { this.training = training; }

        public ParticipationStatus getParticipationStatus() { return participationStatus; }
        public void setParticipationStatus(ParticipationStatus participationStatus) { this.participationStatus = participationStatus; }
    }

    private List<MyTrainingItem> today = new ArrayList<>();
    private List<MyTrainingItem> upcoming = new ArrayList<>();
    private List<MyTrainingItem> completed = new ArrayList<>();
    private List<MyTrainingItem> missed = new ArrayList<>();

    public List<MyTrainingItem> getToday() { return today; }
    public void setToday(List<MyTrainingItem> today) { this.today = today; }

    public List<MyTrainingItem> getUpcoming() { return upcoming; }
    public void setUpcoming(List<MyTrainingItem> upcoming) { this.upcoming = upcoming; }

    public List<MyTrainingItem> getCompleted() { return completed; }
    public void setCompleted(List<MyTrainingItem> completed) { this.completed = completed; }

    public List<MyTrainingItem> getMissed() { return missed; }
    public void setMissed(List<MyTrainingItem> missed) { this.missed = missed; }
}
