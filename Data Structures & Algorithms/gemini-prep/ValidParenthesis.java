import java.util.*;

public class ValidParenthesis {
    public boolean isValidParenthesis(String s) {

        Deque<Character> stack = new ArrayDeque<>();

        for(char c: s.toCharArray()) {
            if(c == '{') {
                stack.push('}');
            } else if (c == '[') {
                stack.push(']');
            } else if (c == '(') {
                stack.push(')');
            } else if (stack.isEmpty() || c != stack.pop()) {
                return false;
            }
        }
        return stack.isEmpty();
    }
}