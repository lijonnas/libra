package com.envisioncn.gssc.libra.core;

import org.junit.BeforeClass;

/**
 * @author zhongshuangli
 * @date 2021-04-01
 */
public class LibraManagerIntergationTest {
    private static LibraConfig libraConfig;

    @BeforeClass
    public static void initalizeGreetingConfig() {
        libraConfig = new LibraConfig();
        //greetingConfig.put(LibraConfigParams.USER_NAME, "World");
        //greetingConfig.put(LibraConfigParams.MORNING_MESSAGE, "Good Morning");
        //greetingConfig.put(LibraConfigParams.AFTERNOON_MESSAGE, "Good Afternoon");
        //greetingConfig.put(LibraConfigParams.EVENING_MESSAGE, "Good Evening");
        //greetingConfig.put(LibraConfigParams.NIGHT_MESSAGE, "Good Night");
    }
/*
    @Test
    public void givenMorningTime_ifMorningMessage_thenSuccess() {
        String expected = "Hello World, Good Morning";
        LibraManager LibraManager = new LibraManager(greetingConfig);
        String actual = LibraManager.greet(LocalDateTime.of(2017, 3, 1, 6, 0));
        assertEquals(expected, actual);
    }

    @Test
    public void givenAfternoonTime_ifAfternoonMessage_thenSuccess() {
        String expected = "Hello World, Good Afternoon";
        LibraManager LibraManager = new LibraManager(greetingConfig);
        String actual = LibraManager.greet(LocalDateTime.of(2017, 3, 1, 13, 0));
        assertEquals(expected, actual);
    }

    @Test
    public void givenEveningTime_ifEveningMessage_thenSuccess() {
        String expected = "Hello World, Good Evening";
        LibraManager LibraManager = new LibraManager(greetingConfig);
        String actual = LibraManager.greet(LocalDateTime.of(2017, 3, 1, 19, 0));
        assertEquals(expected, actual);
    }

    @Test
    public void givenNightTime_ifNightMessage_thenSuccess() {
        String expected = "Hello World, Good Night";
        LibraManager LibraManager = new LibraManager(greetingConfig);
        String actual = LibraManager.greet(LocalDateTime.of(2017, 3, 1, 21, 0));
        assertEquals(expected, actual);
    }
    */
}
