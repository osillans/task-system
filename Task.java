import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class Task {

    private static final Pattern NUMBER =
            Pattern.compile("[+-]?(\\d+([.,]\\d*)?|[.,]\\d+)([eE][+-]?\\d+)?");

    private final double value;
    private final List<String> handlers;

    public Task(double value, List<String> handlers) {
        this.value = value;
        this.handlers = Collections.unmodifiableList(new ArrayList<>(handlers));
    }

    public double getValue() {
        return value;
    }

    public List<String> getHandlers() {
        return handlers;
    }

    public static double parseValue(String raw) {
        String text = raw.trim();
        if (!NUMBER.matcher(text).matches()) {
            throw new IllegalArgumentException("«" + text + "» — не число");
        }
        double value = Double.parseDouble(text.replace(',', '.'));
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("значення має бути скінченним числом");
        }
        return value;
    }

    public static List<String> parseHandlers(String raw) {
        String text = raw.replace(',', ' ').trim().toLowerCase();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("потрібен принаймні один обробник");
        }
        List<String> names = new ArrayList<>();
        Collections.addAll(names, text.split("\\s+"));
        return names;
    }

    public static Task parse(String text) {
        int colon = text.indexOf(':');
        if (colon < 0) {
            throw new IllegalArgumentException("очікується формат «значення: обробник1, обробник2»");
        }
        double value = parseValue(text.substring(0, colon));
        List<String> handlers = parseHandlers(text.substring(colon + 1));
        return new Task(value, handlers);
    }
}