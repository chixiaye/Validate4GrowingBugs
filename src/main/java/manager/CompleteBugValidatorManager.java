package manager;

import model.BugRecordDO;
import util.AllBugsIoUtil;

import java.util.List;

/**
 * bug信息完整性检查
 * @author chixiaye
 */
public class CompleteBugValidatorManager {
    public static void findDuplicateBug() {
        List<BugRecordDO> bugRecordDOS = AllBugsIoUtil.readAllBugs();
        for (int i = 0; i < bugRecordDOS.size(); i++) {
            BugRecordDO bugRecordDO1 = bugRecordDOS.get(i);
        }
    }
}
