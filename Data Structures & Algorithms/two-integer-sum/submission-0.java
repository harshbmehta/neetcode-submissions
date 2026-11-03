class Solution {
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> numMap = new HashMap<>();
        int[] res = new int[2];
        for(int i=0; i<nums.length;i++) {
            if(numMap.containsKey(target-nums[i])) {
                res[1] = i;
                res[0] = numMap.get(target-nums[i]);
            }
            numMap.put(nums[i], i);
        }
        return res;
    }
}
