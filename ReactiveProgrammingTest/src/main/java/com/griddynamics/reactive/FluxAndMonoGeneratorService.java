package com.griddynamics.reactive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class FluxAndMonoGeneratorService {



    public Flux<String> namesFlux() {
        return Flux.fromIterable(List.of("alex","ben","chloe")).log();
    }

    public Mono<String> nameMono() {
        return Mono.just("César");
    }

    public Flux<String> upperNames() {
        return Flux.fromIterable(List.of("alex","ben","chloe"))
                .map(String::toUpperCase).log();
    }

    public static void main(String[] args) {
        FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();
        fluxAndMonoGeneratorService.namesFlux().subscribe(name -> {
            System.out.println("Name is: " + name);
        });
        fluxAndMonoGeneratorService.nameMono().subscribe(name -> {
            System.out.println("Name in mono is: " + name);
        });
        fluxAndMonoGeneratorService.upperNames().subscribe(name -> {
            System.out.println("Name in upperCase: " + name);
        });
    }
}
