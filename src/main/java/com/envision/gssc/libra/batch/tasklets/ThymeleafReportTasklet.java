package com.envision.gssc.libra.batch.tasklets;

import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.TemplateEngine;

/**
 * Report tasklet based on Thymeleaf templating
 * @author zhongshuangli
 * @date 2021-04-01
 */
public abstract class ThymeleafReportTasklet extends ReportTasklet {
    @Autowired
    protected TemplateEngine templateEngine;

    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    public void setTemplateEngine(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }
}
