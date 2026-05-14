package com.codechievement.foremka.v1.fixture;

import com.codechievement.foremka.v1.api.TestScenario;

/**
 * Represents a fully-provisioned product in an e-commerce catalogue.
 */
public record ProductScenario(String id, String name, String category, int basePrice, int discountedPrice)
        implements TestScenario {}
