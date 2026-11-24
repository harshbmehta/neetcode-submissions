public class RevLinkedList {
    class ListNode {
    int val;
    ListNode next;
    ListNode() {}
    ListNode(int val) { this.val = val; }
    ListNode(int val, ListNode next) { this.val = val; this.next = next; }

}

 public ListNode revll(ListNode root) {
    if (root == null) return null;
    ListNode prev = null;
    while(root !=null) {
        ListNode temp = root.next;
        root.next = prev;
        prev = root;
        root = temp;
    }
    return prev;
 }
}
