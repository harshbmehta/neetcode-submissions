/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode() {}
 *     TreeNode(int val) { this.val = val; }
 *     TreeNode(int val, TreeNode left, TreeNode right) {
 *         this.val = val;
 *         this.left = left;
 *         this.right = right;
 *     }
 * }
 */

class Solution {
    List<Integer> res = new ArrayList<>();
    public int kthSmallest(TreeNode root, int k) {
        inOrderDFS(root);
        return res.get(k-1);
    }

    private void inOrderDFS(TreeNode node) {
        if(node == null) return;

        inOrderDFS(node.left);
        res.add(node.val);
        inOrderDFS(node.right);
    }
}
