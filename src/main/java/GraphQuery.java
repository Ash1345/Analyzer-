import java.util.ArrayList;
import java.util.List;

public class GraphQuery {

    private final CodeGraph graph;

    public GraphQuery(CodeGraph graph) {
        this.graph = graph;
    }

    public List<CodeEntity> findCallers(String entityName) {

        List<CodeEntity> result =
                new ArrayList<>();

        CodeEntity target = findEntity(entityName);

        if (target == null) {
            return result;
        }

        for (Relationship relationship :
                graph.findRelationshipsTo(target.id)) {

            if (!"CALLS".equals(relationship.type)
                    && !"OBJECT_CALLS".equals(relationship.type)) {
                continue;
            }

            CodeEntity source =
                    graph.findEntityById(
                            relationship.sourceId
                    );

            if (source != null
                    && !containsEntity(result, source.id)) {

                result.add(source);
            }
        }

        return result;
    }

    public List<CodeEntity> findCallees(String entityName) {

        List<CodeEntity> result =
                new ArrayList<>();

        CodeEntity source = findEntity(entityName);

        if (source == null) {
            return result;
        }

        for (Relationship relationship :
                graph.findRelationshipsFrom(source.id)) {

            if (!"CALLS".equals(relationship.type)) {
                continue;
            }

            CodeEntity target =
                    graph.findEntityById(
                            relationship.targetId
                    );

            if (target != null
                    && !containsEntity(result, target.id)) {

                result.add(target);
            }
        }

        return result;
    }

    public List<CodeEntity> findReaders(String entityName) {

        return findEntitiesFromRelationship(
                entityName,
                "READS"
        );
    }

    public List<CodeEntity> findWriters(String entityName) {

        return findEntitiesFromRelationship(
                entityName,
                "WRITES"
        );
    }

    public List<CodeEntity> findProducers(String entityName) {

        List<CodeEntity> result =
                new ArrayList<>();

        CodeEntity target =
                findEntity(entityName);

        if (target == null) {
            return result;
        }

        for (Relationship relationship :
                graph.findRelationshipsTo(target.id)) {

            if (!"PRODUCES".equals(relationship.type)) {
                continue;
            }

            CodeEntity source =
                    graph.findEntityById(
                            relationship.sourceId
                    );

            if (source != null
                    && !containsEntity(result, source.id)) {

                result.add(source);
            }
        }

        return result;
    }

    private List<CodeEntity> findEntitiesFromRelationship(
            String entityName,
            String relationshipType) {

        List<CodeEntity> result =
                new ArrayList<>();

        CodeEntity source =
                findEntity(entityName);

        if (source == null) {
            return result;
        }

        for (Relationship relationship :
                graph.findRelationshipsFrom(source.id)) {

            if (!relationshipType.equals(
                    relationship.type)) {
                continue;
            }

            CodeEntity target =
                    graph.findEntityById(
                            relationship.targetId
                    );

            if (target != null
                    && !containsEntity(result, target.id)) {

                result.add(target);
            }
        }

        return result;
    }

    private CodeEntity findEntity(
            String entityName) {

        for (CodeEntity entity :
                graph.entities) {

            if (entityName.equals(
                    entity.qualifiedName)) {

                return entity;
            }
        }

        return null;
    }

    private boolean containsEntity(
            List<CodeEntity> entities,
            String id) {

        for (CodeEntity entity : entities) {

            if (id.equals(entity.id)) {
                return true;
            }
        }

        return false;
    }
}