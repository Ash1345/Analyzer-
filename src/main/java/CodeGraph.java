import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class CodeGraph {

    public List<CodeEntity> entities;
    public List<Relationship> relationships;

    public CodeGraph(
            List<CodeEntity> entities,
            List<Relationship> relationships) {

        this.entities = entities;
        this.relationships = relationships;
    }

    public void printGraph() {

        System.out.println();
        System.out.println("========== CODE GRAPH ==========");

        System.out.println("Entities:");

        for (CodeEntity entity : entities) {

            System.out.println(
                    "  "
                            + entity.kind
                            + " : "
                            + entity.qualifiedName
                            + " | logicalId="
                            + entity.logicalId
                            + getTypeInformation(entity)
            );

            if (entity.parameters != null
                    && !entity.parameters.isEmpty()) {

                System.out.println("      Parameters:");

                for (Parameter parameter :
                        entity.parameters) {

                    System.out.println(
                            "        "
                                    + parameter.type
                                    + " "
                                    + parameter.name
                    );
                }
            }
        }

        System.out.println();
        System.out.println("Relationships:");

        for (Relationship relation : relationships) {

            System.out.println(
                    "  "
                            + relation.source
                            + " --"
                            + relation.type
                            + "--> "
                            + relation.target
            );

            if (relation.evidence != null
                    && !relation.evidence.isEmpty()) {

                for (RelationshipEvidence evidence :
                        relation.evidence) {

                    System.out.println(
                            "      Evidence: "
                                    + evidence.file
                                    + ":"
                                    + evidence.line
                                    + ":"
                                    + evidence.column
                    );
                }
            }
        }
    }

    private String getTypeInformation(
            CodeEntity entity) {

        if ("VARIABLE".equals(entity.kind)
                && entity.type != null) {

            return " -> type " + entity.type;
        }

        if (entity.returnType != null) {

            return " -> returns " + entity.returnType;
        }

        return "";
    }

    public List<Relationship> findRelationshipsFrom(
            String sourceId) {

        List<Relationship> result =
                new ArrayList<>();

        for (Relationship relationship :
                relationships) {

            if (sourceId.equals(relationship.sourceId)) {
                result.add(relationship);
            }
        }

        return result;
    }

    public CodeEntity findEntityById(
            String id) {

        for (CodeEntity entity : entities) {

            if (id.equals(entity.id)) {
                return entity;
            }
        }

        return null;
    }

    public List<Relationship> findRelationshipsTo(
            String targetId) {

        List<Relationship> result =
                new ArrayList<>();

        for (Relationship relationship :
                relationships) {

            if (targetId.equals(relationship.targetId)) {
                result.add(relationship);
            }
        }

        return result;
    }

    public List<CodeEntity> findEntitiesByKind(
            String kind) {

        List<CodeEntity> result =
                new ArrayList<>();

        for (CodeEntity entity : entities) {

            if (kind.equals(entity.kind)) {
                result.add(entity);
            }
        }

        return result;
    }

    public List<CodeEntity> findEntitiesByName(
            String name) {

        List<CodeEntity> result =
                new ArrayList<>();

        for (CodeEntity entity : entities) {

            if (name.equals(entity.name)) {
                result.add(entity);
            }
        }

        return result;
    }

    public List<Relationship> findRelationshipsByType(
            String type) {

        List<Relationship> result =
                new ArrayList<>();

        for (Relationship relationship :
                relationships) {

            if (type.equals(relationship.type)) {
                result.add(relationship);
            }
        }

        return result;
    }
    private boolean containsEntity(
            List<CodeEntity> entities,
            String entityId) {

        for (CodeEntity entity :
                entities) {

            if (entityId.equals(entity.id)) {
                return true;
            }
        }

        return false;
    }

    public List<CodeEntity> findRelatedEntities(
            String sourceId) {

        List<CodeEntity> result =
                new ArrayList<>();

        for (Relationship relationship :
                relationships) {

            if (sourceId.equals(
                    relationship.sourceId)) {

                CodeEntity target =
                        findEntityById(
                                relationship.targetId
                        );

                if (target != null
                        && !containsEntity(
                        result,
                        target.id)) {

                    result.add(target);
                }
            }
        }

        return result;
    }
    public List<CodeEntity> findRelatedEntitiesByRelationshipType(
            String sourceId,
            String relationshipType) {

        List<CodeEntity> result =
                new ArrayList<>();

        for (Relationship relationship :
                relationships) {

            if (sourceId.equals(
                    relationship.sourceId)
                    && relationshipType.equals(
                    relationship.type)) {

                CodeEntity target =
                        findEntityById(
                                relationship.targetId
                        );

                if (target != null
                        && !containsEntity(
                        result,
                        target.id)) {

                    result.add(target);
                }
            }
        }

        return result;
    }
    public List<CodeEntity> findEntitiesRelatedTo(
            String targetId,
            String relationshipType) {

        List<CodeEntity> result =
                new ArrayList<>();

        for (Relationship relationship :
                relationships) {

            if (targetId.equals(
                    relationship.targetId)
                    && relationshipType.equals(
                    relationship.type)) {

                CodeEntity source =
                        findEntityById(
                                relationship.sourceId
                        );

                if (source != null
                        && !containsEntity(
                        result,
                        source.id)) {

                    result.add(source);
                }
            }
        }

        return result;
    }
    public List<CodeEntity> findReachableEntities(
            String sourceId,
            String relationshipType) {

        List<CodeEntity> result =
                new ArrayList<>();

        List<String> queue =
                new ArrayList<>();

        Set<String> visited =
                new HashSet<>();

        queue.add(sourceId);
        visited.add(sourceId);

        int index = 0;

        while (index < queue.size()) {

            String currentId =
                    queue.get(index);

            index++;

            for (Relationship relationship :
                    relationships) {

                if (!currentId.equals(
                        relationship.sourceId)) {

                    continue;
                }

                if (!relationshipType.equals(
                        relationship.type)) {

                    continue;
                }

                String targetId =
                        relationship.targetId;

                if (targetId == null) {
                    continue;
                }

                if (visited.contains(targetId)) {
                    continue;
                }

                visited.add(targetId);

                CodeEntity target =
                        findEntityById(
                                targetId
                        );

                if (target != null) {

                    result.add(target);
                }

                queue.add(targetId);
            }
        }

        return result;
    }
    public GraphPath findPath(
            String sourceId,
            String targetId,
            String relationshipType) {

        List<String> queue =
                new ArrayList<>();

        Set<String> visited =
                new HashSet<>();

        java.util.Map<String, String> previousEntity =
                new java.util.HashMap<>();

        java.util.Map<String, Relationship> previousRelationship =
                new java.util.HashMap<>();

        queue.add(sourceId);
        visited.add(sourceId);

        int index = 0;

        while (index < queue.size()) {

            String currentId =
                    queue.get(index);

            index++;

            if (currentId.equals(targetId)) {
                break;
            }

            for (Relationship relationship :
                    relationships) {

                if (!currentId.equals(
                        relationship.sourceId)) {

                    continue;
                }

                if (!relationshipType.equals(
                        relationship.type)) {

                    continue;
                }

                String nextId =
                        relationship.targetId;

                if (nextId == null
                        || visited.contains(nextId)) {

                    continue;
                }

                visited.add(nextId);

                previousEntity.put(
                        nextId,
                        currentId
                );

                previousRelationship.put(
                        nextId,
                        relationship
                );

                queue.add(nextId);
            }
        }

        // ---------------------------------------------------------
        // Target was not reached
        // ---------------------------------------------------------

        if (!visited.contains(targetId)) {

            return null;
        }

        // ---------------------------------------------------------
        // Reconstruct path
        // ---------------------------------------------------------

        List<CodeEntity> entities =
                new ArrayList<>();

        List<Relationship> pathRelationships =
                new ArrayList<>();

        String currentId =
                targetId;

        while (currentId != null) {

            CodeEntity entity =
                    findEntityById(
                            currentId
                    );

            if (entity != null) {

                entities.add(
                        0,
                        entity
                );
            }

            Relationship relationship =
                    previousRelationship.get(
                            currentId
                    );

            if (relationship != null) {

                pathRelationships.add(
                        0,
                        relationship
                );
            }

            currentId =
                    previousEntity.get(
                            currentId
                    );
        }

        return new GraphPath(
                entities,
                pathRelationships
        );
    }
}