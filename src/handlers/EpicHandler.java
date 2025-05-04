package handlers;

import com.sun.net.httpserver.HttpExchange;
import errors.NotFoundException;
import errors.TaskOverlapException;
import model.Epic;
import model.Subtask;
import service.TaskManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EpicHandler extends BaseHttpHandler {
    public EpicHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handleRequest(HttpExchange httpExchange) throws IOException {
        Endpoint endpoint = resolveEndpoint(httpExchange.getRequestURI().getPath(), httpExchange.getRequestMethod());
        Integer id = null;

        if (endpoint.equals(Endpoint.GET_EPIC_BY_ID) || endpoint.equals(Endpoint.GET_SUBTASKS_IN_EPIC) || endpoint.equals(Endpoint.DELETE_EPIC)) {
            id = getTaskId(httpExchange);
            if (id == null) return;
        }

        switch (endpoint) {
            case GET_EPICS:
                handleGetEpics(httpExchange);
                break;
            case GET_EPIC_BY_ID:
                handleGetEpicById(httpExchange, id);
                break;
            case CREATE_EPIC:
                handleCreateEpic(httpExchange);
                break;
            case GET_SUBTASKS_IN_EPIC:
                handleGetSubtasksInEpic(httpExchange, id);
                break;
            case DELETE_EPIC:
                handleDeleteEpic(httpExchange, id);
                break;
            default:
                writeResponse(httpExchange, "Такого эндпоинта не существует", 404);
        }
    }

    private void handleGetEpics(HttpExchange httpExchange) throws IOException {
        List<Epic> epics = new ArrayList<>(taskManager.getEpics().values());
        String epicsList = gson.toJson(epics);
        writeResponse(httpExchange, epicsList, 200);
    }

    private void handleGetEpicById(HttpExchange httpExchange, Integer epicId) throws IOException {
        try {
            Epic epic = taskManager.getEpicById(epicId);
            String response = gson.toJson(epic);
            writeResponse(httpExchange, response, 200);
        } catch (NotFoundException e) {
            sendNotFound(httpExchange, e.getMessage());
        }
    }

    private void handleDeleteEpic(HttpExchange httpExchange, Integer epicId) throws IOException {
        Epic epicToDelete = taskManager.getEpicById(epicId);
        taskManager.deleteEpic(epicToDelete);
        sendText(httpExchange, "Эпик с id " + epicId + " удален");
    }

    private void handleCreateEpic(HttpExchange httpExchange) throws IOException {
        String requestBody = new String(httpExchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
        try {
            Epic newEpic = gson.fromJson(requestBody, Epic.class);
            taskManager.addEpic(newEpic);
            sendText(httpExchange, "Эпик добавлен");
        } catch (TaskOverlapException e) {
            sendHasOverlapping(httpExchange);
        }
    }

    private void handleGetSubtasksInEpic(HttpExchange httpExchange, Integer epicId) throws IOException {
        try {
            Epic epic = taskManager.getEpicById(epicId);
            List<Subtask> subtasks = taskManager.getSubtasksInEpic(epic);
            String response = gson.toJson(subtasks);
            writeResponse(httpExchange, response, 200);
        } catch (NotFoundException e) {
            sendNotFound(httpExchange, e.getMessage());
        }
    }
}