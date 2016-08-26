package bixie.util;

import java.util.List;

public class JSONbug {

    private String bug_class;
    private String kind;
    private String qualifier;
    private String severity;
    private int line;
    private String procedure;
    private String procedure_id;
    private String file;
    List<JSONTraceItem> bug_trace;
    int key;
    List<TagValueRecord> qualifier_tags;
    int hash;
    //The following are options in the original spec
    //Should probably be changed to analysis_source_loc
    String dotty;
    Loc infer_source_loc;

    public JSONbug() {
    }

    public String getBug_class() {
        return bug_class;
    }

    public void setBug_class(String bug_class) {
        this.bug_class = bug_class;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    public String getProcedure_id() {
        return procedure_id;
    }

    public void setProcedure_id(String procedure_id) {
        this.procedure_id = procedure_id;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public List<JSONTraceItem> getBug_trace() {
        return bug_trace;
    }

    public void setBug_trace(List<JSONTraceItem> bug_trace) {
        this.bug_trace = bug_trace;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public List<TagValueRecord> getQualifier_tags() {
        return qualifier_tags;
    }

    public void setQualifier_tags(List<TagValueRecord> qualifier_tags) {
        this.qualifier_tags = qualifier_tags;
    }

    public int getHash() {
        return hash;
    }

    public void setHash(int hash) {
        this.hash = hash;
    }

    public String getDotty() {
        return dotty;
    }

    public void setDotty(String dotty) {
        this.dotty = dotty;
    }

    public Loc getInfer_source_loc() {
        return infer_source_loc;
    }

    public void setInfer_source_loc(Loc infer_source_loc) {
        this.infer_source_loc = infer_source_loc;
    }

    @Override
    public String toString() {
        return "JSONbug [bug_class=" + bug_class + ", kind=" + kind + ", qualifier=" + qualifier + ", severity="
                + severity + ", line=" + line + ", procedure=" + procedure + ", procedure_id=" + procedure_id
                + ", file=" + file + ", bug_trace=" + bug_trace + ", key=" + key + ", qualifier_tags=" + qualifier_tags
                + ", hash=" + hash + ", dotty=" + dotty + ", infer_source_loc=" + infer_source_loc + "]";
    }
}
