class Solution {
    public boolean hasDuplicate(int[] nums) {
        if(nums.length == 0) {
            return false;
        }
        Set<Integer> uniqueSet = HashSet.newHashSet(nums.length);
        for(int i=0; i<nums.length;i++) {
            if(!uniqueSet.add(nums[i])) {
                return true;
            }
        }
        return false;
    }
}