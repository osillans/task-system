import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Executor {

    public static class ExecutorException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ExecutorException(String message) {
            super(message);
        }

        public ExecutorException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class UnknownHandlerException extends ExecutorException {
        private static final long serialVersionUID = 1L;

        public UnknownHandlerException(String message) {
            super(message);
        }
    }

    public static class HandlerException extends ExecutorException {
        private static final long serialVersionUID = 1L;

        public HandlerException(String message, Throwable cause) {
            super(message, cause);
        }

        public HandlerException(String message) {
            super(message);
        }
    }

    private static final class Entry {
        final Handler handler;
        final String description;

        Entry(Handler handler, String description) {
            this.handler = handler;
            this.description = description;
        }
    }

    private final Map<String, Entry> handlers = new LinkedHashMap<>();

    public Executor() {
        registerDefaults();
    }

    public void register(String name, String description, Handler handler) {
        handlers.put(name, new Entry(handler, description));
    }

    private void registerDefaults() {
        register("inc", "x + 1", x -> x + 1);
        register("dec", "x - 1", x -> x - 1);
        register("double", "x * 2", x -> x * 2);
        register("half", "x / 2", x -> x / 2);
        register("square", "x в квадраті", x -> x * x);
        register("cube", "x в кубі", x -> x * x * x);
        register("neg", "-x (протилежне число)", x -> -x);
        register("abs", "|x| (модуль)", x -> Math.abs(x));
        register("sqrt", "квадратний корінь з x (x >= 0)", x -> {
            if (x < 0) {
                throw new IllegalArgumentException("від'ємне число під коренем");
            }
            return Math.sqrt(x);
        });
        register("inv", "1 / x (x != 0)", x -> {
            if (x == 0) {
                throw new ArithmeticException("ділення на нуль");
            }
            return 1 / x;
        });
    }

    public Map<String, String> describe() {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, Entry> e : handlers.entrySet()) {
            result.put(e.getKey(), e.getValue().description);
        }
        return Collections.unmodifiableMap(result);
    }

    public void check(List<String> names) {
        List<String> unknown = new ArrayList<>();
        for (String name : names) {
            if (!handlers.containsKey(name)) {
                unknown.add("«" + name + "»");
            }
        }
        if (!unknown.isEmpty()) {
            throw new UnknownHandlerException("невідомі обробники: " + String.join(", ", unknown));
        }
    }

    public List<Double> run(double value, List<String> names) {
        check(names);
        List<Double> results = new ArrayList<>();
        for (String name : names) {
            double result;
            try {
                result = handlers.get(name).handler.apply(value);
            } catch (RuntimeException e) {
                String reason = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                throw new HandlerException("обробник «" + name + "»: " + reason, e);
            }
            if (Double.isNaN(result) || Double.isInfinite(result)) {
                throw new HandlerException("обробник «" + name + "»: результат не є скінченним числом");
            }
            results.add(result);
        }
        return results;
    }
}