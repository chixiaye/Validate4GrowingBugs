package main;

import lombok.extern.slf4j.Slf4j;
import manager.BuilderCsvManager;

import java.io.IOException;

/**
 * @author chixiaye
 */
@Slf4j
public class Main {
    public static void main(String[] args){
        try {
            BuilderCsvManager.buildBugRecordBO();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

}
