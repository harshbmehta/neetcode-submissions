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
    public List<Integer> rightSideView(TreeNode root) {
        Queue<TreeNode> qu = new LinkedList<>();
        List<Integer> res= new ArrayList();

        if(root != null) qu.offer(root);

        while(!qu.isEmpty()) {
            int currSize = qu.size();
            for(int i=1; i<=currSize;i++) {
                TreeNode curr = qu.poll();
                if(currSize == i) {
                    res.add(curr.val);
                }
                if(curr.left != null) qu.offer(curr.left);
                if(curr.right != null) qu.offer(curr.right);
            }
            
        }
        return res;
    }
}
