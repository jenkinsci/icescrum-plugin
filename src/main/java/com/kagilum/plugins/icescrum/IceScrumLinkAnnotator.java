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
import hudson.MarkupText;
import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogAnnotator;
import hudson.scm.ChangeLogSet;
import java.util.regex.Pattern;

@Extension
public class IceScrumLinkAnnotator extends ChangeLogAnnotator {
    @Override
    public void annotate(AbstractBuild<?, ?> abstractBuild, ChangeLogSet.Entry change, MarkupText markupText) {
        final IceScrumProjectProperty p = abstractBuild.getProject().getProperty(IceScrumProjectProperty.class);
        if (null == p || null == p.getSettings()) {
            return;
        }
        annotate(p.getSettings().getProjectUrl(), markupText);
    }

    public void annotate(String url, final MarkupText text) {
        for (MarkupText.SubText st : text.findTokens(Pattern.compile(IceScrumSession.TASK_PATTERN))) {
            if (st.group(2) == null){
                String detailsI18n = Messages.IceScrumLinkAnnotator_icescrum_link_details();
                st.surroundWith("<a href='" + url + "-T$1' title='"+detailsI18n+"'>", "</a>");
            } else {
                String detailsI18n = Messages.IceScrumLinkAnnotator_icescrum_link_details_time();
                st.surroundWith("<a href='" + url + "-T$1' title='"+detailsI18n+"'>", "</a>");
            }
        }
    }
}
