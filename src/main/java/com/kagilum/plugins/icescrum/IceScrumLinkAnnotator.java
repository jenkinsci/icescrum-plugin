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
