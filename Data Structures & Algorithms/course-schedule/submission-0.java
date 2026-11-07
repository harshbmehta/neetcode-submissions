class Solution {
    public boolean canFinish(int numCourses, int[][] prerequisites) {
        int []indegree = new int[numCourses];
        List<List<Integer>> adjList = new ArrayList<>();

        for(int i=0;i<numCourses; i++) {
            adjList.add(new ArrayList<>());
        }

        for(int[] prereq :prerequisites) {
            int course = prereq[0];
            int pre = prereq[1];
            adjList.get(pre).add(course);
            indegree[course]++;
        }

        Queue<Integer> q = new LinkedList<>();
        for (int i = 0; i < numCourses; i++) {
            if (indegree[i] == 0) {
                q.offer(i);
            }
        }

        int finish = 0;
        while (!q.isEmpty()) {
            int node = q.poll();
            finish++;
            for (int nei : adjList.get(node)) {
                indegree[nei]--;
                if (indegree[nei] == 0) {
                    q.add(nei);
                }
            }
        }

        return finish == numCourses;


    }
}
