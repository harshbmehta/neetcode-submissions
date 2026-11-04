import java.util.HashMap;
import java.util.Map;

public class Attempt1 {
    public String minWindow(String s, String t) {
        if (t.isEmpty()) return "";
        Map<Character, Integer> countT = new HashMap<>();

        for (char c : t.toCharArray()) {
            countT.put(c, countT.getOrDefault(c, 0) + 1);
        }

        Map<Character, Integer> window = new HashMap<>();
        
        int have = 0;
        int need = countT.size();
        int left = 0;
        int[] res = {-1, -1};
        int resLen = Integer.MAX_VALUE;

        for(int r=0; r<s.length(); r++) {
            char c = s.charAt(r);
            window.put(c, window.getOrDefault(c, 0) + 1);

            if(countT.containsKey(c) && window.get(c).equals(countT.get(c))) {
                have++;
            }

            while(have == need) {
                if((r - left + 1) < resLen) {
                    resLen = r-left+1;
                    res[0] = left;
                    res[1] = r;
                }

                char leftChar = s.charAt(left);
                window.put(leftChar, window.get(leftChar) - 1);
                if(countT.containsKey(leftChar) && window.get(leftChar) < countT.get(leftChar)) {
                    have--;
            }
                left++;
            }
        }

        return resLen == Integer.MAX_VALUE ? "" : s.substring(res[0], res[1] + 1);
    }

    public static void main(String[] args) {
        Attempt1 solution = new Attempt1();
        String s = "ADOBECODEBANC";
        String t = "ABC";
        String result = solution.minWindow(s, t);
        System.out.println("Minimum window substring: " + result);
        
        s = "AAAAA";
        t = "AA";
        result = solution.minWindow(s, t);
        System.out.println("Minimum window substring: " + result);

        s = "AAAAA";
        t = "B";
        result = solution.minWindow(s, t);
        System.out.println("Minimum window substring: " + result);
    }
}
