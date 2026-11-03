class Solution {
    public List<List<String>> groupAnagrams(String[] strs) {

        Map<String, List<String>> strMap = new HashMap<>();

        for(String s: strs){
            char[] sArr = s.toCharArray();
            Arrays.sort(sArr);
            String sSorted = String.valueOf(sArr);
            if(!strMap.containsKey(sSorted)) {
                strMap.put(sSorted, new ArrayList<>());
            }
            strMap.get(sSorted).add(s);
        }

        return new ArrayList<>(strMap.values());
    
    }
}
