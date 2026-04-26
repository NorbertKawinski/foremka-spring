package com.codechievement.foremka.v1.internal;

import static com.codechievement.foremka.v1.internal.TestNameDetector.detectCurrentTestName;
import static com.codechievement.foremka.v1.internal.TestNameDetector.findTestFrame;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;

class TestNameDetectorTest {

    @BeforeAll
    static void detectBeforeAll() {
        assertThat(detectCurrentTestName(), equalTo("TestNameDetectorTest.detectBeforeAll"));
    }

    @BeforeEach
    void detectBeforeEach() {
        assertThat(detectCurrentTestName(), equalTo("TestNameDetectorTest.detectBeforeEach"));
    }

    @Test
    void detectTestMethod() {
        assertThat(detectCurrentTestName(), equalTo("TestNameDetectorTest.detectTestMethod"));
    }

    @ParameterizedTest
    @NullSource
    void detectParameterizedTest(Object ignored) {
        assertThat(detectCurrentTestName(), equalTo("TestNameDetectorTest.detectParameterizedTest"));
    }

    @RepeatedTest(1)
    void detectRepeatedTest() {
        assertThat(detectCurrentTestName(), equalTo("TestNameDetectorTest.detectRepeatedTest"));
    }

    @TestFactory
    @Disabled("Too complex to support for now given lack of use cases. Will re-consider in future if needed")
    Iterable<DynamicTest> detectTestFactory() {
        return List.of(dynamicTest(
                "dynamicDetectTestFactory",
                () -> assertThat(detectCurrentTestName(), equalTo("TestNameDetectorTest.detectTestFactory"))));
    }

    @Test
    void detectNonTestMethod() {
        assertThrows(
                IllegalStateException.class, () -> findTestFrame(Stream.of()), "No test method found in call stack");
    }
}
