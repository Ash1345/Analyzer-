import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AstNode {

    public String id;
    public String kind;
    public String name;
    public String opcode;

    public Boolean isImplicit;
    public Boolean isReferenced;

    public String referencedMemberDecl;
    public Map<String, Object> referencedDecl;

    public Map<String, Object> loc;
    public Map<String, Object> range;
    public Map<String, Object> type;

    public List<AstNode> inner;
}