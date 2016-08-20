/*
 * Copyright (c) 2016 Anton Bulakh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package necauqua.mods.cm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Log {

    private Log() {}

    private static Logger logger = LogManager.getLogger("Chiseled Me");

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

    public static void error(Object msg, Throwable cause) {
        logger.error(msg, cause);
    }

    public static void fatal(Object msg, Throwable cause) {
        logger.fatal(msg, cause);
    }
}
