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
                            + (entity.returnType != null
                            ? " -> returns " + entity.returnType
                            : "")
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
}