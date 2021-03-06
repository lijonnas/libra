package com.envisioncn.gssc.libra.batch.tasklets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.Date;
import java.util.UUID;

/**
 * A dummy tasklet for testing purposes.
 * @author jonnas
 * @date 2021-03-31
 */
public class DummyTasklet implements StoppableTasklet, Tasklet {
    private String uid = UUID.randomUUID().toString();
    private static final Logger log = LoggerFactory.getLogger(DummyTasklet.class);
    private static int failureCount = 0;

    boolean stopping = false;
    String name;
    int repeats = 10;
    int sleepTimeMs = 1000;
    Integer checkCount = null;
    String jobName;
    int emulateFailures = 0;

    public DummyTasklet() {
        log.debug("Creating DummyTasklet {}, {}", name, uid);
    }

    @Override
    public RepeatStatus execute(StepContribution sc, ChunkContext cc) throws Exception {
        if (stopping) {
            log.debug("Should stop");
            sc.setExitStatus(ExitStatus.STOPPED);
            stopping = false;
            return RepeatStatus.FINISHED;
        }
        boolean finished = false;

        if (jobName == null) {
            // Schedule
            log.debug("Scheduling..");
            jobName = "Dummy" + new Date();
            Thread.sleep(sleepTimeMs);
            log.debug("Done!");
        } else {
            if (checkCount == null) {
                checkCount = 1;
            } else {
                checkCount = checkCount + 1;
            }

            Thread.sleep(sleepTimeMs);
            if (checkCount > repeats) {
                finished = true;
            }
        }


        if (finished) {
            checkCount = 0;
            if (failureCount < emulateFailures) {
                failureCount++;
                throw new JobExecutionException("Emulated execution failure. Failure count: " + failureCount);
            }
            failureCount=0;
            return RepeatStatus.FINISHED;
        } else {
            return RepeatStatus.CONTINUABLE;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRepeats() {
        return repeats;
    }

    public void setRepeats(int repeats) {
        this.repeats = repeats;
    }

    public int getSleepTimeMs() {
        return sleepTimeMs;
    }

    public void setSleepTimeMs(int sleepTimeMs) {
        this.sleepTimeMs = sleepTimeMs;
    }

    @Override
    public void stop() {
        log.debug("stop() called. Stopping");
        stopping = true;
    }

    public void setEmulateFailures(int emulateFailures) {
        this.emulateFailures = emulateFailures;
    }
}
