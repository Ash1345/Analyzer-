public class CallRelation {

    public String caller;
    public String callee;
    public String type;

    public CallRelation(
            String caller,
            String callee,
            String type) {

        this.caller = caller;
        this.callee = callee;
        this.type = type;
    }
}