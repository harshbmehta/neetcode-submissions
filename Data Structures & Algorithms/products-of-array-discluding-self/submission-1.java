class Solution {
    public int[] productExceptSelf(int[] nums) {
        int left = 1;
        int[] res = new int[nums.length];

        res[0] = 1;
        for(int i=1;i<nums.length;i++) {
            left = left * nums[i-1];//1*1 = 1; //2//8
            res[i] = left;
        }
        //[1,1,2,8]

        int right = 1;
        for(int i=nums.length-2;i>=0; i--) {
            right = right * nums[i+1]; // 6 // 24//48
            res[i] = res[i] * right; // res[2] = 12 //res[1] = 24//
        }

        return res;
    }
}  
