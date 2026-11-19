class Solution {
    private int remaining;
    private int result;

    public int kthSmallest(TreeNode root, int k) {
        remaining = k;
        inorder(root);
        return result;
    }

    private void inorder(TreeNode node) {
        if (node == null || remaining == 0) return;

        inorder(node.left);
        if (remaining == 0) return;          // answer already found in left subtree

        if (--remaining == 0) {              // this node is the kth visited
            result = node.val;
            return;
        }
        inorder(node.right);
    }
}