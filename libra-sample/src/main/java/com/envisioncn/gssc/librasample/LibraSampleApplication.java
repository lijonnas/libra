package com.envisioncn.gssc.librasample;

import com.envisioncn.gssc.libra.core.LibraManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

@EnableAutoConfiguration
public class LibraSampleApplication {
    @Autowired
    private LibraManager libraManager;

    public static void main(String[] args) {
        SpringApplication.run(LibraSampleApplication.class, args);
    }

    public void run(String... args) throws Exception {
    }
}
