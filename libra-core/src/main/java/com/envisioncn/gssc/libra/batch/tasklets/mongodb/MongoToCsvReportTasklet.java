package com.envisioncn.gssc.libra.batch.tasklets.mongodb;

import com.envisioncn.gssc.libra.batch.tasklets.ReportGenerationResult;
import com.envisioncn.gssc.libra.batch.tasklets.ReportGenerator;
import com.envisioncn.gssc.libra.batch.tasklets.ReportTasklet;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Meta;
import org.springframework.data.mongodb.core.query.Query;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.time.Duration;

/**
 * @author zhongshuangli
 * @date 2021-04-01
 */
@Slf4j
@Getter
@Setter
public class MongoToCsvReportTasklet extends ReportTasklet implements ReportGenerator, InitializingBean {
    private String collectionName;
    private String jsonQuery;
    private String reportName;
    private String reportFilename;

    @Autowired
    private MongoTemplate mongoTemplate;

    // Csv settings
    private char delimiter = ',';
    private String encoding = "UTF-8";
    /**
     * Generate a header
     */
    private boolean generateHeader = true;
    private long queryTimeout;
    private int cursorBatchSize;

    public MongoToCsvReportTasklet() {
        addReportGenerator(this);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (reportFilename == null) {
            throw new Exception("reportFilename must not be null");
        }
    }

    @Override
    public ReportGenerationResult generateReport(File reportFile, StepContribution sc, ChunkContext cc)
            throws Exception {
        log.info("Exporting to file. {}", reportFile);
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(delimiter);
        MutableBoolean first = new MutableBoolean(true);

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(reportFile),
                Charset.forName(encoding).newEncoder()); CSVPrinter csvPrinter = new CSVPrinter(writer, format)) {
            Query query = new BasicQuery(jsonQuery);
            Meta meta = new Meta();
            meta.setMaxTime(Duration.ofMillis(queryTimeout));
            meta.setCursorBatchSize(cursorBatchSize);
            query.setMeta(meta);
            mongoTemplate.executeQuery(query, collectionName, document -> {
                try {
                    if (first.isTrue()) {
                        if (generateHeader) {
                            csvPrinter.printRecord(document.keySet());
                        }
                        first.setFalse();
                    }
                    csvPrinter.printRecord(document.values());
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ReportGenerationResult.OK;
    }
}

