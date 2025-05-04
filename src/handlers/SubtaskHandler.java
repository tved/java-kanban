package handlers;

import com.sun.net.httpserver.HttpExchange;
import exceptions.NotFoundException;
import exceptions.TaskOverlapException;
import model.Subtask;
import service.TaskManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler {
    public SubtaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handleRequest(HttpExchange httpExchange) throws IOException {
        Endpoint endpoint = resolveEndpoint(httpExchange.getRequestURI().getPath(), httpExchange.getRequestMethod());
        Integer id = null;

        if (endpoint.equals(Endpoint.GET_SUBTASK_BY_ID) || endpoint.equals(Endpoint.DELETE_SUBTASK)) {
            id = getTaskId(httpExchange);
            if (id == null) return;
        }

        switch (endpoint) {
            case GET_SUBTASKS:
                handleGetSubtasks(httpExchange);
                break;
            case GET_SUBTASK_BY_ID:
                handleGetSubtaskById(httpExchange, id);
                break;
            case CREATE_SUBTASK:
                handleCreateSubtask(httpExchange);
                break;
            case UPDATE_SUBTASK:
                handleUpdateSubtask(httpExchange);
                break;
            case DELETE_SUBTASK:
                handleDeleteSubtask(httpExchange, id);
                break;
            case DELETE_ALL_SUBTASKS:
                handleDeleteAllSubtasks(httpExchange);
            default:
                writeResponse(httpExchange, "Такого эндпоинта не существует", 404);
        }
    }

    private void handleGetSubtasks(HttpExchange httpExchange) throws IOException {
        List<Subtask> subtasks = new ArrayList<>(taskManager.getSubtasks().values());
        String subtasksList = gson.toJson(subtasks);
        writeResponse(httpExchange, subtasksList, 200);
    }

    private void handleGetSubtaskById(HttpExchange httpExchange, Integer subtaskId) throws IOException {
        try {
            Subtask subtask = taskManager.getSubtaskById(subtaskId);
            String response = gson.toJson(subtask);
            writeResponse(httpExchange, response, 200);
        } catch (NotFoundException e) {
            sendNotFound(httpExchange, e.getMessage());
        }
    }

    private void handleDeleteSubtask(HttpExchange httpExchange, Integer subtaskId) throws IOException {
        Subtask subtaskToDelete = taskManager.getSubtaskById(subtaskId);
        taskManager.deleteSubtask(subtaskToDelete);
        sendText(httpExchange, "Подзадача с id " + subtaskId + " удалена");
    }

    private void handleCreateSubtask(HttpExchange httpExchange) throws IOException {
        String requestBody = new String(httpExchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
        try {
            Subtask newSubtask = gson.fromJson(requestBody, Subtask.class);
            taskManager.addSubtask(newSubtask);
            sendText(httpExchange, "Подзадача добавлена", 201);
        } catch (TaskOverlapException e) {
            sendHasOverlapping(httpExchange);
        } catch (NotFoundException e) {
            sendNotFound(httpExchange, e.getMessage());
        }
    }

    private void handleUpdateSubtask(HttpExchange httpExchange) throws IOException {
        String requestBody = new String(httpExchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
        try {
            Subtask updatedSubtask = gson.fromJson(requestBody, Subtask.class);
            taskManager.updateSubtask(updatedSubtask);
            sendText(httpExchange, "Подзадача обновлена");
        } catch (TaskOverlapException e) {
            sendHasOverlapping(httpExchange);
        } catch (NotFoundException e) {
            sendNotFound(httpExchange, e.getMessage());
        }
    }

    private void handleDeleteAllSubtasks(HttpExchange httpExchange) throws IOException {
        taskManager.clearSubtasks();
        sendText(httpExchange, "Все подзадачи удалены");
    }
}
