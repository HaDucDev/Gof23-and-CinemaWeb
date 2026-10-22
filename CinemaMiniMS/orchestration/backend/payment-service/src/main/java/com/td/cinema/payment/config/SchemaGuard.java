package com.td.cinema.payment.config;

import com.td.cinema.payment.repository.DatabaseRepository;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SchemaGuard implements ApplicationRunner {
    private final DatabaseRepository repository;
    private volatile boolean ready;

    public SchemaGuard(DatabaseRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        repository.validateSchema();
        ready = true;
    }

    public boolean ready() {
        return ready;
    }
}
