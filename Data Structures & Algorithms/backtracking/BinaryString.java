/*
Problem: given n, return every string of length n made only of '0' and '1'.

For n = 2 the answer is ["00", "01", "10", "11"].
*/

import java.util.ArrayList;
import java.util.List;

public class BinaryString {

    public List<String> binaryStrings(int n) {
        List<String> res = new ArrayList<>();
        StringBuilder sb= new StringBuilder();
        backtracking(n, sb, res);
        return res;
    }

    private void backtracking(int n, StringBuilder sb, List<String> res) {
        if(sb.length() == n) {
            res.add(String.valueOf(sb));
            return;
        }

        for(char c: new char[]{'0', '1'}) {
            sb.append(c); //CHOOSE
            backtracking(n, sb, res); //EXPLORE 
            sb.deleteCharAt(sb.length()-1); // UNCHOOSE
        }
    }


    public static void main() {
        BinaryString bs = new BinaryString();

        List<String> demo = bs.binaryStrings(2);

        System.out.println(demo);
        System.out.println(demo.size());
    } 

}