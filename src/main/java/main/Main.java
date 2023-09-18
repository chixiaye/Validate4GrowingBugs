package main;

import lombok.extern.slf4j.Slf4j;
import manager.DuplicateBugManager;

/**
 * @author chixiaye
 */
@Slf4j
public class Main {
    public static void main(String[] args){
        DuplicateBugManager.findDuplicateBug();
    }

}
