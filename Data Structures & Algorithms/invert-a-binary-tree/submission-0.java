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
    public TreeNode invertTree(TreeNode root) {
        if(root == null) return root;

        Queue<TreeNode> nodeQueue = new LinkedList<>();

        nodeQueue.offer(root);

        while(!nodeQueue.isEmpty()) {
            TreeNode curr = nodeQueue.poll();
            TreeNode temp = curr.left;
            curr.left = curr.right;
            curr.right = temp;
            if(curr.left != null) nodeQueue.offer(curr.left);
            if(curr.right != null) nodeQueue.offer(curr.right);
        }

        return root;

    }
}
