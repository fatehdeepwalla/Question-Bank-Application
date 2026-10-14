package org.example.questionBankApplication.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class ImageWebConfig implements WebMvcConfigurer {

    private final String imageLocation;

    public ImageWebConfig(@Value("${question-bank.images-directory") String imagesDirectory) {
        Path path = Path.of(imagesDirectory);
        String uri = path.toUri().toString();
        this.imageLocation = uri.endsWith("/") ? uri : uri + "/";
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/question-images/**")
                .addResourceLocations(imageLocation);
    }
}

