import service.TaskManager;
import service.Managers;
import service.HistoryManager;
import service.InMemoryTaskManager;
import service.InMemoryHistoryManager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ManagersTest {
    @Test
    public void testGetDefaultReturnsTaskManager() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "Managers.getDefault() не должен возвращать null.");
        assertTrue(manager instanceof InMemoryTaskManager,
                "Managers.getDefault() должен возвращать экземпляр InMemoryTaskManager.");
    }

    @Test
    public void testGetDefaultHistoryReturnsHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Managers.getDefaultHistory() не должен возвращать null.");
        assertTrue(historyManager instanceof InMemoryHistoryManager,
                "Managers.getDefaultHistory() должен возвращать экземпляр InMemoryHistoryManager.");
    }
}
