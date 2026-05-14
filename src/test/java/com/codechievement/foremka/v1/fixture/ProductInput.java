package com.codechievement.foremka.v1.fixture;

/**
 * Cache key for {@link ProductScenario}.
 *
 * <p>All three fields together identify a unique product configuration. Two inputs are considered
 * the same only when every field matches, which is why using a record as the input type is
 * recommended — records provide stable {@code equals} and {@code hashCode} for free.
 */
public record ProductInput(String name, String category, int basePrice) {}

