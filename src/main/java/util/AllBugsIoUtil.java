package util;

import lombok.extern.slf4j.Slf4j;
import model.BugRecordDO;
import org.apache.commons.compress.utils.Lists;

import java.io.*;
import java.util.List;

/**
 * all bugs的统一文件读写，转换处理
 *
 * @author chixiaye
 */
@Slf4j
public class AllBugsIoUtil {
    static final String ALL_BUGS_PATH = "data/all_bugs.csv";

    /**
     * 表头修改的时候，这个也要改
     */
    public static List<BugRecordDO> readAllBugs( boolean ignoreFlag) {
        List<BugRecordDO> bugRecordDOS = Lists.newArrayList();
        // 读取 csv 文件
        try (BufferedReader reader = new BufferedReader(new FileReader(ALL_BUGS_PATH))) {
            String line = reader.readLine();
            line = reader.readLine();
            while (line != null) {
                String[] fields = line.split(",");
                if(ignoreFlag && fields.length>10 && fields[10].equals("1")){
                    line = reader.readLine();
                    continue;
                }
                // 依赖表头
                BugRecordDO bugRecordDO =  BugRecordDO.builder().id(Integer.valueOf(fields[0]))
                        .projectId(fields[1]).projectName(fields[2]).subProjectLocator(fields[3])
                        .bugId(Integer.valueOf(fields[4])).buggyVersion(fields[5]).fixedVersion(fields[6])
                        .reportId(fields[7]).reportUrl(fields[8]).defects4jBugFlag(Integer.valueOf(fields[9]))
                        .ext(fields.length>10? Integer.valueOf(fields[10]):0)
                        .build();

                bugRecordDOS.add(bugRecordDO);
                // 处理每一行的数据
                line = reader.readLine();
            }
        } catch (IOException e) {
            log.warn("Error reading file: " + e.getMessage());
        }
        return bugRecordDOS;
    }

    public static List<BugRecordDO> readAllBugs() {
        return readAllBugs(false);
    }

    /**
     * 回写数据
     */
    public static void rewriteAllBugs( List<BugRecordDO> bugRecordDOS) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter((ALL_BUGS_PATH)))) {
            String header = "id,projectId,projectName,subProjectLocator,bugId,buggyVersion,fixedVersion," +
                    "reportId,reportUrl,defects4jBugFlag,needDeleted";
            writer.write(header);
            writer.newLine();
            for(BugRecordDO bugRecordDO : bugRecordDOS){
                String str = bugRecordDO.getId() + "," + bugRecordDO.getProjectId() + "," + bugRecordDO.getProjectName()
                        + "," + bugRecordDO.getSubProjectLocator() + "," + bugRecordDO.getBugId()
                        + "," + bugRecordDO.getBuggyVersion() + "," + bugRecordDO.getFixedVersion()
                        + "," + bugRecordDO.getReportId() + "," + bugRecordDO.getReportUrl()
                        + "," + bugRecordDO.getDefects4jBugFlag()+","+ bugRecordDO.getExt();
                writer.write(str);
                writer.newLine();
            }
        }catch (IOException e) {
            log.warn("Error writing file: " + e.getMessage());
        }
        return;
    }
}
