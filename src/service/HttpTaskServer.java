package service;

import com.sun.net.httpserver.HttpServer;
import handlers.*;


import java.io.IOException;
import java.net.InetSocketAddress;


public class HttpTaskServer {
    private final int port;
    private final HttpServer httpServer;

    public static void main(String[] args) throws IOException {
        TaskManager taskManager = Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer(taskManager, 8080);
        server.start();
    }

    public HttpTaskServer(TaskManager taskManager, int port) throws IOException {
        this.port = port;
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/tasks", new TaskHandler(taskManager));
        httpServer.createContext("/subtasks", new SubtaskHandler(taskManager));
        httpServer.createContext("/epics", new EpicHandler(taskManager));
        httpServer.createContext("/history", new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));
    }

    public void start() {
        httpServer.start();
        System.out.println("HTTP сервер запущен на порту " + port);
    }

    public void stop() {
        httpServer.stop(0);
        System.out.println("HTTP сервер остановлен");
    }
}
