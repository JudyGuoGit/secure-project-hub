package com.judy.secureprojecthub.config;

import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

/**
 * Supports custom scalars used by the split schema files:
 * scalar Long
 * scalar DateTime
 * scalar JSON
 *
 * Add this Maven dependency if not already present:
 * <dependency>
 *   <groupId>com.graphql-java</groupId>
 *   <artifactId>graphql-java-extended-scalars</artifactId>
 * </dependency>
 */
@Configuration
public class GraphQLScalarConfig {

    @Bean
    RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(ExtendedScalars.GraphQLLong)
                .scalar(ExtendedScalars.DateTime)
                .scalar(ExtendedScalars.Json);
    }
}
