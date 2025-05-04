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

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerEpicsTest {

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
    public void shouldCreateNewEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic title", "desc");
        String json = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа для POST /epics должен быть 200");
        assertEquals(1, manager.getEpics().size(), "Некорректное количество эпиков");
        assertEquals("Epic title", manager.getEpicById(1).getName(), "Некорректное название эпика");
    }

    @Test
    public void shouldGetAllEpics() throws IOException, InterruptedException {
        manager.addEpic(new Epic("Epic 1", "desc 1"));
        manager.addEpic(new Epic("Epic 2", "desc 2"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Epic[] epics = gson.fromJson(response.body(), Epic[].class);
        assertEquals(200, response.statusCode(), "Код ответа для GET /epics должен быть 200");
        assertEquals(2, epics.length, "Некорректное количество эпиков");
    }

    @Test
    public void shouldGetEpicById() throws IOException, InterruptedException {
        manager.addEpic(new Epic("Epic 1", "Description"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/1"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа для GET /epics/{id} должен быть 200");
        Epic epic = gson.fromJson(response.body(), Epic.class);
        assertEquals("Epic 1", epic.getName(), "Некорректное имя эпика");
    }

    @Test
    public void shouldDeleteEpic() throws IOException, InterruptedException {
        manager.addEpic(new Epic("Epic to delete", "desc"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/1"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа для DELETE /epics/{id} должен быть 200");
        assertTrue(manager.getEpics().isEmpty(), "Эпик должен быть удален");
    }

    @Test
    public void shouldReturn404WhenEpicNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа для GET /epics/{id} для несуществующего эпика должен быть 404");
    }

    @Test
    public void shouldGetAllSubtasksInEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic with subtasks", "desc");
        manager.addEpic(epic);
        manager.addSubtask(new Subtask("sub1", "desc", Status.NEW, 1, Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 5, 12, 0)));
        manager.addSubtask(new Subtask("sub2", "desc", Status.DONE, 1, Duration.ofMinutes(20), LocalDateTime.of(2025, 5, 6, 13, 0)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/1/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(200, response.statusCode(), "Код ответа для GET /epics/{id}/subtasks должен быть 200");
        assertEquals(2, subtasks.length, "Некорректное количество подзадач в эпике");
    }

    @Test
    public void shouldReturn404WhenGettingSubtasksForMissingEpic() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/999/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа для GET /epics/{id}/subtasks если эпик не найден должен быть 404");
    }
}
