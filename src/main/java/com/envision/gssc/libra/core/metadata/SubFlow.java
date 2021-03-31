package com.envision.gssc.libra.core.metadata;

import lombok.Data;

/**
 * @author jonnas
 * @date 2021-03-31
 */
@Data
public abstract class SubFlow {
    protected  String id;
    protected String nextId;
}
