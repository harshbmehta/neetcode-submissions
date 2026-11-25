/*

There are n cities. Some are directly connected and some aren't, but connections can chain: if city a is connected to b, and b is connected to c, then a and c are connected indirectly.

A province is a group of cities that are directly or indirectly connected, with no connection to any city outside the group.

You get an n x n matrix isConnected, where isConnected[i][j] = 1 means cities i and j are directly connected, and 0 means they aren't. Return the total number of provinces.


Example 1
isConnected = [[1,1,0],
               [1,1,0],
               [0,0,1]]
Output: 2          provinces: {0,1} and {2}

Example 2
isConnected = [[1,0,0],
               [0,1,0],
               [0,0,1]]
Output: 3          every city is alone

Example 3
isConnected = [[1,1,1],
               [1,1,0],
               [1,0,1]]
Output: 1          0 links to 1 and 2, so all three are one province
*/
public class NumberOfProvinces {
    public int findConnectedProvinces(int [][]roads) {
        UnionFind uf = new UnionFind(0, roads);

    }

    private static class UnionFind {
        int components;
        int []parents;
        int [] size;
        UnionFind(int n, int[][] edges) {
            components = n;
            parents = new int[n];
            size = new int[n];
            for(int i=0; i<n; i++) {
                parents[i] = i;
                size[i] = 1;
            }
        }

        int find(int x) {
            if(parents[x] != x) parents[x] = find(parents[x]);
            return parents[x];
        }

        boolean union(int x, int y) {
            int rx = find(x);
            int ry = find(y);

            if(rx == ry) return false;

            if(size[rx] < size[ry]) {
                parents[rx] = ry;
                size[ry] += size[rx];
            } else {
                parents[ry] = rx;
                size[rx] += size[ry];
            }

            components--;
            return true;
        }

        int connComponents() {
            return components;
        }
    }
}
