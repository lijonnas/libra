package com.envisioncn.gssc.libra.core;

/**
 * @author jonnas
 * @date 2021-03-31
 */
public interface JobEventListener {
    public void onJobChange(JobInstanceInfo jobInstanceInfo);
    public void onStepChange(BasicJobInstanceInfo basicJobInstanceInfo, StepInfo stepInfo);
}
