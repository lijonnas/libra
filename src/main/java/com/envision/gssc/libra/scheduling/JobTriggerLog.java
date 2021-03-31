package com.envision.gssc.libra.scheduling;

import lombok.Data;

import java.util.Date;

/**
 * @author jonnas
 * @date 2021-03-31
 */
@Data
public class JobTriggerLog {
    Date tstamp;
    String status;
    JobTrigger.TriggerType triggerType;
    String triggeringJobName;
    String jobName;
    String cron;
    String errorMsg;
}
