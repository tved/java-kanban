package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    public HistoryManager history = Managers.getDefaultHistory();
    protected int currentId = 1;
    protected final HashMap<Integer, Task> tasks = new HashMap<>();

    protected final HashMap<Integer, Epic> epics = new HashMap<>();

    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    Set<Task> prioritizedTasks = new TreeSet<>((Task t1, Task t2) -> {
        if (t1.getStartTime().equals(t2.getStartTime())) {
            return t1.getId() - t2.getId();
        }
        return t1.getStartTime().compareTo(t2.getStartTime());
    });

    @Override
    public HashMap<Integer, Task> getTasks() {
        return tasks;
    }

    @Override
    public HashMap<Integer, Epic> getEpics() {
        return epics;
    }

    @Override
    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    @Override
    public void clearTasks() {
        tasks.values().forEach(task -> {
            history.remove(task.getId());
            if (task.getStartTime() != null) {
                prioritizedTasks.remove(task);
            }
        });
        tasks.clear();
    }

    @Override
    public void clearEpics() {
        epics.values().forEach((epic -> {
            history.remove(epic.getId());
        }));
        epics.clear();
        subtasks.values().forEach(subtask -> {
            history.remove(subtask.getId());
            if (subtask.getStartTime() != null) {
                prioritizedTasks.remove(subtask);
            }
        });
        subtasks.clear();
    }

    @Override
    public void clearSubtasks() {
        subtasks.values().forEach(subtask -> {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(subtask);
            }
            history.remove(subtask.getId());
            if (subtask.getStartTime() != null) {
                prioritizedTasks.remove(subtask);
            }
        });
        subtasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        if (tasks.containsKey(id)) {
            history.add(tasks.get(id));
            return tasks.get(id);
        } else {
            System.out.println("Задачи с таким id не существует");
        }
        return null;
    }

    @Override
    public Epic getEpicById(int id) {
        if (epics.containsKey(id)) {
            history.add(epics.get(id));
            return epics.get(id);
        } else {
            System.out.println("Эпика с таким id не существует");
        }
        return null;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            history.add(subtasks.get(id));
            return subtasks.get(id);
        } else {
            System.out.println("Подзадачи с таким id не существует");
        }
        return null;
    }

    @Override
    public void addTask(Task task) {
        if (!isTaskOverlapping(task)) {
            tasks.put(currentId, task);
            task.setId(currentId);
            currentId++;
            if (task.getStartTime() != null) {
                prioritizedTasks.add(task);
            }
        } else {
            System.out.println("Задача пересекается по времени с другими задачами");
        }
    }

    @Override
    public void addEpic(Epic epic) {
        epics.put(currentId, epic);
        epic.setId(currentId);
        currentId++;
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (!isTaskOverlapping(subtask)) {
            subtasks.put(currentId, subtask);
            subtask.setId(currentId);
            currentId++;
            Epic currentEpic = epics.get(subtask.getEpicId());
            if (currentEpic != null) {
                currentEpic.addSubtask(subtask);
            } else {
                System.out.println("Эпик для подзадачи указан неверно или не существует.");
            }

            if (subtask.getStartTime() != null) {
                prioritizedTasks.add(subtask);
            }
        } else {
            System.out.println("Задача пересекается по времени с другими задачами");
        }
    }

    @Override
    public void updateTask(Task task) {
        if (!isTaskOverlapping(task)) {
            Task oldTask = tasks.get(task.getId());
            if (oldTask != null && oldTask.getStartTime() != null) {
                prioritizedTasks.remove(oldTask);
            }
            if (task.getStartTime() != null) {
                prioritizedTasks.add(task);
            }
            tasks.put(task.getId(), task);
        } else {
            System.out.println("Задача пересекается по времени с другими задачами");
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        List<Subtask> epicSubtasks = epics.get(epic.getId()).getSubtasks();
        epic.setSubtasks(epicSubtasks);
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (!isTaskOverlapping(subtask)) {
            Subtask oldSubtask = subtasks.get(subtask.getId());
            if (oldSubtask != null && oldSubtask.getStartTime() != null) {
                prioritizedTasks.remove(oldSubtask);
            }
            if (subtask.getStartTime() != null) {
                prioritizedTasks.add(subtask);
            }
            subtasks.put(subtask.getId(), subtask);
            Epic currentEpic = epics.get(subtask.getEpicId());
            if (currentEpic != null) {
                List<Subtask> subtasksInEpic = currentEpic.getSubtasks();
                subtasksInEpic.stream().filter(s -> s.getId() == subtask.getId()).findFirst().ifPresent(foundSubtask -> {
                    int index = subtasksInEpic.indexOf(foundSubtask);
                    subtasksInEpic.set(index, subtask);
                    currentEpic.setSubtasks(subtasksInEpic);
                });
            } else {
                System.out.println("Эпик для подзадачи указан неверно или не существует.");
            }
        } else {
            System.out.println("Задача пересекается по времени с другими задачами");
        }

    }

    @Override
    public void deleteTask(Task task) {
        tasks.remove(task.getId());
        history.remove(task.getId());
        prioritizedTasks.remove(task);
    }

    @Override
    public void deleteEpic(Epic epic) {
        epic.getSubtasks().forEach(subtask -> {
            subtasks.remove(subtask.getId());
            history.remove(subtask.getId());
            if (subtask.getStartTime() != null) {
                prioritizedTasks.remove(subtask);
            }
        });
        epics.remove(epic.getId());
        history.remove(epic.getId());
    }

    @Override
    public void deleteSubtask(Subtask subtask) {
        Epic currentEpic = epics.get(subtask.getEpicId());
        currentEpic.removeSubtask(subtask);
        subtasks.remove(subtask.getId());
        history.remove(subtask.getId());
        prioritizedTasks.remove(subtask);
    }

    @Override
    public List<Subtask> getSubtasksInEpic(Epic epic) {
        return epics.get(epic.getId()).getSubtasks();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private boolean isOverlapping(Task a, Task b) {
        if (a.getStartTime() != null && a.getEndTime() != null && b.getStartTime() != null && b.getEndTime() != null) {
            return a.getStartTime().isBefore(b.getEndTime()) && b.getStartTime().isBefore(a.getEndTime());
        }
        return false;
    }

    @Override
    public boolean isTaskOverlapping(Task newTask) {
        return prioritizedTasks.stream().filter(task -> task.getId() != newTask.getId()).anyMatch(task -> isOverlapping(task, newTask));
    }
}
