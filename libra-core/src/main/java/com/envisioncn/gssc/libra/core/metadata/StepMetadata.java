package com.envisioncn.gssc.libra.core.metadata;

import lombok.Data;

/**
 * @author jonnas
 * @date 2021-03-31
 */
@Data
public class StepMetadata extends SubFlow {
    public static enum StepType {
        /* SQL Scripts */
        SQL,
        /* Scheduled stored procedure */
        SCH,
        /* Reader/writer (standard spring batch step) */
        RW,
        ASSERT,
        REP,
        OTHER
    }

    private StepType stepType = StepType.OTHER;

    private String description;
    private SplitMetadata split;
    private FlowMetadata flow;
    private boolean firstInSplit;
    private boolean lastInSplit;

    @Override
    public String toString() {
        return "StepMetadata("
                + "id="+id
                + ", stepType="+stepType.toString()
                +", description="+description
                +", next=" + nextId
                +", firstInSplit=" + firstInSplit
                +", lastInSplit=" + lastInSplit
                + ", split="+(split != null ? split.getId():"null")
                + ")";
    }

}
