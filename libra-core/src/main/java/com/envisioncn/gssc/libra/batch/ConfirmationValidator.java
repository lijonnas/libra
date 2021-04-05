package com.envisioncn.gssc.libra.batch;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author jonnas
 * @date 2021-03-31
 */
public class ConfirmationValidator implements JobParametersValidator, JobParametersProvider {
    private final List<String> requiredKeys = Collections.singletonList("confirm");

    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        assert parameters != null;
        String confirm = parameters.getString("confirm");
        if (!"yes".equalsIgnoreCase(confirm)) {
            throw new JobParametersInvalidException("You must confirm with a 'yes'!");
        }
    }

    @Override
    public List<String> getRequiredKeys() {
        return requiredKeys;
    }

    @Override
    public List<String> getOptionalKeys() {
        return Collections.EMPTY_LIST;
    }
}
