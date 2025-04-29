package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class Epic extends Task {
    private List<Subtask> subtasks = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description);
        this.status = Status.NEW;
    }

    private void setStatus(Status status) {
        this.status = status;
    }

    public Epic(int id, String name, String description) {
        super(id, name, description);
        this.status = Status.NEW;
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
        updateStatus();
        updateTimeFields();
    }

    public void addSubtask(Subtask subtask) {
        subtasks.add(subtask);
        updateStatus();
        updateTimeFields();
    }

    public void removeSubtask(Subtask subtask) {
        subtasks.remove(subtask);
        updateStatus();
        updateTimeFields();
    }

    private void updateStatus() {
        boolean allNew = true;
        boolean allDone = true;

        if (subtasks.isEmpty()) {
            setStatus(Status.NEW);
            return;
        }

        for (Subtask subtask : subtasks) {
            if (!subtask.getStatus().equals(Status.NEW)) {
                allNew = false;
            }
            if (!subtask.getStatus().equals(Status.DONE)) {
                allDone = false;
            }
        }

        if (!allDone && !allNew) {
            setStatus(Status.IN_PROGRESS);
        } else if (allNew) {
            setStatus(Status.NEW);
        } else {
            setStatus(Status.DONE);
        }
    }

    private void updateTimeFields() {
        duration = subtasks.stream().map(Subtask::getDuration).filter(Objects::nonNull).reduce(Duration.ZERO, Duration::plus);
        startTime = subtasks.stream().map(Subtask::getStartTime).filter(Objects::nonNull).min(LocalDateTime::compareTo).orElse(null);
        endTime = subtasks.stream().map(Subtask::getEndTime).filter(Objects::nonNull).max(LocalDateTime::compareTo).orElse(null);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public String toString() {
        return "Epic:" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", desc='" + description + '\'' +
                ", status='" + status + '\'' +
                "\n";
    }
}
