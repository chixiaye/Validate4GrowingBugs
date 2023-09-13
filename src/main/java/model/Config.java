package model;

import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Properties;

/**
 * @author chixiaye
 */
@Slf4j
public class Config {
    @Getter
    private static String defects4jPath;

    private static final String confPath = "src/main/resources/config.properties";

    static {
        try {
            readConf();
        } catch (IOException e) {
            log.error("read config.properties error", e);
        }
    }

    private static void readConf() throws IOException {
        InputStream inputStream = new BufferedInputStream(new FileInputStream(confPath));
        Properties properties = new Properties();
        properties.load(inputStream);
        defects4jPath = properties.getProperty("defects4jPath");
    }
}
