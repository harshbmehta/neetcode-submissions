public class Codec {

    // 1. Encodes a tree to a single string.
    public String serialize(TreeNode root) {
        List<String> res = new ArrayList<>();
        dfsSerialize(root, res);
        return String.join(",", res); // Bug Fixed: Removed Arrays.asList()
    }

    private void dfsSerialize(TreeNode root, List<String> res) {
        if (root == null) { 
            res.add("N");
            return;
        }
        res.add(String.valueOf(root.val));
        dfsSerialize(root.left, res);
        dfsSerialize(root.right, res);
    }

    // 2. Decodes your encoded data to tree.
    public TreeNode deserialize(String data) {
        // Load the split array directly into a Queue for O(1) polling
        Queue<String> queue = new LinkedList<>(Arrays.asList(data.split(",")));
        return deserdfs(queue);
    }

    private TreeNode deserdfs(Queue<String> queue) {
        String current = queue.poll(); // Automatically advances the "pointer"
        
        if (current.equals("N")) { // Bug Fixed: String .equals() check
            return null;
        }
        
        TreeNode node = new TreeNode(Integer.parseInt(current));
        
        // Because it was serialized Pre-Order (Node, Left, Right),
        // we MUST reconstruct the left branch fully before touching the right.
        node.left = deserdfs(queue);
        node.right = deserdfs(queue);
        
        return node;
    }
}