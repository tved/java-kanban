import java.util.HashMap;
import java.util.List;

public class TaskManager {
    private int currentId = 1;
    private final HashMap<Integer, Task> tasks = new HashMap<>();

    private final HashMap<Integer, Epic> epics = new HashMap<>();

    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    public HashMap<Integer, Task> getTasks() {
        return tasks;
    }

    public HashMap<Integer, Epic> getEpics() {
        return epics;
    }

    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public void clearTasks() {
        tasks.clear();
    }

    public void clearEpics() {
        for (int epicId : epics.keySet()) {
            for (Subtask subtask : subtasks.values()) {
                if (subtask.getEpicId() == epicId) {
                    subtasks.remove(subtask.getId());
                }
            }
        }
        epics.clear();
    }

    public void clearSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            epics.get(subtask.getEpicId()).removeSubtask(subtask);
        }
        subtasks.clear();
    }

    public Task getTaskById(int id) {
        if (tasks.containsKey(id)) {
            return tasks.get(id);
        } else {
            System.out.println("Задачи с таким id не существует");
        }
        return null;
    }

    public Task getEpicById(int id) {
        if (epics.containsKey(id)) {
            return epics.get(id);
        } else {
            System.out.println("Эпика с таким id не существует");
        }
        return null;
    }

    public Task getSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            return subtasks.get(id);
        } else {
            System.out.println("Подзадачи с таким id не существует");
        }
        return null;
    }

    public void addTask(Task task) {
        tasks.put(currentId, task);
        task.setId(currentId);
        currentId++;
    }

    public void addEpic(Epic epic) {
        epics.put(currentId, epic);
        epic.setId(currentId);
        currentId++;
    }

    public void addSubtask(Subtask subtask) {
        subtasks.put(currentId, subtask);
        subtask.setId(currentId);
        currentId++;
        Epic currentEpic = epics.get(subtask.getEpicId());
        currentEpic.addSubtask(subtask);
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void updateEpic(Epic epic) {
        List<Subtask> epicSubtasks = epics.get(epic.getId()).getSubtasks();
        epic.setSubtasks(epicSubtasks);
        epics.put(epic.getId(), epic);
    }

    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic currentEpic = epics.get(subtask.getEpicId());
        List<Subtask> subtasksInEpic = currentEpic.getSubtasks();
        for (int i = 0; i < subtasksInEpic.size(); i++) {
            if (subtasksInEpic.get(i).getId() == subtask.getId()) {
                subtasksInEpic.set(i, subtask);
                currentEpic.setSubtasks(subtasksInEpic);
                break;
            }
        }
    }

    public void deleteTask(Task task) {
        tasks.remove(task.getId());
    }

    public void deleteEpic(Epic epic) {
        for (Subtask subtask : epic.getSubtasks()) {
            subtasks.remove(subtask.getId());
        }
        epics.remove(epic.getId());
    }

    public void deleteSubtask(Subtask subtask) {
        Epic currentEpic = epics.get(subtask.getEpicId());
        List<Subtask> subtasksInEpic = currentEpic.getSubtasks();
        subtasksInEpic.remove(subtask);
        currentEpic.setSubtasks(subtasksInEpic);

        subtasks.remove(subtask.getId());
    }

    public List<Subtask> getSubtasksInEpic(Epic epic) {
        return epics.get(epic.getId()).getSubtasks();
    }
}
