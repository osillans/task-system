import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Statistics {

    private final List<Double> results = new ArrayList<>(); // результати успішних завдань
    private final Map<String, Integer> handlerCalls = new HashMap<>();
    private int failed;
    private long elapsedNanos;

    public void recordSuccess(Task task, double result) {
        results.add(result);
        for (String name : task.getHandlers()) {
            Integer count = handlerCalls.get(name);
            handlerCalls.put(name, count == null ? 1 : count + 1);
        }
    }

    public void recordFailure() {
        failed++;
    }

    public void addElapsed(long nanos) {
        elapsedNanos += nanos;
    }

    public int total() {
        return results.size() + failed;
    }

    public int succeeded() {
        return results.size();
    }

    public int failed() {
        return failed;
    }

    public boolean hasResults() {
        return !results.isEmpty();
    }

    public double min() {
        return Collections.min(results);
    }

    public double max() {
        return Collections.max(results);
    }

    public double mean() {
        double sum = 0;
        for (double r : results) {
            sum += r;
        }
        return sum / results.size();
    }

    public double elapsedSeconds() {
        return elapsedNanos / 1e9;
    }

    public List<Map.Entry<String, Integer>> handlerCallsSorted() {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(handlerCalls.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                int byCount = b.getValue().compareTo(a.getValue());
                return byCount != 0 ? byCount : a.getKey().compareTo(b.getKey());
            }
        });
        return list;
    }
}