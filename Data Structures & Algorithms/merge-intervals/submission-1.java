class Solution {
    public int[][] merge(int[][] intervals) {

        Arrays.sort(intervals, (a,b) -> Integer.compare(a[0], b[0]));

        ArrayList<int[]> res = new ArrayList<>();

        for(int [] curr : intervals) {
            if(res.isEmpty() || res.get(res.size()-1)[1] < curr[0]) {
                res.add(new int[] {curr[0], curr[1]});
            } else {
                int[] lastInterval = res.get(res.size()-1);
                lastInterval[1] = Math.max(lastInterval[1], curr[1]);
            }

        }        
        return res.toArray(new int[0][]);
    }
}
