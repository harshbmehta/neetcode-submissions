class Solution {
    public boolean hasDuplicate(int[] nums) {
        Set dupSet= new HashSet<>();
        for(int i=0;i<nums.length;i++) {
            if(!dupSet.add(nums[i])) {
                return true;
            }
        }
        return false;
        
    }
}