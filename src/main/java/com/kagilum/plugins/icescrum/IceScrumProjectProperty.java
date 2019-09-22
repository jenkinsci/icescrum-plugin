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
import hudson.model.*;
import hudson.util.FormValidation;
import hudson.util.Secret;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import static org.apache.commons.lang.StringUtils.isEmpty;

public final class IceScrumProjectProperty extends JobProperty<AbstractProject<?, ?>> {

    private IceScrumProjectSettings settings;

    @DataBoundConstructor
    public IceScrumProjectProperty(String url, String username, Secret password, String accessToken, String authType) {
        if (username != null && password != null && authType != null && authType.equals(IceScrumProjectSettings.AUTH_TYPE_BASIC))
            this.settings = new IceScrumProjectSettings(url, username, password);
        else if (accessToken != null && authType != null &&  authType.equals(IceScrumProjectSettings.AUTH_TYPE_TOKEN))
            this.settings = new IceScrumProjectSettings(url, accessToken);
        else {
            this.settings = new IceScrumProjectSettings(url);
        }
    }

    public IceScrumProjectSettings getSettings() {
        return this.settings;
    }

    @Override
    public Collection<? extends Action> getJobActions(AbstractProject<?, ?> job) {
        if (settings != null) {
            return Collections.singleton(new IceScrumLinkAction(this));
        }
        return Collections.emptyList();
    }

    @Extension
    public static final class DescriptorImpl extends JobPropertyDescriptor {

        public DescriptorImpl() {
            super(IceScrumProjectProperty.class);
            load();
        }

        public boolean isApplicable(Class<? extends Job> jobType) {
            return AbstractProject.class.isAssignableFrom(jobType);
        }

        public String getDisplayName() {
            return Messages.IceScrumProjectProperty_icescrum_projectProperty_displayName();
        }

        @RequirePOST
        public FormValidation doCheckUrl(@QueryParameter String value) {
            if(IceScrumProjectSettings.isValidUrl(value))
                return FormValidation.ok();
            else
                return FormValidation.error(Messages.IceScrumProjectProperty_icescrum_error_url());
        }

        @RequirePOST
        public FormValidation doLoginCheck(@QueryParameter("icescrum.accessToken") final String accessToken,
                                           @QueryParameter("icescrum.url") final String url) throws IOException, ServletException {

            if(!IceScrumProjectSettings.isValidUrl(url))
                return FormValidation.error(Messages.IceScrumProjectProperty_icescrum_error_url());

            if (isEmpty(accessToken) || isEmpty(url)){
                return FormValidation.error(Messages.IceScrumProjectProperty_icescrum_parameters_missing());
            } else {
                IceScrumProjectSettings settings = new IceScrumProjectSettings(url, accessToken);
                IceScrumSession session = new IceScrumSession(settings);
                if(!session.isConnect()){
                    return FormValidation.ok(session.getLastError());
                }
            }
            return FormValidation.ok(Messages.IceScrumProjectProperty_icescrum_connection_successful());
        }

        @RequirePOST
        public FormValidation doOldLoginCheck(@QueryParameter("icescrum.username") final String username,
                                              @QueryParameter("icescrum.password") final Secret password,
                                              @QueryParameter("icescrum.url") final String url) throws IOException, ServletException {

            if(!IceScrumProjectSettings.isValidUrl(url))
                return FormValidation.error(Messages.IceScrumProjectProperty_icescrum_error_url());

            if (isEmpty(username) || isEmpty(password.getPlainText()) || isEmpty(url)){
                return FormValidation.error(Messages.IceScrumProjectProperty_icescrum_parameters_missing());
            } else {
                IceScrumProjectSettings settings = new IceScrumProjectSettings(url, username, password);
                IceScrumSession session = new IceScrumSession(settings);
                if(!session.isConnect()){
                    return FormValidation.ok(session.getLastError());
                }
            }
            return FormValidation.ok(Messages.IceScrumProjectProperty_icescrum_connection_successful());
        }


        @Override
        public JobProperty<?> newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            if(req != null){
                IceScrumProjectProperty ipp = req.bindJSON(IceScrumProjectProperty.class, formData);
                if (ipp.getSettings() == null) {
                    ipp = null; // not configured
                }
                return ipp;
            }
            return null;
        }

    }
}
