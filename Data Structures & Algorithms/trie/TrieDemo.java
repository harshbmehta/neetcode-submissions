import java.util.*;

/**
 * Trie tutorial: basic trie -> prefix autocomplete -> ranked top-k autocomplete.
 * Run: javac TrieDemo.java && java TrieDemo
 */
public class TrieDemo {

    // =====================================================================
    // STAGE 1 + 2: basic trie with insert / search / startsWith / autocomplete
    // =====================================================================
    static class TrieNode {
        // TreeMap keeps children alphabetical (O(log k) per lookup, k <= alphabet size).
        // Swap for HashMap (faster, unordered) or TrieNode[26] (fastest, a-z only).
        final Map<Character, TrieNode> children = new TreeMap<>();
        boolean isEnd; // true if a complete word ends at this node
    }

    static class Trie {
        private final TrieNode root = new TrieNode();

        /** O(L) where L = word length. */
        public void insert(String word) {
            TrieNode node = root;
            for (char c : word.toCharArray()) {
                node = node.children.computeIfAbsent(c, k -> new TrieNode());
            }
            node.isEnd = true;
        }

        /** Walk the prefix; return the node it ends on, or null if the path doesn't exist. */
        private TrieNode findNode(String s) {
            TrieNode node = root;
            for (char c : s.toCharArray()) {
                node = node.children.get(c);
                if (node == null) return null;
            }
            return node;
        }

        /** Exact word match. "car" is a word; "ca" is only a prefix. */
        public boolean search(String word) {
            TrieNode node = findNode(word);
            return node != null && node.isEnd;
        }

        /** Does any stored word start with this prefix? */
        public boolean startsWith(String prefix) {
            return findNode(prefix) != null;
        }

        /** Stage 2: all words starting with prefix. O(L + size of subtree). */
        public List<String> autocomplete(String prefix) {
            List<String> results = new ArrayList<>();
            TrieNode start = findNode(prefix);
            if (start == null) return results;
            collect(start, new StringBuilder(prefix), results);
            return results;
        }

        // DFS with backtracking on a shared StringBuilder (avoids O(L) string copies per step)
        private void collect(TrieNode node, StringBuilder path, List<String> out) {
            if (node.isEnd) out.add(path.toString());
            for (Map.Entry<Character, TrieNode> e : node.children.entrySet()) {
                path.append(e.getKey());
                collect(e.getValue(), path, out);
                path.deleteCharAt(path.length() - 1); // backtrack
            }
        }
    }

    // =====================================================================
    // STAGE 3: ranked autocomplete - return top-k by frequency
    // =====================================================================
    static class RankedTrie {
        static class Node {
            final Map<Character, Node> children = new TreeMap<>();
            int frequency; // > 0 means a word ends here; value = how popular it is
        }

        private final Node root = new Node();

        public void insert(String word, int freq) {
            Node node = root;
            for (char c : word.toCharArray()) {
                node = node.children.computeIfAbsent(c, k -> new Node());
            }
            node.frequency += freq; // repeated searches bump the count
        }

        /** Top-k completions of prefix, most frequent first. */
        public List<String> suggest(String prefix, int k) {
            Node start = root;
            for (char c : prefix.toCharArray()) {
                start = start.children.get(c);
                if (start == null) return List.of();
            }

            // Min-heap of size k: the weakest of the current top-k sits on top
            // and gets evicted when something better shows up. O(N log k).
            PriorityQueue<Map.Entry<String, Integer>> heap =
                    new PriorityQueue<>(Map.Entry.<String, Integer>comparingByValue());
            dfs(start, new StringBuilder(prefix), k, heap);

            List<String> result = new ArrayList<>();
            while (!heap.isEmpty()) result.add(heap.poll().getKey());
            Collections.reverse(result); // heap pops lowest first
            return result;
        }

        private void dfs(Node node, StringBuilder path, int k,
                         PriorityQueue<Map.Entry<String, Integer>> heap) {
            if (node.frequency > 0) {
                heap.offer(new AbstractMap.SimpleEntry<>(path.toString(), node.frequency));
                if (heap.size() > k) heap.poll();
            }
            for (Map.Entry<Character, Node> e : node.children.entrySet()) {
                path.append(e.getKey());
                dfs(e.getValue(), path, k, heap);
                path.deleteCharAt(path.length() - 1);
            }
        }
    }

    // =====================================================================
    // Demo
    // =====================================================================
    public static void main(String[] args) {
        System.out.println("--- Stage 1: basic trie ---");
        Trie trie = new Trie();
        for (String w : List.of("cat", "car", "cart", "dog", "do")) trie.insert(w);

        System.out.println("search(car)    = " + trie.search("car"));    // true
        System.out.println("search(ca)     = " + trie.search("ca"));     // false (prefix only)
        System.out.println("startsWith(ca) = " + trie.startsWith("ca")); // true
        System.out.println("startsWith(x)  = " + trie.startsWith("x"));  // false

        System.out.println("\n--- Stage 2: autocomplete ---");
        System.out.println("ca -> " + trie.autocomplete("ca")); // [car, cart, cat]
        System.out.println("do -> " + trie.autocomplete("do")); // [do, dog]
        System.out.println("z  -> " + trie.autocomplete("z"));  // []

        System.out.println("\n--- Stage 3: ranked top-3 ---");
        RankedTrie ranked = new RankedTrie();
        ranked.insert("java", 900);
        ranked.insert("javascript", 1200);
        ranked.insert("java tutorial", 400);
        ranked.insert("java interview questions", 650);
        ranked.insert("jam", 50);
        ranked.insert("jacket", 300);

        System.out.println("ja   -> " + ranked.suggest("ja", 3));
        // [javascript, java, java interview questions]
        System.out.println("java -> " + ranked.suggest("java", 2));
        // [javascript, java]
    }
}