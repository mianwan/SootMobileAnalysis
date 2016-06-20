package edu.usc.sql.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by mianwan on 2/27/16.
 */
public class MethodAnalysis {
    private static final Logger logger = LogManager.getLogger(MethodAnalysis.class);

    public static void main(String[] args) {
        logger.trace("Entering application.");
        Bar bar = new Bar();
        if (!bar.doIt()) {
            logger.error("Didn't do it.");
        }
        logger.trace("Exiting application.");
    }
}
