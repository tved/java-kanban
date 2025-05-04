package exceptions;

public class TaskOverlapException extends RuntimeException {
    public TaskOverlapException() {
        super("Задача пересекается по времени с другими задачами");
    }
}
