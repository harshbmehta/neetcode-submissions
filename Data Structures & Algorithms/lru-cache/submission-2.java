class LRUCache {
    Map<Integer, Node> cache;
    int capacity;

    Node left;
    Node right;

    LRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<Integer, Node>();

        this.left = new Node(-1, -1);
        this.right = new Node(-1, -1);

        this.left.next = right;
        this.right.prev = left;
    }

    public Integer get(int key) {
        if(cache.containsKey(key)) {
            Node n = cache.get(key);
            remove(n);
            insert(n);
            return n.val;
        }

        return -1;
    }

    private void insert(Node n) {
        Node temp = left.next;
        left.next = n;
        n.prev = left;

        temp.prev = n;
        n.next = temp;
    }

    public void put(int key, int val) {
        if(cache.containsKey(key)) {
            remove(cache.get(key));
        }

        Node newNode = new Node(key, val);
        insert(newNode);
        cache.put(key,newNode);

        if(cache.size() > capacity) {
            Node lru = right.prev;
            remove(lru);
            cache.remove(lru.key);
        }
    }

    private void remove(Node n) {
        Node nxt = n.next;
        Node prv = n.prev;

        nxt.prev = prv;
        prv.next = nxt;
    }

    private class Node {
        int val;
        int key;

        Node next;
        Node prev;

        Node(int key, int val) {
            this.key = key;
            this.val = val;
        }
    }
}