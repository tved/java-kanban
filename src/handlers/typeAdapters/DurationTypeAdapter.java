package handlers.typeAdapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.Duration;

public class DurationTypeAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
        if (duration == null) {
            jsonWriter.nullValue();
            return;
        }
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        jsonWriter.value(String.format("%02d:%02d", hours, minutes));
    }

    @Override
    public Duration read(JsonReader in) throws IOException {
        String[] parts = in.nextString().split(":");
        if (parts.length != 2) {
            throw new IOException("Длительность должна быть в формате чч:мм");
        }
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return Duration.ofHours(hours).plusMinutes(minutes);
    }
}
