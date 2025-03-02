package service;

import model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int HISTORY_SIZE = 10;

    private final List<Task> history = new ArrayList<>(HISTORY_SIZE);

    @Override
    public List<Task> getHistory() {
        return history;
    }

    @Override
    public void add(Task task) {
        if (history.size() == HISTORY_SIZE) {
            history.remove(0);
        }
        history.add(task);
    }

    @Override
    public void remove(Task task) {
        history.removeIf(item -> item.equals(task));
    }
}
