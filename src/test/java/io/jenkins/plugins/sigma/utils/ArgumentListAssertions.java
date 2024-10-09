/*
 * Copyright (c) 2024 Black Duck Software, Inc. All rights reserved worldwide.
 */
package io.jenkins.plugins.sigma.utils;

import static org.junit.Assert.assertEquals;

import hudson.util.ArgumentListBuilder;
import java.util.Arrays;
import java.util.List;

public class ArgumentListAssertions {

    public static void assertArgumentList(ArgumentListBuilder actual, String... expected) {
        List<String> expectedArgumentList = Arrays.asList(expected);
        List<String> actualArgumentList = actual.toList();
        assertEquals(expectedArgumentList, actualArgumentList);
    }
}
