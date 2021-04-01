package com.envisioncn.gssc.libra.core;

import lombok.Data;

/**
 * @author jonnas
 * @date 2021-03-31
 */
@Data
public class ReadWriteInfo {
    private int readCount;
    private int writeCount;
    private int commitCount;
    private int rollbackCount;
    private int readSkipCount;
    private int processSkipCount;
    private int writeSkipCount;
    private int filterCount;

    public String toRWSString() {
        return "" + readCount + "/" + writeCount + "/" + (readSkipCount + writeSkipCount + processSkipCount);
    }
}
