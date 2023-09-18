package util;

import lombok.extern.slf4j.Slf4j;
import model.BugRecordDO;
import org.apache.commons.compress.utils.Lists;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
    public static List<BugRecordDO> readAllBugs() {
        List<BugRecordDO> bugRecordDOS = Lists.newArrayList();
        // 读取 csv 文件
        try (BufferedReader reader = new BufferedReader(new FileReader(ALL_BUGS_PATH))) {
            String line = reader.readLine();
            line = reader.readLine();
            while (line != null) {
//                log.info("line: {}", line);
                String[] fields = line.split(",");
                // 依赖表头
                BugRecordDO bugRecordDO =  BugRecordDO.builder().id(Integer.valueOf(fields[0]))
                        .projectId(fields[1]).projectName(fields[2]).subProjectLocator(fields[3])
                        .bugId(Integer.valueOf(fields[4])).buggyVersion(fields[5]).fixedVersion(fields[6])
                        .reportId(fields[7]).reportUrl(fields[8]).defects4jBugFlag(Integer.valueOf(fields[9]))
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
}
