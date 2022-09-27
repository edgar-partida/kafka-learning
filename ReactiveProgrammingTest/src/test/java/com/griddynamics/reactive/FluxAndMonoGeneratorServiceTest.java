package com.griddynamics.reactive;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class FluxAndMonoGeneratorServiceTest {

    FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

    @Test
    void namesFlux() {
        StepVerifier.FirstStep<String> steps = StepVerifier.create(fluxAndMonoGeneratorService.namesFlux());
        steps.expectNext("alex","ben", "chloe").verifyComplete();
        StepVerifier.create(fluxAndMonoGeneratorService.upperNames()).expectNext("ALEX","BEN", "CHLOE").verifyComplete();
        steps.expectNextCount(3).verifyComplete();
    }

}