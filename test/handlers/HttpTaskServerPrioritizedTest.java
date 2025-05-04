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

public class HttpTaskServerPrioritizedTest {

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
    public void shouldReturnEmptyPrioritizedListIfNoTasks() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа для GET /prioritized должен быть 200");
        Task[] prioritized = gson.fromJson(response.body(), Task[].class);
        assertEquals(0, prioritized.length, "Список задач должен быть пустым");
    }

    @Test
    public void shouldReturnTasksInCorrectOrder() throws IOException, InterruptedException {
        Task task1 = new Task("earlier", "desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 1, 9, 0));
        Task task2 = new Task("later", "desc", Status.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 1, 15, 0));

        manager.addTask(task2);
        manager.addTask(task1);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа для GET /prioritized должен быть 200");
        Task[] prioritized = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, prioritized.length, "Список должен содержать 2 задачи");
        assertEquals("earlier", prioritized[0].getName(), "Первая задача должна быть самой ранней по startTime");
        assertEquals("later", prioritized[1].getName(), "Вторая задача должна быть позднее");
    }

    @Test
    public void shouldReturn404ForUnsupportedEndpoint() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/prioritized/unknown"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа для неизвестного эндпоинта должен быть 404");
    }
}
