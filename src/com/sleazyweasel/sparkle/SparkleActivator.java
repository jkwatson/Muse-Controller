package com.sleazyweasel.sparkle;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Activates the Sparkle Framework
 */
public class SparkleActivator {
    private static final Logger logger = Logger.getLogger(SparkleActivator.class.getName());
    /**
     * Native method declaration
     */
    public native static void initSparkle(String pathToSparkleFramework,
                                          boolean updateAtStartup,
                                          int checkInterval);

    /**
     * Whether updates are checked at startup
     */
    private boolean updateAtStartup = true;

    /**
     * Check interval period, in seconds
     */
    private int checkInterval = 86400;  // 1 day

    /**
     * Dynamically loads the JNI object. Will 
     * fail if it is launched on an non-MacOSX system
     * or when libinit_sparkle.dylib is outside of the 
     * DYLD_LIBRARY_PATH
     */
    static {
//        System.load("/Users/john/projects/applescriptifier/lib/native/libsparkle_init.dylib");
        try {
            System.loadLibrary("sparkle_init");
        } catch (Throwable e) {
            logger.log(Level.WARNING, "Exception caught.", e);;
        }
    }

    /**
     * Initialize and start Sparkle
     *
     * @throws Exception
     */
    public void start() throws Exception {
        logger.info("System.getProperty(\"user.dir\") = " + System.getProperty("user.dir"));
        initSparkle(System.getProperty("user.dir") + "/../../Frameworks/Sparkle.framework", updateAtStartup, checkInterval);
    }
}
