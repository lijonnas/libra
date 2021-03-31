package com.envision.gssc.libra.scheduling;

import lombok.Data;

/**
 * @author jonnas
 * @date 2021-03-31
 */
@Data
public class JobTrigger {
    public enum TriggerType {CRON, JOB_COMPLETION}
    Long id;

    TriggerType triggerType;
    String triggeringJobName;
    String jobName;
    String cron;
    String jobParameters;
}
