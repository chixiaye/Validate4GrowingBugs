package manager;

import lombok.extern.slf4j.Slf4j;
import model.BugRecordDO;
import model.Config;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.compress.utils.Sets;
import util.AllBugsIoUtil;

import java.io.File;
import java.util.*;

/**
 * 删除冗余文件/文件夹/文件内容
 *
 * @author chixiaye
 */
@Slf4j
public class ReposManager {

    public static final String WHITE_LIST_WORD = "alternative_";
    private static String CORE_PATH = Config.getDefects4jPath() + "/framework/core/Project/";
    private static String PROJECTS_PATH = Config.getDefects4jPath() + "/framework/projects/";

    private static Set<String> projectSet = Sets.newHashSet();
    ;

    public static void execute() {
        List<BugRecordDO> bugRecordDOS = AllBugsIoUtil.readAllBugs();
        bugRecordDOS.stream().distinct().forEach(bugRecordDO -> projectSet.add(bugRecordDO.getProjectId()));
        deleteWithProjectView();
        deleteWithBugView(bugRecordDOS);
    }

    /**
     * bug维度的删除，每个读入文件检测是存在
     *
     * @param bugRecordDOS
     */
    private static void deleteWithBugView(List<BugRecordDO> bugRecordDOS) {
        if (bugRecordDOS == null) {
            return;
        }
        Set<String> commitSet = Sets.newHashSet();
        Map<String, List<Integer>> projectBugMap = new HashMap<>(8);
        bugRecordDOS.forEach(bugRecordDO -> {
            commitSet.add(bugRecordDO.getBuggyVersion());
            commitSet.add(bugRecordDO.getFixedVersion());
            if (projectBugMap.containsKey(bugRecordDO.getProjectId())) {
                projectBugMap.get(bugRecordDO.getProjectId()).add(bugRecordDO.getBugId());
            } else {
                List<Integer> bugIds = Lists.newArrayList();
                bugIds.add(bugRecordDO.getBugId());
                projectBugMap.put(bugRecordDO.getProjectId(), bugIds);
            }
        });
        // 统一记录到一个list里面，统一删除
        List<File> needDeletedFiles = Lists.newArrayList();
        for (String p : projectSet) {
            String prefix = PROJECTS_PATH + "/" + p + "/";
            handleBuildFiles(commitSet, prefix, needDeletedFiles);
            handleFailingTests(commitSet, prefix, needDeletedFiles);
            List<Integer> bugList = projectBugMap.get(p);
            handleLoadedClasses(bugList, prefix, needDeletedFiles);
            handleModifiedClasses(bugList, prefix, needDeletedFiles);
            handlePatches(bugList, prefix, needDeletedFiles);
            handleRelevantTests(bugList, prefix, needDeletedFiles);
            handleTriggerTests(bugList, prefix, needDeletedFiles);
        }
        needDeletedFiles.forEach(file -> {
            log.warn("need to delete file: {}", file.getAbsolutePath());
        });
    }

    private static void handleTriggerTests(List<Integer> bugList, String prefix, List<File> needDeletedFiles) {
        List<File> files = getAllFilesFromDict(prefix + "trigger_tests", "");
        markFileAsNeedDeletedWByBug(bugList, needDeletedFiles, files);
    }

    private static void handleRelevantTests(List<Integer> bugList, String prefix, List<File> needDeletedFiles) {
        List<File> files = getAllFilesFromDict(prefix + "relevant_tests", "");
        markFileAsNeedDeletedWByBug(bugList, needDeletedFiles, files);
    }

    private static void handlePatches(List<Integer> bugList, String prefix, List<File> needDeletedFiles) {
        List<File> files = getAllFilesFromDict(prefix + "patches", ".src.patch");
        files.addAll(getAllFilesFromDict(prefix + "patches", ".src.patch"));
        markFileAsNeedDeletedWByBug(bugList, needDeletedFiles, files);
    }

    private static void handleLoadedClasses(List<Integer> bugList, String prefix, List<File> needDeletedFiles) {
        List<File> files = getAllFilesFromDict(prefix + "loaded_classes", ".src");
        files.addAll(getAllFilesFromDict(prefix + "loaded_classes", ".test"));
        markFileAsNeedDeletedWByBug(bugList, needDeletedFiles, files);
    }

    private static void handleModifiedClasses(List<Integer> bugList, String prefix, List<File> needDeletedFiles) {
        List<File> files = getAllFilesFromDict(prefix + "modified_classes", ".src");
        markFileAsNeedDeletedWByBug(bugList, needDeletedFiles, files);
    }

    private static void markFileAsNeedDeletedWByBug(List<Integer> bugList, List<File> needDeletedFiles,
                                                    List<File> files) {
        for (File file : files) {
            String fileName = file.getName();
            String bugId = fileName;
            // 特殊逻辑 白名单
            if (fileName.contains(WHITE_LIST_WORD)) {
                continue;
            }
            if (fileName.contains(".")) {
                String[] split = fileName.split("\\.");
                bugId = split[0];
            }
            if (!bugList.contains(Integer.valueOf(bugId))) {
                needDeletedFiles.add(file);
            }
        }
    }

    private static void handleBuildFiles(Set<String> commitSet, String prefix, List<File> needDeletedFiles) {
        List<File> folders = getAllFoldersFromDict(prefix + "build_files");
        for (File folder : folders) {
            String commit = folder.getName();
            if (!commitSet.contains(commit)) {
                needDeletedFiles.add(folder);
            } else {
                getAllFilesFromDict(folder.getAbsolutePath(), ".bak").forEach(file -> needDeletedFiles.add(file));
            }
        }
    }

    private static void handleFailingTests(Set<String> commitSet, String prefix, List<File> needDeletedFiles) {
        List<File> folders = getAllFilesFromDict(prefix + "failing_tests", "");
        for (File file : folders) {
            String commit = file.getName();
            if (!commitSet.contains(commit)) {
                needDeletedFiles.add(file);
            }
        }
    }

    /**
     * 项目维度删除
     */
    private static void deleteWithProjectView() {
        // framework/core/Project/ 底下的删除检测pm文本
        List<File> allPmFilesFromDict = getAllPmFilesFromDict(CORE_PATH);
        Iterator<File> iterator = allPmFilesFromDict.iterator();
        while (iterator.hasNext()) {
            File file = iterator.next();
            String name = file.getName().replace(".pm", "");
            if (!projectSet.contains(name)) {
                log.info("delete file: {}", name);
                iterator.remove();
            }
        }
        // framework/projects/ 底下的删除检测
        List<File> allFoldersFromDict = getAllFoldersFromDict(PROJECTS_PATH);
        Iterator<File> it = allFoldersFromDict.iterator();
        while (it.hasNext()) {
            File file = it.next();
            String name = file.getName();
            if (!projectSet.contains(name)) {
                log.info("delete folder: {}", name);
                it.remove();
            }
        }
    }

    /**
     * 获得目录下的所有pm文件 非递归
     *
     * @param dict
     * @return
     */
    private static List<File> getAllPmFilesFromDict(String dict) {
        List<File> fileList = Lists.newArrayList();
        File file = new File(dict);
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isFile() && f.getName().endsWith(".pm")) {
                fileList.add(f);
            }
        }
        return fileList;
    }

    /**
     * 获得目录下的所有文件夹 非递归
     *
     * @param dict
     * @return
     */
    private static List<File> getAllFoldersFromDict(String dict) {
        List<File> fileList = Lists.newArrayList();
        File file = new File(dict);
        File[] files = file.listFiles();
        if (files == null) {
            return fileList;
        }
        for (File f : files) {
            if (f.isDirectory() && !"lib".equals(f.getName())) {
                fileList.add(f);
            }
        }
        return fileList;
    }

    /**
     * 获得目录下的所有文件  非递归
     *
     * @param dict
     * @return
     */
    private static List<File> getAllFilesFromDict(String dict, String ext) {
        List<File> fileList = Lists.newArrayList();
        File file = new File(dict);
        File[] files = file.listFiles();
        if (files == null) {
            return fileList;
        }
        for (File f : files) {
            if (f.isFile() && (ext == null || ext.length() == 0 || f.getName().endsWith(ext))) {
                fileList.add(f);
            }
        }
        return fileList;
    }

}
