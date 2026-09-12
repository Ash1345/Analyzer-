import java.util.ArrayList;
import java.util.List;

public class CodeEntity {

    public String id;
    public String kind;
    public String name;
    public String qualifiedName;
    public String parentId;
    public String returnType;

    public List<Parameter> parameters;

    public CodeEntity(
            String id,
            String kind,
            String name,
            String qualifiedName) {

        this.id = id;
        this.kind = kind;
        this.name = name;
        this.qualifiedName = qualifiedName;

        this.parameters = new ArrayList<>();
    }
}