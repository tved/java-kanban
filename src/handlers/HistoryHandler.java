package handlers;

import com.sun.net.httpserver.HttpExchange;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {
    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handleRequest(HttpExchange httpExchange) throws IOException {
        Endpoint endpoint = resolveEndpoint(httpExchange.getRequestURI().getPath(), httpExchange.getRequestMethod());
        if (!endpoint.equals(Endpoint.GET_HISTORY)) {
            writeResponse(httpExchange, "Такого эндпоинта не существует", 404);
        } else {
            List<Task> history = taskManager.getHistory();
            String response = gson.toJson(history);
            writeResponse(httpExchange, response, 200);
        }
    }
}
