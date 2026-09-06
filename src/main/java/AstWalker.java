public class AstWalker {

    public static void walk(AstNode node, int depth) {

        if (node == null) {
            return;
        }

        String indentation = "  ".repeat(depth);

        System.out.println(
                indentation +
                        node.kind +
                        (node.name != null ? " : " + node.name : "")
        );

        if (node.inner != null) {
            for (AstNode child : node.inner) {
                walk(child, depth + 1);
            }
        }
    }
}