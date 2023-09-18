package manager;

import lombok.extern.slf4j.Slf4j;
import model.BugRecordDO;
import model.Config;
import org.apache.commons.compress.utils.Lists;
import util.AllBugsIoUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * 重复的bug项管理
 *
 * @author chixiaye
 */
@Slf4j
public class DuplicateBugManager {

    public static final String DEFECTS_4J_PATH = Config.getDefects4jPath();

    public static void findDuplicateBug() {
        List<BugRecordDO> bugRecordDOS = AllBugsIoUtil.readAllBugs();
        for (int i = 0; i < bugRecordDOS.size(); i++) {
//            log.info("i: {}", i);
            for (int j = i + 1; j < bugRecordDOS.size(); j++) {
                BugRecordDO bugRecordDO1 = bugRecordDOS.get(i);
                BugRecordDO bugRecordDO2 = bugRecordDOS.get(j);
                if (isDuplicateBug(bugRecordDO1, bugRecordDO2)) {
                    log.info("project1={}, id1={} # project2={}, id2={} is Duplicate", bugRecordDO1.getProjectName(),
                            bugRecordDO1.getBugId(), bugRecordDO2.getProjectName(), bugRecordDO2.getBugId());
                }
            }
        }
    }

    public static boolean isDuplicateBug(BugRecordDO bugRecordDO1, BugRecordDO bugRecordDO2) {
        String patch1 =
                DEFECTS_4J_PATH + "framework/projects/" + bugRecordDO1.getProjectId() + "/patches/" + bugRecordDO1.getBugId() + ".src.patch";
        String patch2 =
                Config.getDefects4jPath() + "framework/projects/" + bugRecordDO2.getProjectId() + "/patches/" + bugRecordDO2.getBugId() + ".src.patch";
        boolean patchFlag = isDuplicateFile(patch1, patch2);
        if (!patchFlag) {
            return false;
        }
        String trigger1 =
                Config.getDefects4jPath() + "framework/projects/" + bugRecordDO1.getProjectId() + "/trigger_tests/" + bugRecordDO1.getBugId()  ;
        String trigger2 =
                Config.getDefects4jPath() + "framework/projects/" + bugRecordDO2.getProjectId() + "/trigger_tests/" + bugRecordDO2.getBugId()  ;
        return isDuplicateFile(trigger1, trigger2);
    }



    /**
     * 检测patch 是否重复，patch 生成的语句是一致的，所以只检查内容是否重复
     *
     * @param patch1
     * @param patch2
     * @return
     */
    public static boolean isDuplicateFile(String patch1, String patch2) {
        List<String> contentList1 = Lists.newArrayList();
        readContent(patch1, contentList1);
        List<String> contentList2 = Lists.newArrayList();
        readContent(patch2, contentList2);
        if (contentList1.size() != contentList2.size()) {
            return false;
        }
        for (int i = 0; i < contentList1.size(); i++) {
            if (!contentList1.get(i).equals(contentList2.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static void readContent(String patch1, List<String> contentList) {
        // 逐行读取文件
        try (BufferedReader reader = new BufferedReader(new FileReader(patch1))) {
            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith("diff --git") ||
                        line.startsWith("index ") ||
                        line.startsWith("--- a") ||
                        line.startsWith("+++ b")) {
                } else {
                    contentList.add(line);
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            log.warn("Error reading file: e=", e);
        }
    }
}
