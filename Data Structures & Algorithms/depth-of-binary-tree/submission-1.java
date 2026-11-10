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
    public int maxDepth(TreeNode root) {
        if(root == null) return 0;

        Queue<TreeNode> treeQueue = new LinkedList<>();
        int depth = 0;
        treeQueue.offer(root);

        while(!treeQueue.isEmpty()) {
            int len = treeQueue.size();
            for (int i = 0; i < len; i++) {
                TreeNode node = treeQueue.poll();
                if (node.left != null) {
                    treeQueue.add(node.left);
                }
                if (node.right != null) {
                    treeQueue.add(node.right);
                }
            }
            depth++;
        }

        return depth;
    }
}
