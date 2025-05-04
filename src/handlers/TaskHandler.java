package handlers;

import com.sun.net.httpserver.HttpExchange;
import exceptions.NotFoundException;
import exceptions.TaskOverlapException;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TaskHandler extends BaseHttpHandler {
   public TaskHandler(TaskManager taskManager) {
       super(taskManager);
   }

    @Override
    public void handleRequest(HttpExchange httpExchange) throws IOException {
        Endpoint endpoint = resolveEndpoint(httpExchange.getRequestURI().getPath(), httpExchange.getRequestMethod());
        Integer id = null;

        if (endpoint.equals(Endpoint.GET_TASK_BY_ID) || endpoint.equals(Endpoint.DELETE_TASK)) {
            id = getTaskId(httpExchange);
            if (id == null) return;
        }

        switch (endpoint) {
            case GET_TASKS:
                handleGetTasks(httpExchange);
                break;
            case GET_TASK_BY_ID:
                handleGetTaskById(httpExchange, id);
                break;
            case CREATE_TASK:
                handleCreateTask(httpExchange);
                break;
            case UPDATE_TASK:
                handleUpdateTask(httpExchange);
                break;
            case DELETE_TASK:
                handleDeleteTask(httpExchange, id);
                break;
            case DELETE_ALL_TASKS:
                handleDeleteAllTasks(httpExchange);
            default:
                writeResponse(httpExchange, "Такого эндпоинта не существует", 404);
        }
    }

    private void handleGetTasks(HttpExchange httpExchange) throws IOException {
        List<Task> tasks = new ArrayList<>(taskManager.getTasks().values());
        String tasksList = gson.toJson(tasks);
        writeResponse(httpExchange, tasksList, 200);
    }

    private void handleGetTaskById(HttpExchange httpExchange, Integer taskId) throws IOException {
        try {
            Task task = taskManager.getTaskById(taskId);
            String response = gson.toJson(task);
            writeResponse(httpExchange, response, 200);
        } catch (NotFoundException e) {
            sendNotFound(httpExchange, e.getMessage());
        }
    }

    private void handleDeleteTask(HttpExchange httpExchange, Integer taskId) throws IOException {
        Task taskToDelete = taskManager.getTaskById(taskId);
        taskManager.deleteTask(taskToDelete);
        sendText(httpExchange, "Задача с id " + taskId + " удалена");
    }

    private void handleCreateTask(HttpExchange httpExchange) throws IOException {
        String requestBody = new String(httpExchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
        try {
            Task newTask = gson.fromJson(requestBody, Task.class);
            taskManager.addTask(newTask);
            sendText(httpExchange, "Задача добавлена", 201);
        } catch (TaskOverlapException e) {
            sendHasOverlapping(httpExchange);
        }
    }

    private void handleUpdateTask(HttpExchange httpExchange) throws IOException {
        String requestBody = new String(httpExchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
        try {
            Task updatedTask = gson.fromJson(requestBody, Task.class);
            taskManager.updateTask(updatedTask);
            sendText(httpExchange, "Задача обновлена");
        } catch (TaskOverlapException e) {
            sendHasOverlapping(httpExchange);
        }
    }

    private void handleDeleteAllTasks(HttpExchange httpExchange) throws IOException {
       taskManager.clearTasks();
       sendText(httpExchange, "Все задачи удалены");
    }
}
