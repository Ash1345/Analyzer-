import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AstIndex {

    // Raw Clang AST nodes
    private final Map<String, AstNode> nodesById =
            new HashMap<>();

    // Analyzer++ entities by Analyzer++ ID
    private final Map<String, CodeEntity> entitiesById =
            new HashMap<>();

    // Analyzer++ entities by logical identity
    private final Map<String, CodeEntity> entitiesByLogicalId =
            new HashMap<>();


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

            for (AstNode child : node.inner) {

                build(child);
            }
        }
    }


    public AstNode findById(String id) {

        return nodesById.get(id);
    }


    public void addEntity(CodeEntity entity) {

        if (entity == null) {
            return;
        }

        if (entity.id != null) {

            entitiesById.put(
                    entity.id,
                    entity
            );
        }

        if (entity.logicalId != null) {

            entitiesByLogicalId.put(
                    entity.logicalId,
                    entity
            );
        }
    }


    public CodeEntity findEntityById(String id) {

        return entitiesById.get(id);
    }


    public CodeEntity findEntityByLogicalId(
            String logicalId) {

        return entitiesByLogicalId.get(
                logicalId
        );
    }


    public List<CodeEntity> getAllEntities() {

        return new ArrayList<>(
                entitiesById.values()
        );
    }


    public CodeEntity findOrCreateEntity(
            CodeEntity entity) {

        if (entity == null) {
            return null;
        }

        /*
         * First try Analyzer++ logical identity.
         *
         * Example:
         *
         * ?add@Calculator@@QEAAHHH@Z
         *
         * If another AST already produced an entity
         * with this logical ID, return that entity.
         */
        if (entity.logicalId != null) {

            CodeEntity existing =
                    entitiesByLogicalId.get(
                            entity.logicalId
                    );

            if (existing != null) {

                return existing;
            }
        }


        /*
         * No logical match.
         *
         * Fall back to the raw AST identity.
         */
        if (entity.id != null) {

            CodeEntity existing =
                    entitiesById.get(
                            entity.id
                    );

            if (existing != null) {

                return existing;
            }
        }


        /*
         * This is a genuinely new entity.
         */
        addEntity(entity);

        return entity;
    }

    public CodeEntity resolveEntity(
            String astId) {

        if (astId == null) {
            return null;
        }

        return entitiesById.get(astId);
    }
}