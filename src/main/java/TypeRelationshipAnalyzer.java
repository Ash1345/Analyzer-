import java.util.ArrayList;
import java.util.List;

public class TypeRelationshipAnalyzer {

    public static List<Relationship> analyze(
            List<CodeEntity> entities) {

        List<Relationship> relationships =
                new ArrayList<>();

        for (CodeEntity variable : entities) {

            if (!"VARIABLE".equals(variable.kind)) {
                continue;
            }

            if (variable.type == null) {
                continue;
            }

            String variableType =
                    variable.type
                            .replace("*", "")
                            .replace("&", "")
                            .trim();

            for (CodeEntity target : entities) {

                if (!"CLASS".equals(target.kind)) {
                    continue;
                }

                if (target.name.equals(variableType)) {

                    Relationship relationship =
                            new Relationship(
                                    variable.id,
                                    variable.qualifiedName,
                                    target.id,
                                    target.qualifiedName,
                                    "TYPE_OF"
                            );

                    relationships.add(
                            relationship
                    );

                    break;
                }
            }
        }

        return relationships;
    }
}