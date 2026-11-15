public class Solution {

    public String encode(List<String> strs) {
        StringBuilder res = new StringBuilder();
        for (String s : strs) {
            res.append(s.length()).append('#').append(s); //3#abc4#abcs
        }
        return res.toString();
    }

    public List<String> decode(String str) {
        // char[] strArr = str.toCharArray();
        // System.out.println(str);
        List<String> res = new ArrayList<>();
        int i=0;
        while(i<str.length()) {
            int j=i;
            while(str.charAt(j) != '#') {
                j++;
            }

            int length = Integer.parseInt(str.substring(i, j));
            int st = j+1;
            int end = st+length;
            res.add(str.substring(st, end));

            i = end;

        }
        return res;
    }
}