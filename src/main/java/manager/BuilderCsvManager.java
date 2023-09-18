package manager;


import lombok.extern.slf4j.Slf4j;
import model.BugProjectDO;
import model.BugRecordDO;
import model.Config;
import org.apache.commons.compress.utils.Lists;
import util.ProjectIoUtil;

import java.io.IOException;
import java.util.List;

/**
 * @author chixiaye
 */
@Slf4j
public class BuilderCsvManager {

    public static final String FILE_PATH = "./initReadMe.xlsx";
    /**
     * 特殊字符
     */
    public static final String BR = "</br>";
    /**
     * 自增主键
     */
    private static volatile Integer commonId = 1;

    private static String DEFECTS4J_PATH = Config.getDefects4jPath();

    public static void buildBugRecordBO() throws IOException, InstantiationException, IllegalAccessException {
        List<Object> projects = ProjectIoUtil.readExcel(FILE_PATH, BugProjectDO.class);
        List<BugRecordDO> bugRecordDOS = Lists.newArrayList();
        for (Object obj : projects) {
            BugProjectDO bugProjectDO = (BugProjectDO) obj;
            List<BugRecordDO> recordDOS = splitToBugRecordDOS(bugProjectDO);
            bugRecordDOS.addAll(recordDOS);
        }
        log.info("bugRecordDOS size={}", bugRecordDOS.size());
        ProjectIoUtil.convertToCSV(bugRecordDOS, "./test.csv");
    }

    private synchronized static List<BugRecordDO> splitToBugRecordDOS(BugProjectDO bugProjectDO) {
        String projectId = removeWhitespace(bugProjectDO.getProjectId());
        String projectName = removeWhitespace(bugProjectDO.getProjectName());
        Integer numbers = Integer.valueOf(removeWhitespace(bugProjectDO.getNumbers()));
        String locator = removeWhitespace(bugProjectDO.getLocator());
        String bugIdStr = removeWhitespace(bugProjectDO.getBugIds());
        List<Integer> numberList = restoreNumberList(bugIdStr);
//        log.info("projectId={}, projectName={}, numbers={}, locator={}, numberList={}", projectId, projectName,
//                numbers, locator, JSON.toJSONString(numberList));
        if (!numbers.equals(numberList.size())) {
            log.error("projectId={}, the number of bugs is inconsistent in numberList and numbers. numberList={}," +
                    "numbers={}", projectId, numberList.size(), numbers);
        }
        List<BugRecordDO> bugRecordDOS = Lists.newArrayList();

        // 根据项目id拿到active_bugs.csv中

        for (Integer num : numberList) {
            String command = DEFECTS4J_PATH + "framework/projects/" + projectId;
            BugRecordDO builder = BugRecordDO.builder().id(commonId++).projectId(projectId)
                    .projectName(projectName).subProjectLocator(locator).bugId(num).build();
            bugRecordDOS.add(builder);
        }
        return bugRecordDOS;
    }

    public static String removeWhitespace(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("\\s+", "");
    }

    public static List<Integer> restoreNumberList(String input) {
        List<Integer> numberList = Lists.newArrayList();
        if (input == null || input.isEmpty()) {
            return numberList;
        }

        String[] ranges = input.replaceAll(BR, "").split(",");
        for (String range : ranges) {
            if (range.contains("-")) {
                String[] rangeValues = range.split("-");
                int start = Integer.parseInt(rangeValues[0]);
                int end = Integer.parseInt(rangeValues[1]);
                for (int i = start; i <= end; i++) {
                    numberList.add(i);
                }
            } else {
                numberList.add(Integer.parseInt(range));
            }
        }
        return numberList;
    }


}
