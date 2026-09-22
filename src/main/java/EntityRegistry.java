import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EntityRegistry {

    private final Map<String, CodeEntity> entitiesById =
            new LinkedHashMap<>();

    private final Map<String, CodeEntity> entitiesByAstId =
            new LinkedHashMap<>();

    private final Map<String, CodeEntity> entitiesByLogicalId =
            new LinkedHashMap<>();

    public CodeEntity add(
            CodeEntity entity) {

        if (entity == null) {
            return null;
        }

        // -----------------------------------------------------
        // First try logical identity
        // -----------------------------------------------------

        if (entity.logicalId != null) {

            CodeEntity existing =
                    entitiesByLogicalId.get(
                            entity.logicalId
                    );

            if (existing != null) {

                registerAstAlias(
                        entity,
                        existing
                );

                return existing;
            }
        }

        // -----------------------------------------------------
        // Then try Analyzer++ entity identity
        // -----------------------------------------------------

        if (entity.id != null) {

            CodeEntity existing =
                    entitiesById.get(
                            entity.id
                    );

            if (existing != null) {

                registerAstAlias(
                        entity,
                        existing
                );

                return existing;
            }
        }

        // -----------------------------------------------------
        // New canonical entity
        // -----------------------------------------------------

        if (entity.id != null) {

            entitiesById.put(
                    entity.id,
                    entity
            );
        }

        if (entity.astId != null) {

            entitiesByAstId.put(
                    entity.astId,
                    entity
            );
        }

        if (entity.logicalId != null) {

            entitiesByLogicalId.put(
                    entity.logicalId,
                    entity
            );
        }

        return entity;
    }

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

        if (duplicateEntity.id != null) {

            entitiesById.put(
                    duplicateEntity.id,
                    canonicalEntity
            );
        }
    }

    public CodeEntity findById(
            String id) {

        if (id == null) {
            return null;
        }

        return entitiesById.get(
                id
        );
    }

    public CodeEntity findByAstId(
            String astId) {

        if (astId == null) {
            return null;
        }

        return entitiesByAstId.get(
                astId
        );
    }

    public CodeEntity findByLogicalId(
            String logicalId) {

        if (logicalId == null) {
            return null;
        }

        return entitiesByLogicalId.get(
                logicalId
        );
    }

    public List<CodeEntity> getAll() {

        return new ArrayList<>(
                entitiesById.values()
        );
    }

    public int size() {

        return entitiesById.size();
    }
}