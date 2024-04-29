package com.sagar.gcp.gcpcloudstorage.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.cloud.spring.secretmanager.SecretManagerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;


@Configuration
public class SecretConfig {

    private static SecretConfig instance;

    public SecretConfig() {
        instance = this;
    }

    public static SecretConfig getInstance() {
        if (null == instance) {
            instance = new SecretConfig();
        }
        return instance;
    }

    @Autowired
    SecretManagerTemplate secretManagerTemplate;

    @Autowired
    Environment env;
    private Map<String, String> preCache;

    @PostConstruct
    private void init() throws JsonProcessingException {
        fetch();
    }

    private void fetch() throws JsonProcessingException {
        String cval = secretManagerTemplate.getSecretString("repo-secret-dev");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());

        Map<String, String> mtype = new HashMap<>();
        preCache = objectMapper.readValue(cval, mtype.getClass());
    }

    public String get(String key) {
        if (preCache.get(key) != null) {
            return preCache.get(key);
        }
        return env.getProperty(key);
    }
}


