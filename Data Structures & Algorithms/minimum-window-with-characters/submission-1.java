class Solution {
    public String minWindow(String s, String t) {
        if(t.isEmpty()) {
            return "";
        } 
        Map<Character, Integer> countT = new HashMap<>();
        for(char c: t.toCharArray()) {
            countT.put(c, countT.getOrDefault(c, 0) + 1); // A:1,B:1,C:1
        }
        Map<Character, Integer> window = new HashMap<>();
        int need = countT.size();
        int have = 0;
        int l =0;
        int[] resIdx = {-1, -1};
        int resLen = Integer.MAX_VALUE;

        for(int r=0; r<s.length(); r++) { //r=0,1,2,3,4,5,6,7,8,9,10,11,12
            char c = s.charAt(r); // AD0BEC0DEBAN
            window.put(c, window.getOrDefault(c, 0) + 1); // A:1,D:1,0:1,B:1,E:1,C:1,N:1

            if(countT.containsKey(c) && window.get(c).equals(countT.get(c))) {
                    have++; //have = 1,2,3,2,3,2
                }
            while(have == need) { //5-0+1=6,10-1+1=10,10-2+1=9,10-3+1=8,7,6,12-6+1=7,12-7+1=6,12-8+1=5,12-9+1=4,
               if((r-l+1) < resLen) {
                    resLen = r-l+1; //5-0+1=6,12-8+1=5,12-9=4
                    resIdx[0] = l; //0,8,9
                    resIdx[1] = r; // 5,12,12
               }

               char leftChar = s.charAt(l); // A,D,0,B,E,C,0,D,E,B
               window.put(leftChar, window.get(leftChar) - 1); // A:1,D:0,0:0,B:0,E:0,C:0
               if(countT.containsKey(leftChar) && window.get(leftChar) < countT.get(leftChar)) {
                have--; //2,3,2
               }

               l++;//1,2,3,4,5,6,7,8,9
            }
     
        }
                return resLen == Integer.MAX_VALUE ? "" : s.substring(resIdx[0], resIdx[1]+1);
}}
