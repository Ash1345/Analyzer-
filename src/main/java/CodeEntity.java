import java.util.ArrayList;
import java.util.List;

public class CodeEntity {

    // Analyzer++ identity
    public String id;

    // Raw Clang AST node identity
    public String astId;

    // Stable logical compiler identity when available
    // Example: mangled method name
    public String logicalId;

    public String kind;
    public String name;
    public String qualifiedName;

    public String parentId;

    public String returnType;
    public String type;

    public String file;
    public Integer line;
    public Integer column;

    public List<Parameter> parameters;

    public CodeEntity(
            String astId,
            String kind,
            String name,
            String qualifiedName) {

        this.astId = astId;

        // For now, preserve old behavior.
        // Later we will replace this with a stable
        // Analyzer++ entity ID.
        this.id = astId;

        this.kind = kind;
        this.name = name;
        this.qualifiedName = qualifiedName;

        this.parameters = new ArrayList<>();
    }
}