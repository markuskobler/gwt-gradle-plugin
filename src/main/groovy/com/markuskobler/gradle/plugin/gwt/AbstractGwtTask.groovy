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

import org.gradle.api.file.FileCollection
import org.gradle.api.internal.ConventionTask
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.gradle.api.AntBuilder

/**
 *
 * @author Markus Kobler
 */
abstract class AbstractGwtTask extends ConventionTask {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    static final String GWT_CLASSPATH_ID = 'gwt.classpath'

    FileCollection classpath  
    JavaOptions options = new JavaOptions();
    String logLevel ='INFO'
    File genDir;    
    File extraDir    
    File workDir

    List<String> modules

    Iterable<File> getClasspath() {
      return classpath;
    }

    protected void configureAntClasspath(AntBuilder ant, Iterable classpath) {
        ant.path(id: GWT_CLASSPATH_ID) {
            classpath.each {
                logger.debug("Add {} to GWT classpath!", it)
                pathelement(location: it)
            }
        }
    }

}
