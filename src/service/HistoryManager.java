package service;

import model.Task;

import java.util.List;



public interface HistoryManager {
    void add(Task task);

    void remove(Task task);

    List<Task> getHistory();
}
