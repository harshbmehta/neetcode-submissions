public class RedundantConn {
    public int[] findRedundantConnection(int[][] edges) {
        UnionFind uf = new UnionFind(edges.length + 1);   // nodes are labeled 1..n, so index 0 is unused
        for (int[] e : edges) {
            if (!uf.union(e[0], e[1])) {
                return e;                                  // endpoints were already connected: this edge closes the cycle
            }
        }
        return new int[0];                                 // never reached for valid input
    }

    private static class UnionFind {
        private final int[] parent;
        private final int[] size;

        UnionFind(int n) {
            parent = new int[n];
            size = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;                             // everyone starts as their own root
                size[i] = 1;
            }
        }

        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]);               // path compression
            }
            return parent[x];
        }

        boolean union(int x, int y) {
            int rx = find(x);                              // always work with roots
            int ry = find(y);
            if (rx == ry) return false;                    // already in the same group

            if (size[rx] < size[ry]) {
                parent[rx] = ry;                           // smaller tree goes under the larger
                size[ry] += size[rx];
            } else {
                parent[ry] = rx;
                size[rx] += size[ry];
            }
            return true;
        }
    }
}