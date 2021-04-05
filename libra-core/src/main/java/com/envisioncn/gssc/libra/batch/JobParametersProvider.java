package com.envisioncn.gssc.libra.batch;

import java.util.List;

public interface JobParametersProvider {
    List<String> getRequiredKeys();
    List<String> getOptionalKeys();
}
