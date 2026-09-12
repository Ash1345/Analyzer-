import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AstIndex {

    private final Map<String, AstNode> nodesById =
            new HashMap<>();

    private final Map<String, CodeEntity> entitiesById =
            new HashMap<>();

    public void build(AstNode node) {

        if (node == null) {
            return;
        }

        if (node.id != null) {
            nodesById.put(node.id, node);
        }

        if (node.inner != null) {

            for (AstNode child : node.inner) {
                build(child);
            }
        }
    }

    public AstNode findById(String id) {
        return nodesById.get(id);
    }

    public void addEntity(CodeEntity entity) {

        if (entity != null
                && entity.id != null) {

            entitiesById.put(
                    entity.id,
                    entity
            );
        }
    }

    public CodeEntity findEntityById(String id) {
        return entitiesById.get(id);
    }

    public List<CodeEntity> getAllEntities() {

        return new ArrayList<>(
                entitiesById.values()
        );
    }
}