import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String HELP =
            "Система ефективного виконання завдань: серія завдань, результат кожного,\n"
          + "зведена статистика наприкінці.\n"
          + "\n"
          + "Використання: java Main [-t ЗНАЧЕННЯ:ОБРОБНИКИ]... [-c ФУНКЦІЯ] [-l] [-h]\n"
          + "\n"
          + "  -t, --task        завдання, наприклад «5: double, square»; можна вказувати\n"
          + "                    кілька разів. Якщо не задано — параметри запитуються в діалозі.\n"
          + "  -c, --consolidate функція консолідації результатів обробників\n"
          + "                    (за замовчуванням sum): " + Consolidation.codes() + "\n"
          + "  -l, --list        показати словник обробників і вийти\n"
          + "  -h, --help        ця довідка";

    public static void main(String[] args) {
        int code = run(args);
        if (code != 0) {
            System.exit(code);
        }
    }

    static int run(String[] args) {
        List<String> taskTexts = new ArrayList<>();
        Consolidation consolidation = Consolidation.SUM;
        boolean listOnly = false;

        try {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.equals("-t") || arg.equals("--task")) {
                    taskTexts.add(valueAfter(args, ++i, arg));
                } else if (arg.startsWith("--task=")) {
                    taskTexts.add(arg.substring("--task=".length()));
                } else if (arg.equals("-c") || arg.equals("--consolidate")) {
                    consolidation = Consolidation.fromCode(valueAfter(args, ++i, arg));
                } else if (arg.startsWith("--consolidate=")) {
                    consolidation = Consolidation.fromCode(arg.substring("--consolidate=".length()));
                } else if (arg.equals("-l") || arg.equals("--list")) {
                    listOnly = true;
                } else if (arg.equals("-h") || arg.equals("--help")) {
                    System.out.println(HELP);
                    return 0;
                } else {
                    throw new IllegalArgumentException("невідомий параметр «" + arg + "»");
                }
            }
        } catch (IllegalArgumentException e) {
            return usageError(e.getMessage());
        }

        UserInterface ui = new UserInterface(new Executor(), consolidation);
        if (listOnly) {
            ui.printHandlers();
            return 0;
        }

        List<Task> tasks = null;
        if (!taskTexts.isEmpty()) {
            tasks = new ArrayList<>();
            for (String text : taskTexts) {
                try {
                    tasks.add(Task.parse(text));
                } catch (IllegalArgumentException e) {
                    return usageError("некоректне завдання «" + text + "»: " + e.getMessage());
                }
            }
        }

        ui.run(tasks);
        return 0;
    }

    private static String valueAfter(String[] args, int index, String option) {
        if (index >= args.length) {
            throw new IllegalArgumentException("після " + option + " потрібне значення");
        }
        return args[index];
    }

    private static int usageError(String message) {
        System.err.println("Помилка: " + message);
        System.err.println("Довідка: java Main --help");
        return 2;
    }
}