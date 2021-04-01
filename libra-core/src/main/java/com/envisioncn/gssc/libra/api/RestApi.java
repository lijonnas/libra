package com.envisioncn.gssc.libra.api;

import com.envisioncn.gssc.libra.scheduling.JobTrigger;
import com.envisioncn.gssc.libra.scheduling.JobTriggerDao;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author zhongshuangli
 * @date 2021-04-01
 */
@RestController()
@RequestMapping("api")
public class RestApi {
    @Autowired
    JobTriggerDao triggerDao;
    Logger log = LoggerFactory.getLogger(this.getClass());

    public RestApi() {
        log.info("Rest API init");
    }

    @RequestMapping("ping")
    public String ping() {
        return "ping";
    }

    @RequestMapping("triggers")
    public String triggers() {
        List<JobTrigger> triggers= triggerDao.loadTriggers();
        String jobNamesStr = StringUtils.join(triggers);

        return jobNamesStr;
    }
}

