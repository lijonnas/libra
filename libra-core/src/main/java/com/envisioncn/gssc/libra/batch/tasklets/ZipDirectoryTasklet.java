package com.envisioncn.gssc.libra.batch.tasklets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Compresses the given directory into a zip-file.
 * @author zhongshuangli
 * @date 2021-04-01
 */
public class ZipDirectoryTasklet implements Tasklet {
    private static final Logger log = LoggerFactory.getLogger(ZipDirectoryTasklet.class);
    FileSystemResource sourceDir = null;
    FileSystemResource destFile = null;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        File sourceDirFile = sourceDir.getFile();
        if (!sourceDirFile.exists()) {
            log.warn("The directory to zip does not exist: {}", sourceDir.toString());
            return RepeatStatus.FINISHED;
        }
        pack(sourceDir, destFile);

        return RepeatStatus.FINISHED;
    }

    public static void pack(FileSystemResource sourceDirPath, FileSystemResource zipFilePath) throws IOException {
        Files.deleteIfExists(Paths.get(zipFilePath.getPath()));
        Path p = Files.createFile(Paths.get(zipFilePath.getPath()));

        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
            Path sourcePath = Paths.get(sourceDirPath.getPath());
            Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        String sp = path.getParent().getFileName().toString();
                        ZipEntry zipEntry = new ZipEntry(sp + "/" + path.getFileName().toString());
                        try {
                            zs.putNextEntry(zipEntry);
                            zs.write(Files.readAllBytes(path));
                            zs.closeEntry();
                        } catch (Exception e) {
                            System.err.println(e);
                        }
                    });
        }
    }

    /**
     * @param sourceDir The directory to be zipped
     */
    public void setSourceDir(FileSystemResource sourceDir) {
        this.sourceDir = sourceDir;
    }

    /**
     * @param destFile Destination zip file
     */
    public void setDestFile(FileSystemResource destFile) {
        this.destFile = destFile;
    }
}
