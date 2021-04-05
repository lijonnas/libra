package com.envisioncn.gssc.libra.core.metadata;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jonnas
 * @date 2021-03-31
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SplitMetadata extends SubFlow {
    private List<FlowMetadata> flows = new ArrayList<>();

    public void addFlow(FlowMetadata flow) {
        flows.add(flow);
    }

    public List<FlowMetadata> getFlows() {
        return this.flows;
    }
}
