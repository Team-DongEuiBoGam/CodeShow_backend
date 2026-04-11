package org.example.animation;

import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class LanguageInitializer implements CommandLineRunner {

    private final LanguageRepository languageRepository;

    public LanguageInitializer(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    @Override
    public void run(String... args) {
        if (languageRepository.count() > 0) {
            return;
        }

        languageRepository.saveAll(List.of(
                new Language("Java"),
                new Language("Python"),
                new Language("JavaScript"),
                new Language("C++")
        ));
    }
}
