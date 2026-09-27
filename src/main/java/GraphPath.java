import java.util.ArrayList;
import java.util.List;

public class GraphPath {

    public List<CodeEntity> entities =
            new ArrayList<>();

    public List<Relationship> relationships =
            new ArrayList<>();

    public GraphPath() {
    }

    public GraphPath(
            List<CodeEntity> entities,
            List<Relationship> relationships) {

        this.entities.addAll(
                entities
        );

        this.relationships.addAll(
                relationships
        );
    }
}