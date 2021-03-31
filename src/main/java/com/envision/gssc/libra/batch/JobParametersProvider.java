package com.envision.gssc.libra.batch;

import java.util.List;

public interface JobParametersProvider {
    public List<String> getRequiredKeys();
    public List<String> getOptionalKeys();
}
