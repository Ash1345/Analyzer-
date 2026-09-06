import java.util.HashMap;
import java.util.Map;

public class AstIndex {

    private final Map<String, AstNode> nodesById = new HashMap<>();

    private final Map<String, String> methodOwners = new HashMap<>();

    public void build(AstNode node) {

        build(node, null);
    }

    private void build(AstNode node, String currentClass) {

        if (node == null) {
            return;
        }

        if (node.id != null) {
            nodesById.put(node.id, node);
        }

        // Remember the current class
        if ("CXXRecordDecl".equals(node.kind)
                && node.name != null) {

            currentClass = node.name;
        }

        // Remember which class owns each method
        if ("CXXMethodDecl".equals(node.kind)
                && node.id != null
                && currentClass != null) {

            methodOwners.put(node.id, currentClass);
        }

        if (node.inner != null) {

            for (AstNode child : node.inner) {

                build(child, currentClass);
            }
        }
    }

    public AstNode findById(String id) {

        return nodesById.get(id);
    }

    public String findMethodOwner(String methodId) {

        return methodOwners.get(methodId);
    }
}