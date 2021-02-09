/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static dev.necauqua.mods.cm.ChiseledMe.MODID;

public final class Log {

    private Log() {}

    private static final Logger logger = LogManager.getLogger(MODID);

    public static void trace(Object msg) {
        logger.trace(msg);
    }

    public static void debug(Object msg) {
        logger.debug(msg);
    }

    public static void info(Object msg) {
        logger.info(msg);
    }

    public static void warn(Object msg) {
        logger.warn(msg);
    }

    public static void warn(Object msg, Throwable cause) {
        logger.warn(msg, cause);
    }

    public static void error(Object msg) {
        logger.error(msg, null);
    }

    public static void error(Object msg, Throwable cause) {
        logger.error(msg, cause);
    }

    public static void fatal(Object msg, Throwable cause) {
        logger.fatal(msg, cause);
    }
}
