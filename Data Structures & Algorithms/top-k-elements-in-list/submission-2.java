class Solution {
    public int[] topKFrequent(int[] nums, int k) {
        Map<Integer,int[]> numMap = new HashMap<>();
        for (int i: nums){
            if(!numMap.containsKey(i)){
                numMap.put(i, new int[]{i, 0});
            }
            numMap.get(i)[1]++;
        }
        PriorityQueue<int[]> pque = new PriorityQueue<>((a,b) -> a[1]-b[1]);

        for(int[] i: numMap.values()) {
            pque.offer(i);
            if(pque.size()>k) {
                pque.poll();
            }
        }

        int res[] = new int[k];
        int i=0;
        while(!pque.isEmpty()){
            res[i] = pque.poll()[0];
            i++;
        }
        return res; 
    }
}
