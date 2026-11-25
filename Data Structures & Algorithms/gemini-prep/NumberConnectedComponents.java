public class NumberConnectedComponents {
    private int components;
    private final int[] parent;
    private final int[] size;

    public NumberConnectedComponents(int n, int[][] edges) {
        components = n;
        parent = new int[n];
        size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;                 // each node is its own root
            size[i] = 1;
        }
        for (int[] e : edges) {
            union(e[0], e[1]);             // process every edge
        }
    }

    public int connectedComponents() {
        return components;
    }

    private boolean union(int x, int y) {
        int rx = find(x);
        int ry = find(y);
        if (rx == ry) return false;        // already in the same group

        if (size[rx] < size[ry]) {
            parent[rx] = ry;
            size[ry] += size[rx];
        } else {
            parent[ry] = rx;
            size[rx] += size[ry];
        }
        components--;                      // only on a real merge
        return true;
    }

    private int find(int x) {
        if (parent[x] != x) parent[x] = find(parent[x]);
        return parent[x];
    }
}