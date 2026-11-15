import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DFS {
    int vertices;
    boolean[] visited;
    List<Integer> [] adjList;

    @SuppressWarnings("unchecked")
    DFS(int vertices) {
        this.vertices = vertices;
        this.visited = new boolean[vertices];
        this.adjList = new LinkedList[vertices];
        for(int i=0; i<vertices;i++) {
            adjList[i] = new LinkedList<>();
        }
    }

    public void addEdges(int src, int dest) {
        adjList[src].add(dest);
    }

    public void dfsTraverse(int strt) {
        visited[strt] = true;
        System.out.println(strt + " ");
        Iterator<Integer> iter = adjList[strt].listIterator();
        while(iter.hasNext()) {
            int vis = iter.next();
            if(!visited[vis]) {
                dfsTraverse(vis);
            }
        }
    }

    public static void main() {
        DFS dfs = new DFS(4);
        dfs.addEdges(0, 1);
        dfs.addEdges(0, 2);
        dfs.addEdges(0, 3);
        dfs.addEdges(1, 3);

        dfs.dfsTraverse(0);
    }
}
