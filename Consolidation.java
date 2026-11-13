import java.util.List;
public enum Consolidation {

    SUM("sum", "складання", r -> r.stream().mapToDouble(d -> d).sum()),
    PRODUCT("product", "добуток", r -> r.stream().mapToDouble(d -> d).reduce(1.0, (a, b) -> a * b)),
    MAX("max", "максимум", r -> r.stream().mapToDouble(d -> d).max().getAsDouble()),
    MIN("min", "мінімум", r -> r.stream().mapToDouble(d -> d).min().getAsDouble()),
    MEAN("mean", "середнє арифметичне", r -> r.stream().mapToDouble(d -> d).average().getAsDouble());

    @FunctionalInterface
    public interface Rule {
        double apply(List<Double> results);
    }

    private final String code;
    private final String label;
    private final Rule rule;

    Consolidation(String code, String label, Rule rule) {
        this.code = code;
        this.label = label;
        this.rule = rule;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public double apply(List<Double> results) {
        return rule.apply(results);
    }

    public static Consolidation fromCode(String code) {
        for (Consolidation c : values()) {
            if (c.code.equalsIgnoreCase(code)) {
                return c;
            }
        }
        throw new IllegalArgumentException("невідома функція консолідації «" + code
                + "» (доступні: " + codes() + ")");
    }

    public static String codes() {
        StringBuilder sb = new StringBuilder();
        for (Consolidation c : values()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(c.code);
        }
        return sb.toString();
    }
}