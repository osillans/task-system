import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserInterface {

    private final Executor executor;
    private final Consolidation consolidation;
    private final BufferedReader in;
    private final PrintStream out;
    private final Statistics stats = new Statistics();

    public UserInterface(Executor executor, Consolidation consolidation) {
        this(executor, consolidation,
                new BufferedReader(new InputStreamReader(System.in)), System.out);
    }

    public UserInterface(Executor executor, Consolidation consolidation,
                         BufferedReader in, PrintStream out) {
        this.executor = executor;
        this.consolidation = consolidation;
        this.in = in;
        this.out = out;
    }

    public void printHandlers() {
        out.println("Доступні обробники:");
        for (Map.Entry<String, String> e : executor.describe().entrySet()) {
            out.println(String.format("  %-8s %s", e.getKey(), e.getValue()));
        }
    }

    public void run(List<Task> tasks) {
        if (tasks == null) {
            out.println("Система виконання завдань");
            out.println("Функція консолідації: " + consolidation.getLabel()
                    + " (" + consolidation.getCode() + ")");
            tasks = askTasks();
        }
        int number = 1;
        for (Task task : tasks) {
            execute(number++, task);
        }
        printSummary();
    }

    private String ask(String prompt) {
        out.print(prompt);
        out.flush();
        try {
            String line = in.readLine();
            if (line == null) {
                out.println();
            }
            return line;
        } catch (IOException e) {
            return null;
        }
    }

    private List<Task> askTasks() {
        List<Task> tasks = new ArrayList<>();
        out.println("Введіть серію завдань. Порожній рядок — завершити введення, «?» — список обробників.");
        while (true) {
            String raw = ask("Завдання №" + (tasks.size() + 1) + ". Вхідне значення: ");
            if (raw == null) {
                break;
            }
            raw = raw.trim();
            if (raw.isEmpty()) {
                break;
            }
            if (raw.equals("?")) {
                printHandlers();
                continue;
            }
            double value;
            try {
                value = Task.parseValue(raw);
            } catch (IllegalArgumentException e) {
                out.println("  Помилка: " + e.getMessage());
                continue;
            }
            List<String> handlers = askHandlers();
            if (handlers == null) {
                break;
            }
            tasks.add(new Task(value, handlers));
        }
        return tasks;
    }

    private List<String> askHandlers() {
        while (true) {
            String raw = ask("  Обробники (через кому, «?» — список): ");
            if (raw == null) {
                return null;
            }
            raw = raw.trim();
            if (raw.equals("?")) {
                printHandlers();
                continue;
            }
            try {
                List<String> names = Task.parseHandlers(raw);
                executor.check(names);
                return names;
            } catch (IllegalArgumentException | Executor.ExecutorException e) {
                out.println("  Помилка: " + e.getMessage());
            }
        }
    }

    private void execute(int number, Task task) {
        out.println();
        out.println("Завдання №" + number + ": значення " + fmt(task.getValue())
                + ", обробники: " + String.join(", ", task.getHandlers()));

        long started = System.nanoTime();
        List<Double> results;
        double result;
        try {
            results = executor.run(task.getValue(), task.getHandlers());
            
            result = consolidation.apply(results);
            if (Double.isNaN(result) || Double.isInfinite(result)) {
                throw new ArithmeticException("результат консолідації не є скінченним числом");
            }
        } catch (Executor.ExecutorException | ArithmeticException e) {
            stats.recordFailure();
            out.println("  ПОМИЛКА: " + e.getMessage());
            return;
        } finally {
            stats.addElapsed(System.nanoTime() - started);
        }

        stats.recordSuccess(task, result);
        List<String> shown = new ArrayList<>();
        for (double r : results) {
            shown.add(fmt(r));
        }
        out.println("  результати обробників: [" + String.join(", ", shown) + "]");
        out.println("  РЕЗУЛЬТАТ (" + consolidation.getLabel() + "): " + fmt(result));
    }

    private void row(String label, String value) {
        out.println(String.format("%-28s%s", label, value));
    }

    private void printSummary() {
        out.println();
        out.println("============================================");
        out.println("ЗВЕДЕНА СТАТИСТИКА");
        out.println("============================================");
        row("Завдань усього:", String.valueOf(stats.total()));
        row("  виконано успішно:", String.valueOf(stats.succeeded()));
        row("  завершено з помилкою:", String.valueOf(stats.failed()));
        row("Функція консолідації:", consolidation.getLabel() + " (" + consolidation.getCode() + ")");
        if (stats.hasResults()) {
            row("Результати завдань:", "мін " + fmt(stats.min()) + ", макс " + fmt(stats.max())
                    + ", середнє " + fmt(stats.mean()));
        }
        List<Map.Entry<String, Integer>> calls = stats.handlerCallsSorted();
        if (!calls.isEmpty()) {
            out.println("Використання обробників (успішні завдання):");
            for (Map.Entry<String, Integer> e : calls) {
                out.println(String.format("  %-8s %d", e.getKey(), e.getValue()));
            }
        }
        row("Час виконання завдань:", String.format(Locale.ROOT, "%.4f с", stats.elapsedSeconds()));
    }

    static String fmt(double x) {
        if (x == Math.rint(x) && Math.abs(x) < 1e15) {
            return Long.toString((long) x);
        }
        BigDecimal rounded = new BigDecimal(x).round(new MathContext(10)).stripTrailingZeros();
        double abs = Math.abs(x);
        return (abs >= 1e15 || abs < 1e-4) ? rounded.toString() : rounded.toPlainString();
    }
}