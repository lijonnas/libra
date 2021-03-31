package com.envision.gssc.libra.batch.tasklets;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;

/**
 * Deletes the given file. Exits silently if the file does not exist.
 * @author zhongshuangli
 * @date 2021-03-31
 */
public class DeleteFileTasklet implements Tasklet {
    private static final Logger log = LoggerFactory.getLogger(DeleteFileTasklet.class);
    Resource resource = null;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try {
            File dirFile = resource.getFile();
            if (dirFile.exists()) {
                FileUtils.deleteQuietly(dirFile);
            }
        } catch (IOException ex) {
            throw new JobExecutionException("Failed to delete file: " + resource.toString(), ex);
        }
        return RepeatStatus.FINISHED;
    }

    /**
     * @param resource The file to delete.
     */
    public void setResource(Resource resource) {
        this.resource = resource;
    }

}

