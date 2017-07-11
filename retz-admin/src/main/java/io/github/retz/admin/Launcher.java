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
package io.github.retz.admin;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterException;
import io.github.retz.cli.FileConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/*
retz-admin help
retz-admin create-user
retz-admin get-user -id <user>
retz-admin enable-user -id <user>
retz-admin disable-user -id <user>
retz-admin usage -id <user> --start-time <date> --end-time <date>
retz-admin list-user
retz-admin snapshot -path <dest-file>
retz-admin restore -path <src-file>
 */
public class Launcher {
    private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);

    static List<SubCommand> SUB_COMMANDS = new LinkedList<>();

    static {
        SubCommand[] subCommands = {
                new CommandCreateUser(),
                new CommandCreateUsers(),
                new CommandDisableUser(),
                new CommandEnableUser(),
                new CommandGC(),
                new CommandGetUser(),
                new CommandHelp(),
                new CommandListUser(),
                new CommandUsage()
        };
        SUB_COMMANDS.addAll(Arrays.asList(subCommands));
    }

    public static void main(String... argv) throws Throwable {
        int status = execute(argv);
        System.exit(status);
    }

    public static int execute(String... argv) throws Throwable {
        int port = -1;
        try {
            Configuration conf = parseConfiguration(argv);
            port = conf.fileConfiguration.getJmxPort();
            JCommander commander = conf.commander;

            if (commander.getParsedCommand() == null) {
                LOG.error("Invalid subcommand");
                help(SUB_COMMANDS);
            } else {
                if (conf.commands.verbose) {
                    LOG.info("Command: {}, Config file: {}", commander.getParsedCommand(),
                            conf.commands.getConfigFile());
                    LOG.info("Using JMX port {}", conf.fileConfiguration.getJmxPort());
                }
                return conf.getParsedSubCommand().handle(conf.fileConfiguration, conf.commands.verbose);
            }
        } catch (JMException e) {
            LOG.error("JMX server is not up at localhost:{}", port);
        } catch (IOException e) {
            LOG.error("Invalid configuration file", e);
        } catch (URISyntaxException e) {
            LOG.error("Bad file format: {}", e.toString());
        } catch (MissingCommandException e) {
            LOG.error(e.toString());
            help(SUB_COMMANDS);
        } catch (ParameterException e) {
            LOG.error("{}", e.toString());
            help(SUB_COMMANDS);
        }
        return -1;
    }

    public static void help() {
        help(SUB_COMMANDS);
    }

    private static void help(List<SubCommand> subCommands) {
        LOG.info("Subcommands:");
        for (SubCommand subCommand : subCommands) {
            LOG.info("\t{}\t{} ({})", subCommand.getName(),
                    subCommand.description(), subCommand.getClass().getName());
        }
    }


    static Configuration parseConfiguration(String... argv) throws IOException, URISyntaxException {
        Configuration conf = new Configuration();

        conf.commands = new MainCommand();
        conf.commander = new JCommander(conf.commands);

        for (SubCommand subCommand : SUB_COMMANDS) {
            subCommand.add(conf.commander);
        }

        conf.commander.parse(argv);

        if (conf.commands.getConfigFile() != null) {
            conf.fileConfiguration = new FileConfiguration(conf.commands.getConfigFile());
        }
        return conf;
    }

    static class Configuration {
        FileConfiguration fileConfiguration;
        JCommander commander;
        MainCommand commands;

        SubCommand getParsedSubCommand() {
            for (SubCommand subCommand : SUB_COMMANDS) {
                if (subCommand.getName().equals(commander.getParsedCommand())) {
                    return subCommand;
                }
            }
            throw new ParameterException("unknown command: " + commander.getParsedCommand());
        }
    }
}
