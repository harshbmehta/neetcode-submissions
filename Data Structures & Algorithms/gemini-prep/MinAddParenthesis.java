
public class MinAddParenthesis {
    public int minAddToMakeValid(String s) {
        int unopenedCt = 0;
        int unclosedCt = 0;

        for (char c: s.toCharArray()) {
            if(c == '(') {
                unclosedCt++; // 1,2,3
            }
            if (c == ')') {
                if(unclosedCt > 0) {
                    unclosedCt--;
                } else {
                    unopenedCt++;
                }
            }
        }
        return unclosedCt+unopenedCt;
    }
    
}
