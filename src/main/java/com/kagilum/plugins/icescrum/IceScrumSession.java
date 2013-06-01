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

import net.sf.json.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IceScrumSession {


    public static final int BUILD_SUCCESS = 1;
    public static final int BUILD_FAILURE = 5;
    public static final int BUILD_ERROR = 10;
    public static final String TASK_PATTERN = "T(\\d+)-?(\\d+\\.\\d+|\\d+\\,\\d+|\\d+)?";

    private IceScrumProjectSettings settings;
    private HttpClient client;
    private String httpError = null;

    public IceScrumSession(IceScrumProjectSettings settings) {
        this.settings = settings;
        initClient();
    }

    public boolean isConnect() {
        GetMethod method = new GetMethod(settings.getUrl() + "/ws/p/" + settings.getPkey() + "/version/");
        return executeMethod(method);
    }

    public boolean sendBuildStatut(JSONObject build) throws UnsupportedEncodingException {
        PostMethod method = new PostMethod(settings.getUrl() + "/ws/p/" + settings.getPkey() + "/build");
        StringRequestEntity requestEntity = new StringRequestEntity(build.toString(),"application/json","UTF-8");
        method.setRequestEntity(requestEntity);
        return executeMethod(method);
    }

    private void initClient() {
        client = new HttpClient();
    }

    private boolean executeMethod(PostMethod method){
        boolean result = false;
        try {
            setAuthentication();
            client.executeMethod(method);
            int code = method.getStatusCode();
            if (code != HttpStatus.SC_OK) {
                checkServerStatus(code);
            }else {
                result = true;
            }
        } catch (IOException e) {
            httpError = e.getMessage();
            LOGGER.log(Level.WARNING, httpError, e);
        }finally {
            method.releaseConnection();
        }
        return result;
    }

    private boolean executeMethod(GetMethod method){
        boolean result = false;
        try {
            setAuthentication();
            method.setRequestHeader("Content-type","text/json");
            client.executeMethod(method);
            int code = method.getStatusCode();
            if (code != HttpStatus.SC_OK) {
                checkServerStatus(code);
            }else {
                result = true;
            }
        } catch (IOException e) {
            httpError = e.getMessage();
            LOGGER.log(Level.WARNING, httpError, e);
        }finally {
            method.releaseConnection();
        }
        return result;
    }

    private void setAuthentication() throws MalformedURLException {
        int port;
        URL url = new URL(settings.getUrl() + "/ws/p/" + settings.getPkey() + "/task/");
        if (url.getPort() == -1) {
            port = url.getDefaultPort();
        } else {
            port = url.getPort();
        }
        client.getState().setCredentials(new AuthScope(url.getHost(), port),new UsernamePasswordCredentials(settings.getUsername(), settings.getPassword()));
    }

    private void checkServerStatus(int code) throws IOException {
        switch(code){
            case HttpStatus.SC_SERVICE_UNAVAILABLE:
                throw new IOException(Messages.IceScrumSession_icescrum_http_unavailable());
            case HttpStatus.SC_UNAUTHORIZED:
                throw new IOException(Messages.IceScrumSession_icescrum_http_unauthorized());
            case HttpStatus.SC_FORBIDDEN:
                throw new IOException(Messages.IceScrumSession_icescrum_http_forbidden());
            case HttpStatus.SC_NOT_FOUND:
                throw new IOException(Messages.IceScrumSession_icescrum_http_notfound());
            default:
                throw new IOException(Messages.IceScrumSession_icescrum_http_error()+" (" + HttpStatus.getStatusText(code) + ")");
        }
    }

    private static final Logger LOGGER = Logger.getLogger(IceScrumSession.class.getName());

    public String getLastError() {
        return httpError != null ? httpError : "";
    }
}
