package com.codechievement.foremka.v1.internal;

import com.codechievement.foremka.v1.api.TestScenarioWithMeta;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestScenariosMap extends ConcurrentHashMap<ScenarioInputWithMeta, TestScenarioWithMeta> {}
