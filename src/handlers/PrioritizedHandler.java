package handlers;

import com.sun.net.httpserver.HttpExchange;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {
    public PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handleRequest(HttpExchange httpExchange) throws IOException {
        Endpoint endpoint = resolveEndpoint(httpExchange.getRequestURI().getPath(), httpExchange.getRequestMethod());
        if (!endpoint.equals(Endpoint.GET_PRIORITIZED_TASKS)) {
            writeResponse(httpExchange, "Такого эндпоинта не существует", 404);
        } else {
            List<Task> prioritized = taskManager.getPrioritizedTasks();
            String response = gson.toJson(prioritized);
            writeResponse(httpExchange, response, 200);
        }
    }
}
