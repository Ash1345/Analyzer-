import java.util.List;

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
                new java.util.ArrayList<>();

        for (Relationship relationship :
                relationships) {

            if (sourceId.equals(relationship.sourceId)) {
                result.add(relationship);
            }
        }

        return result;
    }

    public CodeEntity findEntityById(String id) {

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
                new java.util.ArrayList<>();

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
                new java.util.ArrayList<>();

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
                new java.util.ArrayList<>();

        for (CodeEntity entity : entities) {

            if (name.equals(entity.name)) {
                result.add(entity);
            }
        }

        return result;
    }
}