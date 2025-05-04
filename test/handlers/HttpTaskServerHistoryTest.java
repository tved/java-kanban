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

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerHistoryTest {

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
    public void shouldReturnEmptyHistoryIfNoTasksViewed() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа для GET /history должен быть 200");
        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(0, history.length, "История должна быть пустой, если задачи не просматривались");
    }

    @Test
    public void shouldReturnHistoryWithViewedTasks() throws IOException, InterruptedException {
        Task task = new Task("Task1", "desc", Status.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 10, 12, 0));
        manager.addTask(task);

        manager.getTaskById(1);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа для GET /history должен быть 200");
        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(1, history.length, "История должна содержать 1 просмотренную задачу");
        assertEquals("Task1", history[0].getName(), "Некорректная задача в истории");
    }

    @Test
    public void shouldReturn404ForUnsupportedEndpoint() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/history/unknown"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа для неизвестного эндпоинта должен быть 404");
    }
}
