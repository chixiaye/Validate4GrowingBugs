package model;

import lombok.Data;

/**
 * @author chixiaye
 */
@Data
public class BugRecordDO {
    Integer id;
    String projectId;
    String projectName;
    String subProjectLocator;
    Integer bugId;
    String buggyVersion;
    String fixedVersion;
    String reportId;
    String reportUrl;
    /**
     * 1:是 0:否
     */
    Integer defects4jBugFlag;
}
