/**
 *    Retz
 *    Copyright (C) 2016-2017 Nautilus Technologies, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package io.github.retz.misc;

import java.sql.SQLException;

import org.slf4j.Logger;

public class LogUtil {

    public static void error(Logger logger, String message, Throwable throwable) {
        action(logger, message, throwable, (log, m, t) -> log.error(m, t));
    }

    public static void warn(Logger logger, String message, Throwable throwable) {
        action(logger, message, throwable, (log, m, t) -> log.warn(m, t));
    }

    public static void info(Logger logger, String message, Throwable throwable) {
        action(logger, message, throwable, (log, m, t) -> log.info(m, t));
    }

    @FunctionalInterface
    private interface LogConsumer {
        public void log(Logger log, String message, Throwable t);
    }

    private static void action(Logger logger, String message, Throwable t, LogConsumer consumer) {
        consumer.log(logger, message, t);
        for (; t != null; t = t.getCause()) {
            if (t instanceof SQLException) {
                SQLException e = (SQLException) t;
                for (e = e.getNextException(); e != null; e = e.getNextException()) {
                    consumer.log(logger, "next exception", e);
                }
            }
        }
    }
}
