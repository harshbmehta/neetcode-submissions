public class UnionFind {
    int [] parent;
    int [] size;
    int components;

    UnionFind(int n) {
        parent = new int[n];
        size = new int[n];
        components = n;
        for(int i=0;i<n;i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    public int find(int x) {
        if(parent[x] != x) parent[x] = find(parent[x]);
        return parent[x];
    }

    public boolean union(int x, int y) {
        int ra = find(x);
        int rb = find(y);

        if(ra == rb) return false;

        if(size[ra] < size[rb]) {
            parent[ra] = parent[rb];
            size[rb] = size[rb] + size[ra];
        } else {
            parent[rb] = parent[ra];
            size[ra] = size[ra] + size[rb];
        }
        components --;
        return true;
    }
    
     boolean connected(int a, int b) { return find(a) == find(b); }
    int count()                     { return components; }
    int groupSize(int x)            { return size[find(x)]; }
    
}
