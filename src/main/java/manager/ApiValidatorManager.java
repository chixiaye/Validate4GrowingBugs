package manager;


import lombok.extern.slf4j.Slf4j;
import model.BugRecordDO;
import util.AllBugsIoUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.io.Closeable;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author chixiaye
 */
@Slf4j
public class ApiValidatorManager {

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors()/2,
            Runtime.getRuntime().availableProcessors()/2 + 1, 30, java.util.concurrent.TimeUnit.SECONDS,
            new java.util.concurrent.LinkedBlockingQueue<>(), new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

    public static void validate() {
        List<BugRecordDO> bugRecordDOS = AllBugsIoUtil.readAllBugs();
        for (int i = 153;  i < bugRecordDOS.size(); i++) {
            BugRecordDO bugRecordDO = bugRecordDOS.get(i);
            threadPoolExecutor.execute(() -> validateCheckout(bugRecordDO));
//            validateCheckout(bugRecordDO);
//            break;
        }
    }

    private static void validateTest(BugRecordDO bugRecordDO, String type) {
        String checkoutPath = "/tmp/" + bugRecordDO.getProjectId() + "_" + bugRecordDO.getBugId() + "_" + type;
        String subProjectLocator = bugRecordDO.getSubProjectLocator();
        String cmd = "defects4j test";
        String result = runCmdAndGetResult(cmd, new File(checkoutPath + "/" + subProjectLocator));
        // 验证每一行的结果是否为OK结尾
        String[] lines = result.split("\n");
        if (lines.length < 3) {
            log.error("defects4j failed in test: type={}\nbugRecordDO={}\nresult={}", type, bugRecordDO, result);
            return;
        }
        for (int i = lines.length-1 ;i >=lines.length - 2; i--) {
            if (!lines[i].endsWith(".. OK")) {
                log.error("defects4j failed in test: type={}\nbugRecordDO={}\nresult={}", type, bugRecordDO, result);
                break;
            }
        }
    }

    private static void validateCompile(BugRecordDO bugRecordDO, String type) {
        String checkoutPath = "/tmp/" + bugRecordDO.getProjectId() + "_" + bugRecordDO.getBugId() + "_" + type;
        String subProjectLocator = bugRecordDO.getSubProjectLocator();
        String cmd = "defects4j compile";
        String result = runCmdAndGetResult(cmd, new File(checkoutPath + "/" + subProjectLocator));
        // 验证每一行的结果是否为OK结尾
        String[] lines = result.split("\n");
        for (String line : lines) {
            if (!line.endsWith(".. OK")) {
                log.error("defects4j failed in compile: type={}\nbugRecordDO={}\nresult={}", type, bugRecordDO, result);
                break;
            }
        }
    }


    /**
     * 验证  defects4j checkout  命令 是否能正常执行 命令格式如下
     * defects4j checkout -p Shiro_core -v 37b -w /tmp/Shiro_core_37_buggy -s core
     */
    private static void validateCheckout(BugRecordDO bugRecordDO) {
        final String[] types = {"b", "f"};
        log.info("validate id={},projectId={},bugid={}", bugRecordDO.getId(), bugRecordDO.getProjectId(),
                bugRecordDO.getBugId());
        for (String type : types) {
            String checkoutPath = "/tmp/" + bugRecordDO.getProjectId() + "_" + bugRecordDO.getBugId() + "_" + type;
            String subProjectLocator = bugRecordDO.getSubProjectLocator();
            if (subProjectLocator == null || "".equals(subProjectLocator)) {
                subProjectLocator = ".";
            }
            String cmdStr =
                    "defects4j checkout -p " + bugRecordDO.getProjectId() + " -v " + bugRecordDO.getBugId() + type
                    + " -w " + checkoutPath + " -s "
                    + subProjectLocator;
            String result = runCmdAndGetResult(cmdStr, null);
            // 验证每一行的结果是否为OK结尾
            boolean endWithOk=true;
            String[] lines = result.split("\n");
            for (String line : lines) {
                if (!line.endsWith(" OK")) {
                    log.error("defects4j failed in checkout: cmd={}\nbugRecordDO={}\nresult={}", cmdStr, bugRecordDO,
                            result);
                    endWithOk =false;
                    break;
                }
            }
            // validate compile and test
            if(endWithOk){
                validateCompile(bugRecordDO, type);
                validateTest(bugRecordDO, type);
            }
            // remove checkout folder
            runCmdAndGetResult("rm -rf " + checkoutPath, null);
            if(!endWithOk){
                return;
            }
        }

    }

    /**
     * 执行系统命令, 返回执行结果
     *
     * @param cmd 需要执行的命令
     * @param dir 执行命令的子进程的工作目录, null 表示和当前主进程工作目录相同
     */
    private static String runCmdAndGetResult(String cmd, File dir) {
        StringBuilder result = new StringBuilder();
        Process process = null;
        BufferedReader bufrIn = null;
        BufferedReader bufrError = null;

        try {
            // 执行命令, 返回一个子进程对象（命令在子进程中执行）
            process = Runtime.getRuntime().exec(cmd, null, dir);

            // 方法阻塞, 等待命令执行完成（成功会返回0）
            process.waitFor();

            // 获取命令执行结果, 有两个结果: 正常的输出 和 错误的输出（PS: 子进程的输出就是主进程的输入）
            bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));

            // 读取输出
            String line = null;
            while ((line = bufrIn.readLine()) != null) {
                result.append(line).append('\n');
            }
            while ((line = bufrError.readLine()) != null) {
                result.append(line).append('\n');
            }

            // 返回执行结果
            return result.toString();
        } catch (Exception e) {
            log.error("runCmdAndGetResult error e=", e);
            return null;
        } finally {
            closeStream(bufrIn);
            closeStream(bufrError);

            // 销毁子进程
            if (process != null) {
                process.destroy();
            }

            // 返回执行结果
            return result.toString();
        }

    }

    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                // nothing
            }
        }
    }
}
