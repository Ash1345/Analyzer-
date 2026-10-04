import java.util.List;

public interface GraphAnalyzer {

    List<Relationship> analyze(
            AstNode root,
            EntityRegistry entityRegistry,
            SourceLocationResolver locationResolver
    );
}
