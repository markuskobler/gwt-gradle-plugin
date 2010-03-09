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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.War
import org.gradle.api.tasks.testing.Test

/**
 *
 * @author Markus Kobler
 */
class Gwt2Plugin implements Plugin<Project> {

    public static final String COMPILE_GWT_TASK_NAME = "compileGwt"
    public static final String GWT_DEV_MODE_TASK_NAME = "gwtDevMode"
    public static final String CLEAN_TASK_NAME = "clean"

    public static final String GWT_CONFIGURATION_NAME = "gwt"

    void use(final Project project) {

        project.getPlugins().usePlugin(JavaPlugin.class);

        configureConfigurations(project)

        Gwt2PluginConvention pluginConvention = configureConventions(project)
        project.convention.plugins.gwt = pluginConvention
        
        configureGwtDependenciesIfVersionSpecified(project, pluginConvention)
        
        configureJarTaskDefaults(project, pluginConvention)
        configureTestTaskDefaults(project, pluginConvention)
        configureWarTaskDefaults(project, pluginConvention)

        addCompileGwt(project)
        addGwtDevMode(project)
    }

    private void configureConfigurations(final Project project) {
        ConfigurationContainer configurations = project.configurations;
        configurations.add(GWT_CONFIGURATION_NAME).
                setVisible(false).
                extendsFrom(configurations.getByName(JavaPlugin.COMPILE_CONFIGURATION_NAME)).
                setDescription("GWT libraries to be used to compile this GWT project.")
    }


    private void configureGwtDependenciesIfVersionSpecified(final Project project, final Gwt2PluginConvention convention) {
        project.getGradle().getTaskGraph().whenReady {TaskExecutionGraph taskGraph ->
            
            if( convention.gwtVersion != null && convention.gwtVersion.trim().length() > 0) {

                def version = convention.gwtVersion.trim()
                
                ExternalModuleDependency dependency = new DefaultExternalModuleDependency("com.google.gwt", "gwt-dev", version )
                project.configurations.getByName(GWT_CONFIGURATION_NAME).addDependency(dependency)

                if (project.getTasks().findByName(WarPlugin.WAR_TASK_NAME) != null) {
                    dependency = new DefaultExternalModuleDependency("com.google.gwt", "gwt-user", version )
                    project.configurations.getByName(WarPlugin.PROVIDED_COMPILE_CONFIGURATION_NAME).addDependency(dependency)

                    dependency = new DefaultExternalModuleDependency("com.google.gwt", "gwt-servlet", version )
                    project.configurations.getByName(JavaPlugin.RUNTIME_CONFIGURATION_NAME).addDependency(dependency)
                } else {
                    
                    dependency = new DefaultExternalModuleDependency("com.google.gwt", "gwt-user", version )
                    project.configurations.getByName(JavaPlugin.COMPILE_CONFIGURATION_NAME).addDependency(dependency)
                    
                }

            }
        }        
    }


    private Gwt2PluginConvention configureConventions(final Project project) {

        Gwt2PluginConvention pluginConvention = new Gwt2PluginConvention(project)

        project.tasks.withType(AbstractGwtTask.class).allTasks {AbstractGwtTask task ->
            task.conventionMapping.classpath = {
                SourceSet mainSourceSet = project.convention.getPlugin(JavaPluginConvention.class).sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
                project.files(mainSourceSet.resources.srcDirs,
                        mainSourceSet.java.srcDirs,
                        mainSourceSet.classesDir,
                        project.configurations.getByName(GWT_CONFIGURATION_NAME));
            }
            task.conventionMapping.modules = { pluginConvention.gwtModules }
            task.conventionMapping.logLevel = { pluginConvention.gwtLogLevel }
        }

        project.tasks.withType(CompileGwtTask.class).allTasks {CompileGwtTask task ->
            task.conventionMapping.buildDir = { pluginConvention.gwtBuildDir }
        }

        project.tasks.withType(GwtDevModeTask.class).allTasks {GwtDevModeTask task ->
            task.conventionMapping.startupUrls = { pluginConvention.gwtStartupUrls }
            task.conventionMapping.buildDir = { pluginConvention.gwtBuildDir }
            task.conventionMapping.warDir = { pluginConvention.gwtWarDir }
            task.conventionMapping.webApp = {
                War war = (War) project.getTasks().findByName(WarPlugin.WAR_TASK_NAME)
                war != null ? war.getArchivePath() : null
            }
        }

        return pluginConvention
    }

    private void configureJarTaskDefaults(final Project project, final Gwt2PluginConvention pluginConvention) {
        Jar jarTask = (Jar) project.getTasks().findByName(JavaPlugin.JAR_TASK_NAME)
        if (jarTask != null) {
            SourceSet mainSourceSet = project.convention.getPlugin(JavaPluginConvention.class).sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
            jarTask.from(mainSourceSet.java.matching { include("**/client/**") });
        }
    }

    private void configureWarTaskDefaults(final Project project, final Gwt2PluginConvention pluginConvention) {
        project.tasks.withType(War.class).allTasks({War task ->
            task.dependsOn(COMPILE_GWT_TASK_NAME);
            task.from(project.fileTree(pluginConvention.gwtBuildDir));
        });
    }

    private void configureTestTaskDefaults(final Project project, final Gwt2PluginConvention pluginConvention) {
        project.tasks.withType(Test.class).allTasks {Test test ->

            test.conventionMapping.classpath = {

                SourceSet testSourceSet = project.convention.getPlugin(JavaPluginConvention.class).sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME);
                SourceSet mainSourceSet = project.convention.getPlugin(JavaPluginConvention.class).sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)

                project.files(testSourceSet.java.srcDirs,
                        testSourceSet.runtimeClasspath,
                        mainSourceSet.java.srcDirs,
                        mainSourceSet.classesDir,
                        project.configurations.getByName(GWT_CONFIGURATION_NAME));
            }

        }
    }

    private void addCompileGwt(final Project project) {
        CompileGwtTask compileGwt = project.tasks.add(COMPILE_GWT_TASK_NAME, CompileGwtTask.class)
        compileGwt.dependsOn(JavaPlugin.COMPILE_JAVA_TASK_NAME)
        compileGwt.description = "Compile GWT Modules"
    }

    private void addGwtDevMode(final Project project) {
        GwtDevModeTask gwtDevMode = project.tasks.add(GWT_DEV_MODE_TASK_NAME, GwtDevModeTask.class)
        gwtDevMode.dependsOn(COMPILE_GWT_TASK_NAME)
        gwtDevMode.description = "Run's GWT Developer Mode"

        // Also depend on WAR plugin if included
        if (null != project.getTasks().findByName(WarPlugin.WAR_TASK_NAME)) {
            gwtDevMode.dependsOn(WarPlugin.WAR_TASK_NAME)
        }        
    }

}
