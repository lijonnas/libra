package com.envisioncn.gssc.libra.scheduling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * @author jonnas
 * @date 2021-03-31
 */
public class JobTriggerDao implements RowMapper<JobTrigger> {
    @Autowired
    DataSource dataSource;

    @Transactional
    public void saveOrUpdate(JobTrigger [] triggers) {
        for (JobTrigger trigger : triggers) {
            saveOrUpdate(trigger);
        }
    }

    @Transactional
    public void saveOrUpdate(JobTrigger trigger) {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        if (trigger.getId() == null) {
            template.update("insert into SCHEDULING_TRIGGER (JOB_NAME,TRIGGER_TYPE,TRIGGERING_JOB_NAME,CRON,JOB_PARAMETERS) values(?,?,?,?,?)", trigger.getJobName(), trigger.getTriggerType().toString(), trigger.getTriggeringJobName(), trigger.getCron(), trigger.getJobParameters());
        } else {
            template.update("update SCHEDULING_TRIGGER set JOB_NAME=?,TRIGGER_TYPE=?,TRIGGERING_JOB_NAME=?,CRON=?,JOB_PARAMETERS=? where ID=?", trigger.getJobName(), trigger.getTriggerType().toString(), trigger.getTriggeringJobName(), trigger.getCron(), trigger.getJobParameters(), trigger.getId());
        }
    }

    public void delete(JobTrigger jobTrigger) {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        template.update("delete from SCHEDULING_TRIGGER where ID=?", jobTrigger.getId());
    }
    public void deleteAll() {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        template.update("delete from SCHEDULING_TRIGGER");
    }


    public List<JobTrigger> loadTriggers() {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        String sql = "select ID,JOB_NAME, TRIGGER_TYPE,TRIGGERING_JOB_NAME, CRON, JOB_PARAMETERS from SCHEDULING_TRIGGER";
        List<JobTrigger> out = template.query(sql, this);
        return out;
    }

    @Override
    public JobTrigger mapRow(ResultSet rs, int rowNum) throws SQLException {
        JobTrigger jt = new JobTrigger();
        jt.setId(rs.getLong("ID"));
        jt.setJobName(rs.getString("JOB_NAME"));
        jt.setCron(rs.getString("CRON"));
        jt.setTriggeringJobName(rs.getString("TRIGGERING_JOB_NAME"));
        jt.setTriggerType(JobTrigger.TriggerType.valueOf(rs.getString("TRIGGER_TYPE")));
        jt.setJobParameters(rs.getString("JOB_PARAMETERS"));
        return jt;
    }

    public List<JobTrigger> getJobCompletionTriggersFor(String completedJobName) {
        String args[] = {completedJobName};
        String sql = "select ID,JOB_NAME, TRIGGER_TYPE,TRIGGERING_JOB_NAME, CRON, JOB_PARAMETERS from SCHEDULING_TRIGGER  where TRIGGERING_JOB_NAME=?";
        JdbcTemplate template = new JdbcTemplate(dataSource);
        List<JobTrigger> out = template.query(sql, this,args);
        return out;
    }

    public void logSuccess(JobTrigger jt) {
        log(jt, "SUCCESS", null);
    }

    public void logFailure(JobTrigger jt, String errorMsg) {
        log(jt, "FAILED", errorMsg);
    }

    public void log(JobTrigger jt, String status, String errorMsg) {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        Date timestamp = new Date();
        template.update("insert into SCHEDULING_LOG (TSTAMP, STATUS, JOB_NAME, TRIGGER_TYPE, TRIGGERING_JOB_NAME, CRON, ERROR_MSG) values(?,?,?,?,?,?,?)",
                timestamp, status, jt.getJobName(), jt.getTriggerType().toString(), jt.getTriggeringJobName(), jt.getCron(), errorMsg);
    }

    public List<JobTriggerLog> loadLog(int maxResults) {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        RowMapper<JobTriggerLog> rowMapper = (rs, rowNum) -> {
            JobTriggerLog l = new JobTriggerLog();
            l.setTstamp(rs.getTimestamp("TSTAMP"));
            l.setCron(rs.getString("CRON"));
            l.setErrorMsg(rs.getString("ERROR_MSG"));
            l.setJobName(rs.getString("JOB_NAME"));
            l.setStatus(rs.getString("STATUS"));
            l.setTriggeringJobName(rs.getString("TRIGGERING_JOB_NAME"));
            l.setTriggerType(JobTrigger.TriggerType.valueOf(rs.getString("TRIGGER_TYPE")));
            return l;
        };
        List<JobTriggerLog> out = template.query("select TSTAMP, STATUS, JOB_NAME, TRIGGER_TYPE, TRIGGERING_JOB_NAME, CRON, ERROR_MSG from SCHEDULING_LOG  order by TSTAMP desc limit " + maxResults, rowMapper);
        return out;
    }

}
