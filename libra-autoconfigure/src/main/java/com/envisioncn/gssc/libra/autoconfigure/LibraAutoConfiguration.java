package com.envisioncn.gssc.libra.autoconfigure;

import com.envisioncn.gssc.libra.core.LibraConfig;
import com.envisioncn.gssc.libra.core.LibraManager;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.ConfigurableEnvironment;

import static com.envisioncn.gssc.libra.core.LibraConfigParams.*;

/**
 * @author jonnas
 * @date 2021-04-01
 */
@Configuration
@ConditionalOnClass(LibraManager.class)
@ComponentScan(basePackages = {"com.envisioncn.gssc.libra.autoconfigure"
        ,"com.envisioncn.gssc.libra.config"
        ,"com.envisioncn.gssc.libra.core.metadata"
        ,"com.envisioncn.gssc.libra.cmd"
        ,"com.envisioncn.gssc.libra.api"
        ,"com.envisioncn.gssc.libra.scheduling"})
@EnableConfigurationProperties(LibraProperties.class)
public class LibraAutoConfiguration implements EnvironmentPostProcessor {
    @Autowired
    private LibraProperties libraProperties;

    @Bean
    @ConditionalOnMissingBean
    public LibraConfig libraConfig() {
        String homeDir = libraProperties.getHomeDir() == null ? System.getProperty("bauta.homeDir") : libraProperties.getHomeDir();
        String jobBeansDir = libraProperties.getJobBeansDir() == null ? System.getProperty("bauta.jobBeansDir") : libraProperties.getJobBeansDir();
        String reportDir = libraProperties.getJobBeansDir() == null ? System.getProperty("bauta.reportDir") : libraProperties.getReportDir();

        LibraConfig libraConfig = new LibraConfig();
        libraConfig.put(HOME_DIR, homeDir);
        libraConfig.put(JOB_BEANS_DIR, jobBeansDir);
        libraConfig.put(REPORT_DIR, reportDir);
        return libraConfig;
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("dataSourceInitializer")
    public LibraManager libraManager(LibraConfig libraConfig, JobOperator jobOperator, JobRepository jobRepository, JobExplorer jobExplorer, JobRegistry jobRegistry) {
        return new LibraManager(libraConfig, jobOperator, jobRepository, jobExplorer, jobRegistry);
    }

    /**
     * Gives us a chance to customize the Environment or Application at an early stage
     * @param environment
     * @param application
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        /**
         * Avoiding problems with autoconfiguration of spring batch.
         * We want to create our own data sources.
         */
        environment.getSystemProperties().put("spring.autoconfigure.exclude", "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration");

        //application.setAllowBeanDefinitionOverriding(true);
    }
}
