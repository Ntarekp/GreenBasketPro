package com.greenbasket.api.config;

import org.hibernate.dialect.PostgreSQLDialect;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig extends PostgreSQLDialect {
    public HibernateConfig() {
        super();
    }
} 