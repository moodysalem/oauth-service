package com.oauth2cloud.server.rest;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;

public class EmailTemplateFreemarkerConfiguration extends Configuration {
    public EmailTemplateFreemarkerConfiguration() {
        super(Configuration.VERSION_2_3_23);
        setTemplateLoader(new ClassTemplateLoader(this.getClass().getClassLoader(), "/templates/email"));
        setDefaultEncoding("UTF-8");
    }
}
