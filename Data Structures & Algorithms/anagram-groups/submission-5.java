class Solution {
    public List<List<String>> groupAnagrams(String[] strs) {
        Map<String, List<String>> strMap = new HashMap<>();

        for (String s : strs) {
            // Sort the string to create the key
            char[] sArr = s.toCharArray();
            Arrays.sort(sArr);
            String sSorted = String.valueOf(sArr);
            
            // computeIfAbsent handles the initialization of the list automatically
            strMap.computeIfAbsent(sSorted, k -> new ArrayList<>()).add(s);
        }

        return new ArrayList<>(strMap.values());
    }
}