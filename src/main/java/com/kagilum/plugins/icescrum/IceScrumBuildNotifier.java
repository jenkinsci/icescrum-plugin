/*
 * Copyright 2013 Kagilum SAS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author(s):
 *
 * Vincent Barrier (vbarrier@kagilum.com)
 */

package com.kagilum.plugins.icescrum;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.scm.ChangeLogSet;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IceScrumBuildNotifier extends Notifier {


    @DataBoundConstructor
    public IceScrumBuildNotifier() {
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        final IceScrumProjectProperty p = build.getProject().getProperty(IceScrumProjectProperty.class);
        if (null == p || null == p.getSettings() || !p.getSettings().hasAuth()) {
            return true;
        }

        IceScrumSession session = new IceScrumSession(p.getSettings());
        JSONObject jsonRoot = createIceScrumBuildObject(build, listener, IceScrumSession.TASK_PATTERN, !p.getSettings().isTokenAuth());

        if (session.sendBuildStatut(jsonRoot)) {
            listener.getLogger().println(Messages.IceScrumBuildNotifier_icescrum_build_success()+p.getSettings().getProjectUrl()+")");
        } else {
            listener.getLogger().println(Messages.IceScrumBuildNotifier_icescrum_build_error()+p.getSettings().getProjectUrl()+Messages.IceScrumBuildNotifier_icescrum_build_error_check());
        }
        return true;
    }

    public JSONObject createIceScrumBuildObject(AbstractBuild<?, ?> build, BuildListener listener, String pattern, boolean includeBuiltOn){
        Hudson instance =  Hudson.getInstance();
        String url = instance != null ? instance.getRootUrl() : "";
        String jobUrl = url != null ? url+"job/"+build.getProject().getName()+"/" : "";

        JSONObject jsonData = new JSONObject();
        JSONObject jsonBuild = new JSONObject();

        jsonBuild.element("jobName",build.getProject().getDisplayName());
        jsonBuild.element("name",build.getDisplayName());
        jsonBuild.element("number",build.getNumber());
        jsonBuild.element("date", build.getTimeInMillis());
        jsonBuild.element("url", jobUrl+build.getNumber());

        //only for old icescrum server
        if(includeBuiltOn){
            jsonBuild.element("builtOn", "Jenkins: "+build.getHudsonVersion());
        }

        Result result = build.getResult();
        if(result != null){
            if (result.isBetterOrEqualTo(Result.SUCCESS)) {
                jsonBuild.element("status",IceScrumSession.BUILD_SUCCESS);
            } else if (result.isBetterOrEqualTo(Result.UNSTABLE)) {
                jsonBuild.element("status", IceScrumSession.BUILD_FAILURE);
            } else {
                jsonBuild.element("status",IceScrumSession.BUILD_ERROR);
            }
        } else {
            jsonBuild.element("status",IceScrumSession.BUILD_ERROR);
        }

        ArrayList<Integer> ids = new ArrayList<Integer>();
        if (!build.getChangeSet().isEmptySet()){
            for (ChangeLogSet.Entry change : build.getChangeSet()) {
                Matcher m = Pattern.compile(pattern).matcher(change.getMsg());
                while (m.find()) {
                    if (m.groupCount() >= 1) {
                        ids.add(Integer.parseInt(m.group(1)));
                    }
                }

            }
            jsonBuild.element("tasks", ids);
        }

        if (ids.size() == 0){
            listener.getLogger().println(Messages.IceScrumBuildNotifier_icescrum_build_empty());
        }

        jsonData.element("build",jsonBuild);
        return jsonData;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return Messages.IceScrumBuildNotifier_icescrum_notifier_displayName();
        }
    }
}