import java.util.HashMap;
import java.util.Map;

public class AstIndex {

    // =========================================================
    // RAW CLANG AST NODE INDEX
    // =========================================================

    private final Map<String, AstNode> nodesById =
            new HashMap<>();


    // =========================================================
    // BUILD AST INDEX
    // =========================================================

    public void build(AstNode node) {

        if (node == null) {
            return;
        }

        if (node.id != null) {

            nodesById.put(
                    node.id,
                    node
            );
        }

        if (node.inner != null) {

            for (AstNode child :
                    node.inner) {

                build(child);
            }
        }
    }


    // =========================================================
    // AST NODE RESOLUTION
    // =========================================================

    public AstNode findById(String id) {

        if (id == null) {
            return null;
        }

        return nodesById.get(id);
    }
}