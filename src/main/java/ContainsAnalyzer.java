import java.util.ArrayList;
import java.util.List;

public class ContainsAnalyzer {

    public static List<Relationship> analyze(
            List<CodeEntity> entities) {

        List<Relationship> relationships =
                new ArrayList<>();

        for (CodeEntity entity : entities) {

            if (!"METHOD".equals(entity.kind)) {
                continue;
            }

            if (entity.parentId == null) {
                continue;
            }

            CodeEntity parentEntity = null;

            for (CodeEntity candidate : entities) {

                /*
                 * parentId now contains the Analyzer++
                 * entity ID of the parent.
                 */
                if (candidate.id.equals(
                        entity.parentId)) {

                    parentEntity = candidate;
                    break;
                }
            }

            if (parentEntity != null) {

                relationships.add(
                        new Relationship(
                                parentEntity.id,
                                parentEntity.qualifiedName,
                                entity.id,
                                entity.qualifiedName,
                                "CONTAINS"
                        )
                );
            }
        }

        return relationships;
    }
}