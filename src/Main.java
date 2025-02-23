public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = new TaskManager();
        Task task1 = new Task("task 1", "do sth useful", Status.NEW);
        Task task2 = new Task("task 2", "do sth different", Status.NEW);
        Epic epic1 = new Epic("Epic 1", "do sth");
        Epic epic2 = new Epic("Epic 2", "do sth else");
        Subtask subtask1For1 = new Subtask("subtask1For1", "desc for task", Status.NEW, 3);
        Subtask subtask2For1 = new Subtask("subtask2For1", "desc for task", Status.NEW, 3);
        Subtask subtask3For1 = new Subtask("subtask3For1", "desc for task", Status.NEW, 3);
        Subtask subtask1For2 = new Subtask("subtask1For2", "desc for task", Status.NEW, 4);
        Subtask subtask2For2 = new Subtask("subtask2For2", "desc for task", Status.NEW, 4);
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);
        taskManager.addSubtask(subtask1For1);
        taskManager.addSubtask(subtask2For1);
        taskManager.addSubtask(subtask3For1);
        taskManager.addSubtask(subtask1For2);
        taskManager.addSubtask(subtask2For2);
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubtasks());
        taskManager.updateSubtask(new Subtask(5, "subtask1For1", "desc for task", Status.IN_PROGRESS, 3));
        System.out.println(taskManager.getSubtasks());
        System.out.println(taskManager.getEpics());
        taskManager.deleteSubtask(new Subtask(5, "subtask1For1", "desc for task", Status.IN_PROGRESS, 3));
        System.out.println(taskManager.getEpics());
    }
}
