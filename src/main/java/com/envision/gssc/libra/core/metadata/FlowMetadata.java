package com.envision.gssc.libra.core.metadata;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jonnas
 * @date 2021-03-31
 */
@Data
public class FlowMetadata {
    private String id;
    private List<StepMetadata> steps = new ArrayList<>();
    private SplitMetadata split;
    public void addStep(StepMetadata step) {
        steps.add(step);
        step.setFlow(this);
    }

    @Override
    public String toString() {
        return "FlowMetadata("
                + "split="+(split != null ? split.getId():"null")
                + ", steps="+ StringUtils.join(steps)
                + ")";
    }
}
