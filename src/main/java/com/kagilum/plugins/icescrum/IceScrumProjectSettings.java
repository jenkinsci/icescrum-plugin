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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IceScrumProjectSettings {

    private String url;
    private String pkey;
    private String username = null;
    private String password = null;
    private static final String PATTERN_ICESCRUM_URL = "(http|https)://(.*)/p/([0-9A-Z]*)";

    public IceScrumProjectSettings(String url){
        Pattern pattern = Pattern.compile(PATTERN_ICESCRUM_URL);
        Matcher matches = pattern.matcher(url);
        if (matches.matches()){
            this.url = matches.group(1) +"://"+ matches.group(2);
            this.pkey = matches.group(3);
        }
    }

    public IceScrumProjectSettings(String url, String username, String password){
        this(url);
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPkey() {
        return pkey;
    }

    public String getUrl() {
        return url;
    }

    public String getPassword() {
        return password;
    }

    public String getProjectUrl(){
        return this.url + "/p/" + this.pkey;
    }

    public boolean hasAuth(){
        return this.password != null && this.username != null;
    }

    public static boolean isValidUrl(String url){
        Pattern pattern = Pattern.compile(PATTERN_ICESCRUM_URL);
        Matcher matches = pattern.matcher(url);
        return matches.matches();
    }
}
