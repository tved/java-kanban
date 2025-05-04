package handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import handlers.typeAdapters.DurationTypeAdapter;
import handlers.typeAdapters.LocalDateTimeTypeAdapter;
import model.Epic;
import model.Status;
import model.Subtask;
import org.junit.jupiter.api.*;
import service.HttpTaskServer;
import service.InMemoryTaskManager;
import service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerSubtasksTest {

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
    public void shouldCreateNewSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "desc");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("subtask1For1", "desc for task", Status.NEW, 1, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 5, 13, 0));
        String json = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код ответа для POST /subtasks должен быть 201");
        List<Subtask> subtasks = new ArrayList<>(manager.getSubtasks().values());
        assertEquals(1, subtasks.size(), "Некорректное количество подзадач");
        assertEquals("subtask1For1", subtasks.get(0).getName(), "Некорректное название подзадачи");
    }

    @Test
    public void shouldGetAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "desc");
        manager.addEpic(epic);
        manager.addSubtask(new Subtask("subtask1", "desc", Status.NEW, 1, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 5, 13, 0)));
        manager.addSubtask(new Subtask("subtask1", "desc", Status.DONE, 1, Duration.ofMinutes(20), LocalDateTime.of(2025, 5, 1, 11, 0)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа для GET /subtasks должен быть 200");
        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(2, subtasks.length, "Некорректное количество подзадач");
    }

    @Test
    public void shouldGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "desc");
        manager.addEpic(epic);
        manager.addSubtask(new Subtask("created subtask", "desc", Status.NEW, 1, Duration.ofMinutes(20), LocalDateTime.of(2025, 5, 1, 13, 0)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks/2"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа для GET /subtasks/{id} должен быть 200");
        Subtask subtask = gson.fromJson(response.body(), Subtask.class);
        assertEquals("created subtask", subtask.getName(), "Некорректное название подзадачи");
    }

    @Test
    public void shouldUpdateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "desc");
        manager.addEpic(epic);
        manager.addSubtask(new Subtask("subtask", "desc", Status.NEW, 1,Duration.ofMinutes(10), LocalDateTime.of(2025, 5, 2, 12, 0)));

        String json = gson.toJson(new Subtask(2, "updated", "desc", Status.NEW, 1, Duration.ofMinutes(10), LocalDateTime.of(2025, 5, 2, 12, 0)));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks/2"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа для POST /subtasks/{id} должен быть 200");
        assertEquals("updated", manager.getSubtaskById(2).getName(), "Некорректное название подзадачи");
    }

    @Test
    public void shouldDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "desc");
        manager.addEpic(epic);
        manager.addSubtask(new Subtask("subtask to delete", "desc", Status.NEW, 1, Duration.ofMinutes(10), LocalDateTime.of(2025, 5, 3, 15, 0)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks/2"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа для DELETE /subtasks/{id} должен быть 200");
        assertTrue(manager.getSubtasks().isEmpty(), "Подзадача должна быть удалена");
    }

    @Test
    public void shouldReturn404WhenSubtaskNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks/10"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа для GET /subtasks/{id} для несуществующей одзадачи должен быть 404");
    }

    @Test
    public void shouldReturn406WhenSubtaskOverlaps() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "desc");
        manager.addEpic(epic);
        manager.addSubtask(new Subtask("s1", "d", Status.NEW, 1, Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 5, 12, 0)));
        manager.addSubtask(new Subtask("s2", "d", Status.NEW, 1, Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 5, 15, 0)));

        Subtask conflictingSubtask = new Subtask(3, "s2", "d", Status.NEW, 1, Duration.ofMinutes(45), LocalDateTime.of(2025, 5, 5, 12, 30));
        String json = gson.toJson(conflictingSubtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks/3"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(), "Код ответа для POST /subtasks/{id}, если задачи пересекаются, должен быть 406");
    }

    @Test
    public void shouldReturn404WhenSubtaskEpicMissing() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("subtask", "d", Status.NEW, 1, Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 6, 12, 0));
        String json = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа для POST /subtasks/{id}, если эпик не найден, должен быть 404");
    }

    @Test
    public void shouldDeleteAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "desc");
        manager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "desc", Status.NEW, 1,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 2, 10, 0));
        Subtask subtask2 = new Subtask("Subtask 2", "desc", Status.IN_PROGRESS, 1,
                Duration.ofMinutes(45), LocalDateTime.of(2025, 5, 2, 12, 0));

        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа для DELETE /subtasks должен быть 200");
        assertTrue(manager.getSubtasks().isEmpty(), "Список подзадач должен быть пустым");

        Epic epicOfDeletedSubtasks = manager.getEpicById(1);
        assertTrue(epicOfDeletedSubtasks.getSubtasks().isEmpty(), "Эпик должен содержать пустой список подзадач");
    }

}
