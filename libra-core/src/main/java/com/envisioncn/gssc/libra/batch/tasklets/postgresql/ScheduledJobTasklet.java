package com.envisioncn.gssc.libra.batch.tasklets.postgresql;


import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Offers a way to run PLSQL code asynchronously. Typically by calling a stored procedure.
 * <p>
 * Runs a PLSQL block asynchronously once as a scheduled job using the Oracle DBMS_SCHEDULER.
 * See https://docs.oracle.com/en/database/oracle/oracle-database/19/arpls/DBMS_SCHEDULER.html#GUID-A24DEB5D-2EAF-4C0B-8715-30DC947B3F87
 * <p>
 * The tasklet starts the block asynchronously and then continuously checks the status of the job in the DB.
 * <p>
 * Will essentially run the equivalent of:
 * <code>
 * begin
 * SYS.DBMS_SCHEDULER.CREATE_JOB(
 * job_name=&gt;'MY_JOB123',
 * job_type=&gt;'PLSQL_BLOCK',
 * job_action=&gt;'begin MY_PROCEDURE(''some_argument'',123);end;',
 * enabled=&gt;TRUE);
 * end;
 * /
 * </code>
 */
public class ScheduledJobTasklet implements StoppableTasklet {

    private final Logger log = LoggerFactory.getLogger(ScheduledJobTasklet.class);
    private String action;

    /* Interval (in ms) between status checks.  */
    private Long statusCheckInterval = 30000L;

    /**
     * The maximum length of the DBMS_SCHEDULER job name. In older versions of Oracle, the max length is 30 characteres, in newer 255.
     */
    private int schedulerNameMaxLength = 255;

    private boolean stopped = false;

    private Object sleepObject = new Object();

    @Autowired
    @Qualifier("stagingDataSource")
    DataSource dataSource;

    /**
     * @param action The PLSQL block to run. Typically calls a stored procedure, e.g.
     * <code>
     * "begin my_procedure(''some_argument'', 123);end;"
     * </code>
     *
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * How often to query Oracle and check the status of the scheduled job.
     *
     * @param statusCheckInterval The interval between checks in ms. Valid values are 10000 - 300000
     */
    public void setStatusCheckInterval(Long statusCheckInterval) {
        if (statusCheckInterval < 1000 || statusCheckInterval > 300000) {
            throw new IllegalArgumentException("Illegal vaue for statusCheckInterval. Valid values are 1000 - 300000");
        }
        this.statusCheckInterval = statusCheckInterval;
    }

    public void setSchedulerNameMaxLength(int schedulerNameMaxLength) {
        this.schedulerNameMaxLength = schedulerNameMaxLength;
    }

    @Override
    public void stop() {
        log.debug("Stopping..");
        stopped = true;
        synchronized (sleepObject) {
            sleepObject.notifyAll();
        }

    }
    private void init() {
        this.stopped = false;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.debug("execute");
        init();
        // Create a unique JOB_NAME.
        // TODO: This could _potentially_ create non-unique names if another step is started very close in time.
        String stepName = chunkContext.getStepContext().getStepName();

        String dbmsJobName = createSchedulerJobName(stepName);
        log.debug("Creating a new scheduled DBMS job. Job name will be '{}'", dbmsJobName);
        schedule(dbmsJobName, contribution);
        Map<String, Object> outParams = new HashMap<>();
        outParams.put("JOB_NAME", dbmsJobName);
        chunkContext.getStepContext().getStepExecution().getExecutionContext().put("outParams", outParams);
        long checkInterval = 500;
        do {
            log.debug("Sleeping for {}ms", checkInterval);
            synchronized (sleepObject) {
                try {
                    sleepObject.wait(checkInterval);
                }
                catch(InterruptedException ie) {
                    log.debug("sleep interrupted");
                }
            }
            log.debug("Done sleeping");
            checkInterval = Math.min(checkInterval*2, statusCheckInterval);
            log.debug("Time to check status");
            String status = checkStatus(dbmsJobName, contribution);
            if ("SUCCEEDED".equals(status)) {
                return RepeatStatus.FINISHED;
            } else if ("STOPPED".equals(status)) {
                log.debug("Job is stopped");
                throw new JobExecutionException("Scheduled job " + dbmsJobName + " was stopped");
            } else if (!StringUtils.isEmpty(status)) {
                // Not any of the expected values
                throw new JobExecutionException("Scheduled job " + dbmsJobName + " finished with (unexpected) status '" + status + "'");
            } else {
                // We could just assume that the job is running, but this is a more robust way to make sure the job is running
                boolean running = checkRunning(dbmsJobName, contribution, outParams);
                //if (!running) throw new JobExecutionException("Job " + dbmsJobName + " does not seem to be running");
            }
        }
        while (!this.stopped);

        stopScheduledJob(dbmsJobName);
        log.debug("Stop command sent. End as FAILED");
        throw new JobExecutionException("Job " + dbmsJobName + " was stopped");

    }

    /**
     * Max length of job name is 30 in older versions of oracle. 255 in newer. Oracle also converts it to uppercase. Must not begin with '_', therefor prepending 'J'
     * @param stepName
     * @return
     */
    protected final String createSchedulerJobName(String stepName) {
        SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmssSSS");
        // Max length of job name is 30. Oracle also converts it to uppercase. Must not begin with '_', therefor prepending 'J'
        if (schedulerNameMaxLength < 31) {
            return "J" + StringUtils.right(stepName.toUpperCase().replace("-", "_") + format.format(new Date()), schedulerNameMaxLength - 1);
        }
        else {
            return "J_" + StringUtils.right(stepName.toUpperCase().replace("-", "_") + "_" + format.format(new Date()), schedulerNameMaxLength - 1);
        }
    }

    private void schedule(String dbmsJobName, StepContribution contribution) throws SQLException {
        String sql = buildStatement(dbmsJobName);
        log.debug("Executing statement: '{}'", sql);

        // execute statement
        try (Connection connection = dataSource.getConnection(); Statement stmt = connection.createStatement()) {
            boolean hasResult = stmt.execute(sql);
        }
        log.debug("Statement executed successfully");
        contribution.incrementWriteCount(1);


    }

    /**
     * Make an attempt to stop the scheduled job cleanly by sending a stop
     * request to the DBMS_SCHEDULER.
     *
     * @param dbmsJobName The job name
     */
    private void stopScheduledJob(String dbmsJobName) {
        String sql = buildStopStatement(dbmsJobName);
        log.debug("Executing stop statement {}", sql);
        // execute statement
        try (Connection connection = dataSource.getConnection(); Statement stmt = connection.createStatement()) {
            boolean hasResult = stmt.execute(sql);
            log.debug("Stop statement executed successfully");
        } catch (Exception e) {
            log.warn("Failed to stop job", e);
        }
    }

    /**
     * Checks if the given job is running.
     *
     * @return True if job is still running.
     * @throws SQLException
     */
    private boolean checkRunning(String dbmsJobName, StepContribution contribution, Map outParams) throws SQLException {
        String checkIfRunningSql = "select JOB_NAME, SESSION_ID, SLAVE_PROCESS_ID from USER_SCHEDULER_RUNNING_JOBS where job_name=?";
        try (Connection connection = dataSource.getConnection(); PreparedStatement stmt = connection.prepareStatement(checkIfRunningSql)) {
            int active  = ((BasicDataSource)dataSource).getNumActive();
            int idle  = ((BasicDataSource)dataSource).getNumIdle();
            log.debug("active: {}, idle: {}", active, idle);
            log.debug("Check if running with query '{}'", checkIfRunningSql);
            stmt.setString(1, dbmsJobName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String jobName = rs.getString(1);
                    long sessionId = rs.getLong(2);
                    long slaveProcessId = rs.getLong(3);
                    log.debug("checkRunning result: jobname: {},sessionId: {},slaveProcessId: {}", jobName, sessionId, slaveProcessId);
                    outParams.put("sessionId", sessionId);
                    outParams.put("slaveProcessId", slaveProcessId);
                    contribution.incrementReadCount();
                    return true;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to checkIfRunning", e);
        }
        return false;
    }

    private void sleep(long sleepMs) {
        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException ex) {
            log.warn("Unexpected interruptedException", ex);
        }
    }

    /**
     * When a scheduled job has finished (successfully or not), a row is written
     * to the table user_scheduler_job_run_details. This method returns the value in
     * column STATUS if it has any of the values "SUCCEEDED", "STOPPED".
     * Returns null if the job has not finished.
     * Throws JobExecutionException if the status value is "FAILED"
     */
    private String checkStatus(String dbmsJobName, StepContribution contribution) throws JobExecutionException {
        String status = null;
        long oraError = 0;
        String additionalInfo = null;
        // Check if the job has ended
        final String checkSuccessSql = "select JOB_NAME,STATUS,ERROR#,ADDITIONAL_INFO from user_scheduler_job_run_details where job_name=?";

        try (Connection connection = dataSource.getConnection(); PreparedStatement stmt = connection.prepareStatement(checkSuccessSql)) {
            log.debug("Checking status: '{}'", checkSuccessSql);
            int active  = ((BasicDataSource)dataSource).getNumActive();
            int idle  = ((BasicDataSource)dataSource).getNumIdle();
            log.debug("active: {}, idle: {}", active, idle);

            stmt.setString(1, dbmsJobName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String jobName = rs.getString(1);
                    status = rs.getString(2);
                    oraError = rs.getLong(3);
                    additionalInfo = rs.getString(4);
                    log.debug("checkStatus result: jobName: {}, status: {}, oraError: {}, additionInfo: {}", jobName, status, oraError, additionalInfo);
                    if ("FAILED".equals(status)) {
                        throw new JobExecutionException("Job " + jobName + " failed: " + additionalInfo);
                    }
                    else if ("STOPPED".equals(status)) {
                        throw new JobExecutionException("Job " + jobName + " ended with status STOPPED: " + additionalInfo);
                    }
                    // Just assert that we only received one row
                    if (rs.next()) {
                        // table should only contain one row, since we assume that we are using a unique job name
                        log.warn("Additional row(s) found in user_scheduler_job_run_details. Expected only one row");
                    }
                } else {
                    log.debug("No result. Job is not finished.");
                }
            }
        } catch (JobExecutionException jee) {
            throw jee;
        } catch (Exception e) {
            log.warn("Unexpected error when checking status", e);
            throw new RuntimeException("Unexpected error when checking status", e);
        }
        return status;
    }

    private String buildStatement(String jobName) {
        /* Build a statement like this
        begin
            SYS.DBMS_SCHEDULER.CREATE_JOB(
            job_name=>'DUMMY_JOB123',
            job_type=>'PLSQL_BLOCK',
            job_action=>'begin DUMMY_PROCEDURE(''hello'',30);end;',
            enabled=>TRUE);
        end;
         */

        String lf = System.lineSeparator();
        //plsql.append("/").append(lf);
        String plsql = "begin " + lf +
                "SYS.DBMS_SCHEDULER.CREATE_JOB(" + lf +
                "job_name=>'" + jobName + "'," + lf +
                "job_type=>'PLSQL_BLOCK'," + lf +
                "job_action=>'" + action + "'," + lf +
                "enabled=>TRUE);" + lf +
                "end;" + lf;
        return plsql;
    }

    private String buildStopStatement(String jobName) {
        /* Build a statement like this
        BEGIN
            DBMS_SCHEDULER.STOP_JOB ( job_name=>'MY_JOB_123' , force=> true );
        END;
         */
        String lf = System.lineSeparator();
        String plsql = "begin " + lf +
                "SYS.DBMS_SCHEDULER.STOP_JOB(" + lf +
                "job_name=>'" + jobName + "'," + lf +
                "force=>TRUE);" + lf +
                "end;" + lf;
        return plsql;
    }
}

