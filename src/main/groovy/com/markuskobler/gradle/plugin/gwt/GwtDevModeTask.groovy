/*
   Copyright 2010 Markus Kobler

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.markuskobler.gradle.plugin.gwt

import org.apache.tools.ant.taskdefs.Expand
import org.gradle.api.tasks.StopActionException
import org.gradle.api.tasks.TaskAction
import org.gradle.util.AntUtil

/**
 *
 * @author Markus Kobler
 */
class GwtDevModeTask extends AbstractGwtTask {    

    static final String DEVMODE_CLASSNAME = 'com.google.gwt.dev.DevMode'

    boolean noserver;
    int port = 0;
    String whitelist
    String blacklist
    File logDir;
    Class/*<ServletContainerLauncher>*/ server
    int codeServerPort = 0;

    File warDir
    File webApp

    List<String> startupUrls
  
    @TaskAction
    def executeDevMode() {

        if( modules == null || modules.size == 0 ) throw new StopActionException("No modules specified");

        configureAntClasspath(project.ant, classpath)

        Map otherArgs = [
            classpathref: GWT_CLASSPATH_ID,
            classname: DEVMODE_CLASSNAME
        ]

        ant.java(otherArgs + options.optionMap()) {
            
            options.forkOptions.jvmArgs.each { jvmarg(value: it) }
            options.forkOptions.environment.each {String key, value -> env(key: key, value: value) }
            options.systemProperties.each {String key, value -> sysproperty(key: key, value: value) }
            
            if ( noserver ) arg(line: "-noserver")

            if ( port > 0) arg(line: "-port ${port}")
            if ( codeServerPort > 0) arg(line: "-codeServerPort ${codeServerPort}")

            if ( whitelist ) arg(line: "-whitelist ${whitelist}")
            if ( blacklist ) arg(line: "-blacklist ${blacklist}")

            startupUrls.each {
                logger.info("Startup URL {}", it)
                arg(line: "-startupUrl ${it}")
            }

            warDir.mkdirs()
            if( webApp != null && webApp.exists() ) {
              Expand unzip = new Expand();
              unzip.src = webApp;
              unzip.dest = warDir;
              AntUtil.execute(unzip);
            }

            arg(line: "-war ${warDir}")

            if (logDir) {
                logDir.mkdirs()
                arg(line: "-logdir ${logDir}")
            }
            arg(line: "-logLevel ${logLevel}")

            if (genDir) {
                genDir.mkdirs()
                arg(line: "-gen ${genDir}")
            }

            if (workDir) {
                workDir.mkdirs()
                arg(line: "-workDir ${workDir}")
            }

            if (extraDir) {
                extraDir.mkdirs()
                arg(line: "-extra ${extraDir}")
            }

            modules.each {
                logger.info("GWT Module {}", it)
                arg(value: it)
            }
        }
    }

}
