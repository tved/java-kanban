package handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import handlers.typeAdapters.DurationTypeAdapter;
import handlers.typeAdapters.LocalDateTimeTypeAdapter;
import service.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public abstract class BaseHttpHandler implements HttpHandler {
    Gson gson;
    protected final TaskManager taskManager;
    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;

        GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter());
        gson = gsonBuilder.create();
    }

    @Override
    public final void handle(HttpExchange exchange) throws IOException {
        try {
            handleRequest(exchange);
        } catch (Exception e) {
            sendServerError(exchange, e.getMessage());
        }
    }

    protected abstract void handleRequest(HttpExchange exchange) throws IOException;

    protected void sendText(HttpExchange h, String text, int responseCode) throws IOException {
        writeResponse(h, gson.toJson(Map.of("message", text)), responseCode);
    }

    protected void sendText(HttpExchange h, String text) throws IOException {
        sendText(h, text, 200);
    }



    protected void sendNotFound(HttpExchange h, String errorText) throws IOException {
        writeResponse(h, gson.toJson(Map.of("error", errorText)), 404);
    }

    protected void sendHasOverlapping(HttpExchange h) throws IOException {
        writeResponse(h, gson.toJson(Map.of("error", "Задача пересекается с другими по времени")), 406);
    }

    protected void sendServerError(HttpExchange h, String message) throws IOException {
        writeResponse(h, gson.toJson(Map.of("error", "Ошибка сервера: " + message)), 500);
    }

    protected void writeResponse(HttpExchange exchange, String responseString, int responseCode) throws IOException {
        try (OutputStream os = exchange.getResponseBody()) {
            exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            exchange.sendResponseHeaders(responseCode, 0);
            os.write(responseString.getBytes(DEFAULT_CHARSET));
        }
        exchange.close();
    }

    protected Integer getTaskId(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Integer.parseInt(pathParts[2]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            writeResponse(exchange, "Некорректный идентификатор задачи", 400);
            return null;
        }
    }

    protected Endpoint resolveEndpoint(String path, String method) {
        if (path.startsWith("/tasks")) {
            return resolveTaskEndpoint(path, method);
        }
        if (path.startsWith("/subtasks")) {
            return resolveSubtaskEndpoint(path, method);
        }
        if (path.startsWith("/epics")) {
            return resolveEpicEndpoint(path, method);
        }
        if (path.equals("/history")) {
            return Endpoint.GET_HISTORY;
        }

        if (path.equals("/prioritized")) {
            return Endpoint.GET_PRIORITIZED_TASKS;
        }

        return Endpoint.UNKNOWN;
    }

    private Endpoint resolveTaskEndpoint(String path, String method) {
        String[] parts = path.split("/");
        if (parts.length == 2) {
            if (method.equals("GET")) {
                return Endpoint.GET_TASKS;
            } else if (method.equals("POST")) {
                return Endpoint.CREATE_TASK;
            } else if (method.equals("DELETE")) {
                return Endpoint.DELETE_ALL_TASKS;
            }
        }
        if (parts.length == 3) {
            if (method.equals("GET")) {
                return Endpoint.GET_TASK_BY_ID;
            } else if (method.equals("POST")) {
                return Endpoint.UPDATE_TASK;
            } else if (method.equals("DELETE")) {
                return Endpoint.DELETE_TASK;
            }
        }
        return Endpoint.UNKNOWN;
    }

    private Endpoint resolveSubtaskEndpoint(String path, String method) {
        String[] parts = path.split("/");
        if (parts.length == 2) {
            if (method.equals("GET")) {
                return Endpoint.GET_SUBTASKS;
            } else if (method.equals("POST")) {
                return Endpoint.CREATE_SUBTASK;
            } else if (method.equals("DELETE")) {
                return Endpoint.DELETE_ALL_SUBTASKS;
            }
        }
        if (parts.length == 3) {
            if (method.equals("GET")) {
                return Endpoint.GET_SUBTASK_BY_ID;
            } else if (method.equals("POST")) {
                return Endpoint.UPDATE_SUBTASK;
            } else if (method.equals("DELETE")) {
                return Endpoint.DELETE_SUBTASK;
            }
        }
        return Endpoint.UNKNOWN;
    }

    private Endpoint resolveEpicEndpoint(String path, String method) {
        String[] parts = path.split("/");
        if (parts.length == 2) {
            if (method.equals("GET")) {
                return Endpoint.GET_EPICS;
            } else if (method.equals("POST")) {
                return Endpoint.CREATE_EPIC;
            } else if (method.equals("DELETE")) {
                return Endpoint.DELETE_ALL_EPICS;
            }
        }
        if (parts.length == 3) {
            if (method.equals("GET")) {
                return Endpoint.GET_EPIC_BY_ID;
            } else if (method.equals("DELETE")) {
                return Endpoint.DELETE_EPIC;
            }
        }

        if (parts.length == 4 && method.equals("GET")) {
            return Endpoint.GET_SUBTASKS_IN_EPIC;
        }

        return Endpoint.UNKNOWN;
    }
}
