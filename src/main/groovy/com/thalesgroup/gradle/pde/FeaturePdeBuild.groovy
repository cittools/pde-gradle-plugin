/** *****************************************************************************
 * Copyright (c) 2009 Thales Corporate Services SAS                             *
 * Author : Gregory Boissinot                                                   *
 *                                                                              *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 ****************************************************************************** */

package com.thalesgroup.gradle.pde;


import java.lang.reflect.Field
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.thalesgroup.gradle.pde.tasks.feature.*

public class FeaturePdeBuild implements Plugin<Project> {
    
    public static final String CLEAN_TASK_NAME = "pdeClean";
    public static final String INIT_TASK_NAME = "pdeInit";
    public static final String PROCESS_RESOURCES_TASK_NAME = "pdeProcessResources";
    public static final String PDE_BUILD_TASK_NAME = "pdeBuild";
    public static final String UPLOAD_TASK_NAME = "pdeUpload";
    
    
    public void apply(final Project project) {
        FeaturePdeConvention featurePdeConvention = new FeaturePdeConvention(project);
        project.setProperty("FeaturePde", featurePdeConvention);
        configureClean(project);
        configureInit(project);
        configureProcessResources(project);
        configurePdeBuild(project);
        configureDeploy(project);
    }
    
    private void configureClean(Project project) {
        project.getTasks().add(CLEAN_TASK_NAME, CleanFeatureTask.class).setDescription("Deletes the build directory");
    }
    
    
    private void configureInit(Project project) {
        project.getTasks().add(INIT_TASK_NAME, InitFeatureTask.class).setDescription("Initializes the build directory and the target platform");
    }
    
    
    private void configureProcessResources(Project project) {
        project.getTasks().withType(ResourceFeatureTask.class).allTasks(new Action<ResourceFeatureTask>() {
                    public void execute(ResourceFeatureTask task) {
                        task.dependsOn(INIT_TASK_NAME);
                    }
                });
        project.getTasks().add(PROCESS_RESOURCES_TASK_NAME, ResourceFeatureTask.class).setDescription("Processes PDE resources");
    }
    
    
    private void configurePdeBuild(final Project project) {
        project.getTasks().withType(PdeFeatureTask.class).allTasks(new Action<PdeFeatureTask>() {
                    public void execute(PdeFeatureTask pdeTask) {
                        pdeTask.dependsOn(PROCESS_RESOURCES_TASK_NAME);
                    }
                });
        project.getTasks().add(PDE_BUILD_TASK_NAME, PdeFeatureTask.class).setDescription("Launches the PDE build process");
    }
    
    
    private void configureDeploy(Project project) {
        project.getTasks().withType(DeployFeatureTask.class).allTasks(new Action<DeployFeatureTask>() {
                    public void execute(DeployFeatureTask task) {
                        task.dependsOn(PDE_BUILD_TASK_NAME);
                    }
                });
        project.getTasks().add(UPLOAD_TASK_NAME, DeployFeatureTask.class).setDescription("Unzips artifacts produced by the PDE build into the publish directory");
    }
}

