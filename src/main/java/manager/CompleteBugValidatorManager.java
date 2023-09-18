package manager;

import lombok.extern.slf4j.Slf4j;
import model.BugRecordDO;
import model.Config;
import util.AllBugsIoUtil;

import java.io.File;
import java.util.List;

/**
 * bug信息完整性检查
 *
 * @author chixiaye
 */
@Slf4j
public class CompleteBugValidatorManager {
    public static final String DEFECTS_4J_PATH = Config.getDefects4jPath();

    public static void ValidateCompleteBug() {
        List<BugRecordDO> bugRecordDOS = AllBugsIoUtil.readAllBugs();
        for (int i = 0; i < bugRecordDOS.size(); i++) {
            BugRecordDO bugRecordDO = bugRecordDOS.get(i);
            String projectPath = DEFECTS_4J_PATH + "framework/projects/" + bugRecordDO.getProjectId();
            isExistFile(bugRecordDO, projectPath + "/patches/", ".src.patch");
//            isExistFile(projectPath, "/patches/" + bugRecordDO.getBugId() + ".test.patch");
            isExistFile(bugRecordDO, projectPath + "/trigger_tests/", "");
            isExistFile(bugRecordDO, projectPath + "/loaded_classes/", ".src");
            isExistFile(bugRecordDO, projectPath + "/loaded_classes/", ".test");
            isExistFile(bugRecordDO, projectPath + "/modified_classes/", ".src");
            isExistFile(bugRecordDO, projectPath + "/relevant_tests/", "");
        }
    }

    private static boolean isExistFile(BugRecordDO bugRecordDO, String prefix, String extension) {
        String pathname = prefix + bugRecordDO.getBugId() + extension;
        File file = new File(pathname);
        boolean res = file.exists() && file.length() != 0;
        if (!res) {
            log.warn("file not exist or empty: pathname={} bugRecordDO={}", pathname, bugRecordDO );
        }
        return res;
    }
}
