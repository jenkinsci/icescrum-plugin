package com.kagilum.plugins.icescrum;


import hudson.model.Action;

public class IceScrumLinkAction implements Action {

    private final transient IceScrumProjectProperty projectProperty;

    public IceScrumLinkAction(IceScrumProjectProperty iceScrumProjectProperty) {
        this.projectProperty = iceScrumProjectProperty;
    }

    public String getDisplayName() {
        return "iceScrum";
    }

    public String getIconFileName() {
        return "/plugin/icescrum/logo.png";
    }

    public String getUrlName() {
        return projectProperty.getSettings().getProjectUrl();
    }
}
