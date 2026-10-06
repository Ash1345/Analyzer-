import java.util.Map;

public class BaseSpecifier {

    public String access;

    public Map<String, Object> type;

    public String writtenAccess;

    public String getQualifiedType() {

        if (type == null) {
            return null;
        }

        Object qualType =
                type.get("qualType");

        if (qualType == null) {
            return null;
        }

        return qualType.toString();
    }
}