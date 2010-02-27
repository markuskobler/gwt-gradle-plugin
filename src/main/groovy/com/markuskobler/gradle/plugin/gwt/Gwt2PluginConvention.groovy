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

import org.gradle.api.Project

/**
 * 
 * @author Markus Kobler
 */
class Gwt2PluginConvention {

    Project project

    String gwtVersion

    String gwtBuildDirName = "build/gwt/out"
    String gwtWarDirName   = "build/gwt/war"
    String gwtLogLevel
    List<String> gwtModules
    List<String> gwtStartupUrls

    Gwt2PluginConvention(project) {
        this.project = project
    }

    File getGwtBuildDir() {
        project.file(gwtBuildDirName)
    }

    File getGwtWarDir() {
        project.file(gwtWarDirName)
    }

    void setGwtModules(List<String> modules) {
        gwtModules = modules
    }

    void setGwtModule(String module) {
        gwtModules = [module]
    }

    void setGwtStartupUrls(List<String> urls) {
        gwtStartupUrls = urls
    }

    void setGwtStartupUrl(String url) {
        gwtStartupUrls = [url]
    }

}
