package com.zd.updateservicedemo;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
//        assertEquals(4, 2 + 2);
        int CLINET_MSG_FIRST = 0x1;
        System.out.println(CLINET_MSG_FIRST);
        System.out.println(CLINET_MSG_FIRST << 1);
        System.out.println(CLINET_MSG_FIRST << 2);
        System.out.println(CLINET_MSG_FIRST << 3);
        System.out.println(CLINET_MSG_FIRST << 4);


    }
}