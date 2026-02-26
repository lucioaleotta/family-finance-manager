package com.lucio.financeapp.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.lucio.financeapp.users.infrastructure.security.JwtProperties;

@Configuration
@EnableConfigurationProperties({ FinanceProperties.class, JwtProperties.class })
public class Config {}
