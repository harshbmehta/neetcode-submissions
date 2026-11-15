import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class KeyValue {
    Map<String, String> keyValMap;
    Map<String, String> overlay;
    public final static String TOMBSTONE = new String("<DELETED>");

    KeyValue() {
        keyValMap = new ConcurrentHashMap<>();
        overlay = new HashMap<>();
        }

    public void put(String key, String val) {
        keyValMap.put(key, val);
    }

    public String get(String key) {
        if(overlay != null && overlay.containsKey(key)) {
            String val = overlay.get(key);
            if(val.equals(TOMBSTONE)) {
                return null;
            }
            return val;
        }
        return keyValMap.get(key);
    }

    public void delete(String key) {
         keyValMap.remove(key); 
    }

    public void begin() {
        if(overlay != null && !overlay.isEmpty()) {
            throw new IllegalStateException("Transaction already in progress");
        }
        overlay = new HashMap<>();
    }

    public void commit() {
        if(overlay == null || overlay.isEmpty()) {
            throw new IllegalStateException("No transaction in progress");
        }
        for(Map.Entry<String, String> entry : overlay.entrySet()) {
            if(entry.getValue().equals(TOMBSTONE)) {
                keyValMap.remove(entry.getKey());
            } else {
                keyValMap.put(entry.getKey(), entry.getValue());
            }
        }
        overlay = null;
    }

    public void rollback() {
        if(overlay == null || overlay.isEmpty()) {
            throw new IllegalStateException("No transaction in progress");
        }
        overlay = null;
    }

    public void write(String key, String val) {
        if(overlay != null) {
            overlay.put(key, val);
        } else if(val.equals(TOMBSTONE)) {
            keyValMap.remove(key);
        } else {
            keyValMap.put(key, val);
        }
    }


    public static void main() throws Exception {
        KeyValue cache = new KeyValue();
        cache.put("type", "a");
        cache.put("type2", "b");
        cache.put("type3", "5");

        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Future<?>> futures = new ArrayList<>();
        for (int t = 0; t < 8; t++) {
            final int id = t;
            futures.add(pool.submit(() -> {
                for (int i = 0; i < 50_000; i++) cache.put("k" + id + "_" + i, "v");
            }));
        }
        for (Future<?> f : futures) f.get();
        pool.shutdown();
        System.out.println("Expected size: 400000, actual size: " + cache.keyValMap.size());

    }
}
