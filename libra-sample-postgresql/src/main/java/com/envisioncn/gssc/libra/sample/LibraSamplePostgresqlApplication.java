package com.envisioncn.gssc.libra.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author zhongshuangli
 * @date 2021-04-05
 */
@PropertySource("git.properties")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class, BatchAutoConfiguration.class, ErrorMvcAutoConfiguration.class})
public class LibraSamplePostgresqlApplication {
    public static void main(String[] args) {
        SpringApplication.run(LibraSamplePostgresqlApplication.class, args);
    }
}
