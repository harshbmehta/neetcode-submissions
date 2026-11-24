public class RevLinkedSublist {
    class ListNode {
        int val;
        ListNode next;

        ListNode() {
        }

        ListNode(int val) {
            this.val = val;
        }

        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }

    }

    public ListNode revSublist(ListNode head, int left, int right) {
        if(head == null || left == right) return head;

        ListNode dummy = new ListNode(0, head);
        ListNode preLeft = dummy;
        int count = right - left + 1;
        ListNode curr = head;

        for(int i=0; i<left-1; i++) {
            curr = curr.next;
            preLeft = preLeft.next;
        }

        ListNode subListPtr = curr;
        ListNode prev = null;
        for(int i=0; i<count;i++) {
            ListNode temp = curr.next;
            curr.next = prev;
            prev = curr;
            curr = temp;
        }

        preLeft.next = prev;
        subListPtr.next = curr;

        return dummy.next;
    }
}
