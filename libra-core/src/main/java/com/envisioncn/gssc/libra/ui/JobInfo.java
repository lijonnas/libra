package com.envisioncn.gssc.libra.ui;

import com.envisioncn.gssc.libra.core.BasicJobInstanceInfo;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Tag;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.Locale;

/**
 * @author zhongshuangli
 * @date 2021-04-01
 */
@Tag("div")
public class JobInfo extends Component {

    private static final long serialVersionUID = -3836905025140238997L;

    public JobInfo(BasicJobInstanceInfo job) {

        getElement().setAttribute("class", "job-info");
        update(job);
    }
    public void update(BasicJobInstanceInfo job) {

        StringBuilder html = new StringBuilder("<ul style='list-style: none'>");
        if (job.getInstanceId() != null) {
            html.append("<li>Instance/Execution: ")
                    .append(job.getInstanceId())
                    .append("/")
                    .append(job.getLatestExecutionId());
        }
        if (job.getExecutionCount() > 0 ) {
            html.append("<li>Executions: ").append(job.getExecutionCount());
        }
        if (job.getExecutionStatus() != null) {
            html.append("<li>Status: <div class='batch_status batch_status_label' data-status=")
                    .append(job.getExecutionStatus())
                    .append(">")
                    .append(job.getExecutionStatus()).append("</div>");
        }
        if (job.getStartTime() != null) {
            html.append("<li>Started: ").append(DateFormatUtils.format(job.getStartTime(), "yyMMdd HH:mm:ss", Locale.US));
        }
        if (job.getEndTime() != null) {
            html.append("<li>Ended: ").append(DateFormatUtils.format(job.getEndTime(), "yyMMdd HH:mm:ss", Locale.US));
        }
        if (job.getLatestDuration()  > 0) {
            html.append("<li>Latest Duration: ").append(DurationFormatUtils.formatDuration(job.getLatestDuration(), "HH:mm:ss"));
        }
        if (job.getDuration() > 0) {
            html.append("<li>Total Duration: ").append(DurationFormatUtils.formatDuration(job.getDuration(), "HH:mm:ss"));
        }
        if (job.getExitStatus() != null && !"UNKNOWN".equals(job.getExitStatus())) {
            html.append("<li>Exit status: <div class='batch_status batch_status_label' data-status=")
                    .append(job.getExitStatus())
                    .append(">")
                    .append(job.getExitStatus())
                    .append("</div>");
        }
        if (job.getJobParameters() != null && job.getJobParameters().size() > 0) {
            html.append("<li>Params: ").append(job.getJobParameters().toString());
        }
        this.getElement().removeAllChildren();
        this.getElement().appendChild(new Html(html.toString()).getElement());
    }
}

