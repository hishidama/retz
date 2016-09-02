/**
 *    Retz
 *    Copyright (C) 2016 Nautilus Technologies, Inc.
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
package io.github.retz.cli;

import io.github.retz.protocol.Application;
import io.github.retz.protocol.ListAppResponse;
import io.github.retz.web.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;

public class CommandListApp implements SubCommand {
    static final Logger LOG = LoggerFactory.getLogger(CommandListApp.class);

    @Override
    public String description() {
        return "List all loaded applications";
    }

    @Override
    public String getName() {
        return "list-app";
    }

    @Override
    public int handle(FileConfiguration fileConfig) {
        LOG.debug("Configuration: {}", fileConfig.toString());

        try (Client webClient = new Client(fileConfig.getUri().getHost(),
                fileConfig.getUri().getPort())) {

            ListAppResponse r = (ListAppResponse) webClient.listApp();
            for (Application a : r.applicationList()) {
                LOG.info("Application {}: fetch: {} / {} / persistent ({} MB): {}", a.getAppid(),
                        String.join(" ", a.getFiles()),
                        String.join(" ", a.getLargeFiles()),
                        a.getDiskMB(), String.join(" ", a.getPersistentFiles()));
            }
            return 0;

        } catch (ConnectException e) {
            LOG.error("Cannot connect to server {}", fileConfig.getUri());

        } catch (IOException e) {
            LOG.error(e.toString(), e);
        }
        return -1;
    }
}
