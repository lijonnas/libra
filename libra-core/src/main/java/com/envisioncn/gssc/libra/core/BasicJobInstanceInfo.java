package com.envisioncn.gssc.libra.core;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author jonnas
 * @date 2021-03-31
 */
@Getter
@Setter
public class BasicJobInstanceInfo {
    protected String name;
    protected String description;
    private String executionStatus = "UNKNOWN";
    private String exitStatus = "UNKNOWN";
    private Long latestExecutionId;
    private Long instanceId;
    private Date startTime;
    private Date endTime;
    private long duration;
    private long latestDuration;
    private Properties jobParameters;
    private List<String> requiredJobParamKeys;
    private List<String> optionalJobParamKeys;
    private int executionCount = 0;
    protected int completedCount = 0;
    protected int runningCount = 0;
    protected int unknownCount = 0;
    protected int stoppedCount = 0;
    protected int failedCount = 0;



    public BasicJobInstanceInfo(String name) {
        this.name = name;
    }



    public boolean isRestartable() {
        return latestExecutionId != null && ("STOPPED".equals(exitStatus) || "FAILED".equals(exitStatus));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicJobInstanceInfo jobInstanceInfo = (BasicJobInstanceInfo) o;
        return name.equals(jobInstanceInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "BasicJobInstanceInfo{" +
                "name='" + name + '\'' +
                ", executionStatus='" + executionStatus + '\'' +
                ", executionId=" + latestExecutionId +
                ", instanceId=" + instanceId +
                '}';
    }


    /**
     * Does this job take any job parameters?
     */
    public boolean hasJobParameters() {
        return (optionalJobParamKeys != null && optionalJobParamKeys.size() > 0) || (requiredJobParamKeys != null && requiredJobParamKeys.size() > 0);
    }

}
