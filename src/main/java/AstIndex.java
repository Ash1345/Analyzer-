import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AstIndex {

    // Raw Clang AST nodes
    private final Map<String, AstNode> nodesById =
            new HashMap<>();

    /*
     * Raw Clang AST ID -> canonical Analyzer++ entity
     *
     * Important:
     * Multiple raw AST IDs may point to the SAME
     * canonical CodeEntity.
     */
    private final Map<String, CodeEntity> entitiesByAstId =
            new HashMap<>();

    /*
     * Logical compiler identity -> canonical entity
     *
     * Example:
     * ?add@Calculator@@QEAAHHH@Z
     */
    private final Map<String, CodeEntity> entitiesByLogicalId =
            new HashMap<>();

    /*
     * Canonical entities only.
     *
     * We keep this separately because entitiesByAstId
     * may contain multiple keys pointing to the same entity.
     */
    private final Set<CodeEntity> canonicalEntities =
            new LinkedHashSet<>();


    // =========================================================
    // AST NODE INDEX
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

            for (AstNode child : node.inner) {
                build(child);
            }
        }
    }


    public AstNode findById(String id) {

        if (id == null) {
            return null;
        }

        return nodesById.get(id);
    }


    // =========================================================
    // ENTITY REGISTRATION
    // =========================================================

    public void addEntity(CodeEntity entity) {

        if (entity == null) {
            return;
        }

        canonicalEntities.add(entity);

        /*
         * Register raw AST ID as an alias
         * to the canonical entity.
         */
        if (entity.astId != null) {

            entitiesByAstId.put(
                    entity.astId,
                    entity
            );
        }

        /*
         * Transitional fallback.
         *
         * At the moment entity.id is still generally
         * equal to the Clang AST ID.
         */
        if (entity.id != null) {

            entitiesByAstId.put(
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


    // =========================================================
    // ENTITY RESOLUTION
    // =========================================================

    public CodeEntity resolveEntity(String astId) {

        if (astId == null) {
            return null;
        }

        return entitiesByAstId.get(astId);
    }


    public CodeEntity findEntityById(String id) {

        return resolveEntity(id);
    }


    public CodeEntity findEntityByLogicalId(
            String logicalId) {

        if (logicalId == null) {
            return null;
        }

        return entitiesByLogicalId.get(
                logicalId
        );
    }


    // =========================================================
    // CANONICAL ENTITY REGISTRATION
    // =========================================================

    public CodeEntity findOrCreateEntity(
            CodeEntity entity) {

        if (entity == null) {
            return null;
        }


        // -----------------------------------------------------
        // 1. Try logical identity first
        // -----------------------------------------------------

        if (entity.logicalId != null) {

            CodeEntity existing =
                    entitiesByLogicalId.get(
                            entity.logicalId
                    );

            if (existing != null) {

                /*
                 * Very important:
                 *
                 * Even though this entity is a duplicate,
                 * its raw AST ID must still resolve to the
                 * canonical entity.
                 */
                registerAstAlias(
                        entity,
                        existing
                );

                return existing;
            }
        }


        // -----------------------------------------------------
        // 2. Try raw AST identity
        // -----------------------------------------------------

        String rawAstId =
                entity.astId != null
                        ? entity.astId
                        : entity.id;

        if (rawAstId != null) {

            CodeEntity existing =
                    entitiesByAstId.get(
                            rawAstId
                    );

            if (existing != null) {

                return existing;
            }
        }


        // -----------------------------------------------------
        // 3. This is a new canonical entity
        // -----------------------------------------------------

        addEntity(entity);

        return entity;
    }


    // =========================================================
    // AST ALIAS REGISTRATION
    // =========================================================

    private void registerAstAlias(
            CodeEntity duplicateEntity,
            CodeEntity canonicalEntity) {

        if (duplicateEntity == null
                || canonicalEntity == null) {

            return;
        }

        if (duplicateEntity.astId != null) {

            entitiesByAstId.put(
                    duplicateEntity.astId,
                    canonicalEntity
            );
        }

        /*
         * Transitional support while CodeEntity.id
         * still contains raw AST IDs.
         */
        if (duplicateEntity.id != null) {

            entitiesByAstId.put(
                    duplicateEntity.id,
                    canonicalEntity
            );
        }
    }


    // =========================================================
    // CANONICAL ENTITY COLLECTION
    // =========================================================

    public List<CodeEntity> getAllEntities() {

        return new ArrayList<>(
                canonicalEntities
        );
    }
}