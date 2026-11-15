class Solution {
    public int[] findOrder(int numCourses, int[][] prerequisites) {
        List<List<Integer>> adjList = new ArrayList<>();
        int [] indegree = new int[numCourses];

        for(int i=0; i<numCourses; i++) {
            adjList.add(new ArrayList<>());
        }

        for(int[] i: prerequisites) {
            int course = i[0];
            int prereq = i[1];
            indegree[course]++;
            adjList.get(prereq).add(course);
        }

        Queue<Integer> queue = new LinkedList<>();
        for(int i=0;i<numCourses;i++) {
            if(indegree[i]==0) {
                queue.offer(i); // 0
            }
        }

        int index = 0;
        int res[] = new int[numCourses];
        while(!queue.isEmpty()) {
            Integer currentCourse = queue.poll();
            res[index++] = currentCourse;
            Iterator<Integer> iter = adjList.get(currentCourse).listIterator();
            while(iter.hasNext()){
                int item = iter.next();
                indegree[item]--;
                if(indegree[item] == 0) {
                    queue.offer(item);
                }
            }
        }

        return index==numCourses? res: new int[0];

    }
}
