/**
 * Definition of Interval:
 * public class Interval {
 *     public int start, end;
 *     public Interval(int start, int end) {
 *         this.start = start;
 *         this.end = end;
 *     }
 * }
 */

class Solution {
    public boolean canAttendMeetings(List<Interval> intervals) {
        intervals.sort(Comparator.comparingInt(i -> i.end));

        for(int i=1;i<intervals.size();i++) {
            Interval prev = intervals.get(i-1);
            Interval curr = intervals.get(i);
            if(curr.start < prev.end) {
                return false;
            }
        }
         return true;
    }
}
