class Solution {
    public boolean hasDuplicate(int[] nums) {
        Set<Integer> uniqueSet = HashSet.newHashSet(nums.length);
        for(Integer i: nums) {
            if(!uniqueSet.add(i)) {
                return true;
            }
        }
        return false;
    }
}