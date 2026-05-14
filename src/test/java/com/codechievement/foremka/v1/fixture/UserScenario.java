package com.codechievement.foremka.v1.fixture;

import com.codechievement.foremka.v1.api.TestScenario;

/**
 * Represents a fully-provisioned application user.
 */
public record UserScenario(String id, String username, String email, String role) implements TestScenario {}
