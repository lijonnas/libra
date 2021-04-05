package com.envisioncn.gssc.libra.batch.tasklets;


import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tasklet that generates reports. Delegates to one or many {@link ReportGenerator}.
 * @author zhongshuangli
 * @date 2021-03-31
 */
@Slf4j
public class ReportTasklet implements Tasklet {
    @Value("${libra.homeDir}/reports")
    protected String reportDir;

    /**
     * True if you want the step to fail if one or more validations fail.
     */
    @Setter
    protected boolean failIfValidationFailures = true;

    private List<ReportGenerator> reportGenerators = new ArrayList<>();

    protected void addReportGenerator(ReportGenerator reportGenerator) {
        this.reportGenerators.add(reportGenerator);
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        List<String> fileNames = new ArrayList<>();
        List<String> urls = new ArrayList<>();
        List<ReportGenerationResult> failedResults = new ArrayList<>();

        for (ReportGenerator reportGenerator : reportGenerators) {
            String fileName = reportGenerator.getReportFilename();
            File reportFile = ReportUtils.generateReportFile(reportDir
                    , chunkContext.getStepContext().getStepExecution(), fileName);
            FileUtils.forceMkdirParent(reportFile);
            ReportGenerationResult result = reportGenerator.generateReport(reportFile, stepContribution, chunkContext);

            if (result.getStatus() == ReportGenerationResult.ReportGenerationResultStatus.Failed) {
                failedResults.add(result);
            }

            fileNames.add(fileName);
            urls.add(ReportUtils.generateReportUrl(chunkContext.getStepContext().getStepExecution(), fileName));
        }

        chunkContext.getStepContext().getStepExecution().getExecutionContext().put("fileNames", fileNames);
        chunkContext.getStepContext().getStepExecution().getExecutionContext().put("reportUrls", urls);

        if (this.failIfValidationFailures && failedResults.size() > 0) {
            // Generate a message for the failure exception
            StringBuilder sb = new StringBuilder();
            for (ReportGenerationResult rgr : failedResults) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(rgr.getMessage());
            }
            throw new JobExecutionException(sb.toString());
        }

        return RepeatStatus.FINISHED;
    }
}
