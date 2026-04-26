package com.codechievement.foremka.v1.internal;

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;

import java.lang.StackWalker.StackFrame;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Stream;
import lombok.SneakyThrows;

/**
 * Utility that inspects the current thread's call stack to discover the name of the JUnit test
 * method that triggered a scenario access.
 *
 * <p>Detection works by looking for methods annotated with a known JUnit test annotation.
 * All JUnit annotation lookups are performed via reflection so that this class does not require
 * a compile-time dependency on JUnit.
 */
public final class TestNameDetector {

    private static final Set<String> ANNOTATION_CLASSES = Set.of(
            "org.junit.Test",
            "org.junit.jupiter.api.Test",
            "org.junit.jupiter.params.ParameterizedTest",
            "org.junit.jupiter.api.RepeatedTest",
            "org.junit.jupiter.api.BeforeAll",
            "org.junit.jupiter.api.BeforeEach");

    public static String detectCurrentTestName() {
        var testFrame = StackWalker.getInstance(RETAIN_CLASS_REFERENCE).walk(TestNameDetector::findTestFrame);
        return testFrame.getDeclaringClass().getSimpleName() + "." + testFrame.getMethodName();
    }

    static StackFrame findTestFrame(Stream<StackFrame> frames) {
        return frames.filter(TestNameDetector::isTestMethod)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No test method found in call stack"));
    }

    @SneakyThrows
    private static boolean isTestMethod(StackFrame frame) {
        Class<?> clazz = frame.getDeclaringClass();
        Method method = clazz.getDeclaredMethod(
                frame.getMethodName(), frame.getMethodType().parameterArray());
        for (Annotation annotation : method.getAnnotations()) {
            if (ANNOTATION_CLASSES.contains(annotation.annotationType().getName())) {
                return true;
            }
        }
        return false;
    }
}
