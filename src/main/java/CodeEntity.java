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

    // Function/method signature
    // Example:
    // Calculator::add(int,int)
    public String signature;

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

        /*
         * Analyzer++ identity.
         *
         * The identity is derived from the entity kind and
         * qualified name so that relationships can reference
         * entities independently of their raw Clang AST IDs.
         */
        this.id =
                kind
                        + ":"
                        + qualifiedName;

        this.kind = kind;
        this.name = name;
        this.qualifiedName = qualifiedName;

        this.parameters = new ArrayList<>();

    }
    public void updateIdentity() {

        if (("METHOD".equals(kind)
                || "FUNCTION".equals(kind))
                && signature != null) {

            this.id =
                    kind
                            + ":"
                            + signature;

            return;
        }

        this.id =
                kind
                        + ":"
                        + qualifiedName;
    }
}