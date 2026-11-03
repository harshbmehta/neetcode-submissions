class Solution {
    public boolean isAnagram(String s, String t) {
        if(s.length() != t.length()) return false;
        int [] alphaArr = new int[26];
        char[] sArr = s.toCharArray();
        char[] tArr = t.toCharArray();
        for(int i=0; i<s.length();i++) {
            alphaArr[sArr[i] - 'a']++;
            alphaArr[tArr[i] - 'a']--;
        }

        for(int i:alphaArr)
        if(i != 0) {
            return false;
        }
        return true; 
    }
}
