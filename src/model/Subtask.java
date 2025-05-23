package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String name, String description, Status status, int epicId, Duration duration, LocalDateTime startTime) {
        super(name, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, Status status, int epicId, Duration duration, LocalDateTime startTime) {
        super(id, name, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toString() {
        return "Subtask:" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", desc='" + description + '\'' +
                ", status='" + status + '\'' +
                "\n";
    }
}
