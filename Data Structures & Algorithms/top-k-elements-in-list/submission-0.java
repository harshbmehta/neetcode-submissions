class Solution {
    public int[] topKFrequent(int[] nums, int k) {
        int[] res = new int[k];
        Map<Integer, Integer> freqMap = new HashMap<>();
        for(int i: nums) {
            freqMap.put(i, freqMap.getOrDefault(i, 0)+1);
        }

        PriorityQueue<int[]> heap = new PriorityQueue<>((a,b) -> a[0]-b[0]);

        for(Map.Entry<Integer, Integer> entry: freqMap.entrySet()) {
            heap.offer(new int[] {entry.getValue(), entry.getKey()});
            if(heap.size() > k) {
                heap.poll();
            }
        }

        for(int i=0; i<k;i++) {
            res[i] = heap.poll()[1];
        }

return res;


    }
}
