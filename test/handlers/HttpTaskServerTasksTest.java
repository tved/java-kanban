package handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import handlers.typeAdapters.DurationTypeAdapter;
import handlers.typeAdapters.LocalDateTimeTypeAdapter;
import model.Status;
import model.Task;
import org.junit.jupiter.api.*;
import service.HttpTaskServer;
import service.InMemoryTaskManager;
import service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTasksTest {

    private static final int PORT = 8081;
    private static final String BASE_URL = "http://localhost:" + PORT;

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager, PORT);
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .create();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void shouldCreateNewTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Test Description", Status.NEW, Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 1, 12, 0));
        String json = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа для GET /tasks должен быть 200");

        List<Task> tasks = manager.getTasks().values().stream().toList();
        assertEquals(1, tasks.size(), "Некорректное количество задач");
        assertEquals("Test Task", tasks.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void shouldGetAllTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Desc 1", Status.NEW, Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 1, 10, 0));
        Task task2 = new Task("Task 2", "Desc 2", Status.DONE, Duration.ofMinutes(45), LocalDateTime.of(2025, 5, 2, 14, 0));
        manager.addTask(task1);
        manager.addTask(task2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа для GET /tasks должен быть 200");
        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, tasks.length, "Некорректное количество задач");
    }

    @Test
    public void shouldGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("do sth", "description", Status.NEW, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 3, 12, 0));
        manager.addTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/1"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа для GET /tasks/{id} должен быть 200");
        Task returned = gson.fromJson(response.body(), Task.class);
        assertEquals("do sth", returned.getName(), "Некорректное название задачи");
    }

    @Test
    public void shouldUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("test task", "desc", Status.NEW, Duration.ofMinutes(20), LocalDateTime.of(2025, 5, 4, 9, 0));
        manager.addTask(task);

        String json = gson.toJson(new Task(1,"updated name", "desc", Status.NEW, Duration.ofMinutes(20), LocalDateTime.of(2025, 5, 4, 9, 0)));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/1"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа для POST /tasks/{id} должен быть 200");
        assertEquals("updated name", manager.getTaskById(1).getName(), "Некорректное название обновленной задачи");
    }

    @Test
    public void shouldDeleteTask() throws IOException, InterruptedException {
        Task task = new Task("task 1", "task to delete", Status.NEW, Duration.ofMinutes(25), LocalDateTime.of(2025, 5, 5, 11, 0));
        manager.addTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/1"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа для DELETE /tasks/{id} должен быть 200");
        assertTrue(manager.getTasks().isEmpty(), "Список задач должен быть пустым");
    }

    @Test
    public void shouldReturn404WhenTaskNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/100"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа для GET /tasks/{id} для несуществующей задачи должен быть 404");
    }

    @Test
    public void shouldReturn406WhenTaskOverlapsOnUpdate() throws IOException, InterruptedException {
        Task task1 = new Task("Task1", "desc", Status.NEW, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 10, 12, 0));
        manager.addTask(task1);

        Task task2 = new Task("Task2", "desc", Status.NEW, Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 10, 18, 30));
        manager.addTask(task2);


        String json = gson.toJson(new Task(2,"Task2", "desc", Status.NEW, Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 10, 12, 30)));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/2"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(), "Код ответа для POST /tasks/{id}, если задачи пересекаются, должен быть 406");
    }

    @Test
    public void shouldReturn406WhenTaskOverlapsOnCreate() throws IOException, InterruptedException {
        Task task1 = new Task("task1", "desc", Status.NEW, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 10, 12, 0));
        manager.addTask(task1);

        Task newTask = new Task("new task", "desc", Status.NEW, Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 10, 12, 30));
        String json = gson.toJson(newTask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(), "Код ответа для POST /tasks, если задачи пересекаются, должен быть 406");
    }
}
