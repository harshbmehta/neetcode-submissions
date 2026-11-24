/*
Problem: First Non-Repeating CharacterGiven a string s consisting of lowercase English letters, find the first non-repeating character in it and return its index. 
If no such character exists, return -1.

Examples:
Input: s = "leetcode"Output: 0 ('l' is the first character that appears only once)
Input: s = "loveleetcode"Output: 2 ('v' is the first character that appears only once)
Input: s = "aabb"Output: -1Constraints:$1 \le \text{s.length} \le 10^5$
 */

import java.util.*;

public class UniqueChar {
    public int findUnique(String s) {
        int idx = -1;
        Map<Character, List<Integer>> charMap = new HashMap<>();

        for(int i =0; i<s.length();i++) {
            charMap.computeIfAbsent(s.charAt(i), k -> new ArrayList<>()).add(i);
        }

        for(int i =0; i<s.length();i++) {
            if(charMap.get(s.charAt(i)).size() == 1) {
                return charMap.get(s.charAt(i)).get(0);
            }
        }
        return idx;
    }
}
