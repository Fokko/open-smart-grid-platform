/**
 * Copyright 2018 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.adapter.ws.clients;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.opensmartgridplatform.adapter.ws.domain.entities.NotificationWebServiceLookupKey;
import org.opensmartgridplatform.adapter.ws.domain.entities.WebServiceConfigurationData;
import org.opensmartgridplatform.adapter.ws.domain.repositories.WebServiceConfigurationDataRepository;
import org.opensmartgridplatform.shared.exceptionhandling.WebServiceSecurityException;
import org.opensmartgridplatform.shared.infra.ws.CircuitBreaker;
import org.opensmartgridplatform.shared.infra.ws.CircuitBreakerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.security.support.KeyStoreFactoryBean;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

public class DataBasedWebServiceTemplateFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataBasedWebServiceTemplateFactory.class);

    private static final String PROXY_SERVER = "proxy-server";
    private static final Map<NotificationWebServiceLookupKey, WebServiceTemplate> webServiceTemplates = new ConcurrentHashMap<>();

    private final WebServiceConfigurationDataRepository configRepository;
    private final WebServiceMessageFactory messageFactory;
    private final List<ClientInterceptor> fixedInterceptors = new ArrayList<>();

    /**
     * Web service template factory that creates web service templates based on
     * configuration from the database.
     *
     * @param configRepository
     *            repository with web service configuration data
     * @param messageFactory
     *            message factory to be used by the template
     * @param fixedInterceptors
     *            client interceptors that will always be configured, and do not
     *            depend on stored configuration
     */
    public DataBasedWebServiceTemplateFactory(final WebServiceConfigurationDataRepository configRepository,
            final WebServiceMessageFactory messageFactory, final List<ClientInterceptor> fixedInterceptors) {

        this.configRepository = Objects.requireNonNull(configRepository, "configRepository must not be null");
        this.messageFactory = Objects.requireNonNull(messageFactory, "messageFactory must not be null");
        if (fixedInterceptors != null) {
            this.fixedInterceptors.addAll(fixedInterceptors);
        }
    }

    public WebServiceTemplate getTemplate(final NotificationWebServiceLookupKey templateKey) {

        return webServiceTemplates.computeIfAbsent(templateKey, key -> {
            try {
                return this.createTemplate(key);
            } catch (@SuppressWarnings("squid:S1166") final WebServiceSecurityException ignored) {
                /*
                 * Suppressing squid:S1166: Exception handlers should preserve
                 * the original exceptions
                 *
                 * Details about the cause of the exception are logged at the
                 * location where ignored is created and thrown.
                 *
                 * Return null to deal with this like with any other reason the
                 * template is not available.
                 */
                return null;
            }
        });
    }

    private WebServiceTemplate createTemplate(final NotificationWebServiceLookupKey id)
            throws WebServiceSecurityException {

        final WebServiceConfigurationData config = this.configRepository.findOne(id);
        if (config == null) {
            LOGGER.warn("Unable to create template: no web service configuration data available for {}", id);
            return null;
        }
        return this.createTemplate(config);
    }

    private WebServiceTemplate createTemplate(final WebServiceConfigurationData config)
            throws WebServiceSecurityException {

        LOGGER.info("About to create web service template for {}", config.getId());
        final WebServiceTemplate template = new WebServiceTemplate(this.messageFactory);
        this.setTargetUri(template, config.getTargetUri());
        this.setMarshaller(template, config.getMarshallerContextPath());
        this.setInterceptors(template, config);
        this.setMessageSender(template, config);
        return template;
    }

    private void setTargetUri(final WebServiceTemplate template, final String targetUri) {
        template.setDefaultUri(targetUri);
        template.setCheckConnectionForFault(!targetUri.contains(PROXY_SERVER));
    }

    private void setMarshaller(final WebServiceTemplate template, final String contextPath) {
        final Jaxb2Marshaller marshaller = this.createMarshaller(contextPath);
        template.setMarshaller(marshaller);
        template.setUnmarshaller(marshaller);
    }

    private Jaxb2Marshaller createMarshaller(final String contextPath) {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath(contextPath);
        return marshaller;
    }

    private void setInterceptors(final WebServiceTemplate template, final WebServiceConfigurationData config) {
        final List<ClientInterceptor> interceptors = new ArrayList<>(this.fixedInterceptors);
        this.addCircuitBreakerInterceptor(interceptors, config);
        template.setInterceptors(interceptors.toArray(new ClientInterceptor[interceptors.size()]));
    }

    private void addCircuitBreakerInterceptor(final List<ClientInterceptor> interceptors,
            final WebServiceConfigurationData config) {

        if (config.isUseCircuitBreaker()) {
            final CircuitBreaker circuitBreaker = new CircuitBreaker.Builder()
                    .withThreshold(config.getCircuitBreakerThreshold())
                    .withInitialDuration(config.getCircuitBreakerDurationInitial())
                    .withMaximumDuration(config.getCircuitBreakerDurationMaximum())
                    .withMultiplier(config.getCircuitBreakerDurationMultiplier()).build();
            interceptors.add(new CircuitBreakerInterceptor(circuitBreaker));
        }
    }

    private void setMessageSender(final WebServiceTemplate template, final WebServiceConfigurationData config)
            throws WebServiceSecurityException {

        final HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        if (config.isUseKeyStore() || config.isUseTrustStore()) {
            clientBuilder.setSSLSocketFactory(this.createSslConnectionSocketFactory(config));
        }
        clientBuilder.setMaxConnPerRoute(config.getMaxConnectionsPerRoute());
        clientBuilder.setMaxConnTotal(config.getMaxConnectionsTotal());
        this.preventDuplicateHeaders(clientBuilder);
        this.setRequestConfig(clientBuilder, config);
        template.setMessageSender(new HttpComponentsMessageSender(clientBuilder.build()));
    }

    private LayeredConnectionSocketFactory createSslConnectionSocketFactory(final WebServiceConfigurationData config)
            throws WebServiceSecurityException {

        final SSLContextBuilder sslContextBuilder = SSLContexts.custom();
        this.loadKeyMaterial(sslContextBuilder, config);
        this.loadTrustMaterial(sslContextBuilder, config);
        try {
            return new SSLConnectionSocketFactory(sslContextBuilder.build(),
                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (final GeneralSecurityException e) {
            LOGGER.error("Exception creating SSL connection socket factory", e);
            throw new WebServiceSecurityException("Unable to build SSL context", e);
        }
    }

    private void loadKeyMaterial(final SSLContextBuilder sslContextBuilder, final WebServiceConfigurationData config)
            throws WebServiceSecurityException {

        if (!config.isUseKeyStore()) {
            return;
        }
        try {
            sslContextBuilder.loadKeyMaterial(this.createKeyStore(config), config.getKeyStorePassword().toCharArray());
        } catch (final GeneralSecurityException e) {
            LOGGER.error("Exception loading key material", e);
            throw new WebServiceSecurityException("Unable to load key material for created KeyStore", e);
        }
    }

    private KeyStore createKeyStore(final WebServiceConfigurationData config) throws WebServiceSecurityException {
        final KeyStoreFactoryBean keyStoreFactory = new KeyStoreFactoryBean();
        keyStoreFactory.setType(config.getKeyStoreType());
        keyStoreFactory.setLocation(new FileSystemResource(config.getKeyStoreLocation()));
        keyStoreFactory.setPassword(config.getKeyStorePassword());
        try {
            keyStoreFactory.afterPropertiesSet();
            final KeyStore keyStore = keyStoreFactory.getObject();
            if ((keyStore == null) || (keyStore.size() == 0)) {
                throw new KeyStoreException("Key store is empty");
            }
            return keyStore;
        } catch (final GeneralSecurityException | IOException e) {
            LOGGER.error("Exception creating key store", e);
            throw new WebServiceSecurityException("Unable to create KeyStore", e);
        }
    }

    private void loadTrustMaterial(final SSLContextBuilder sslContextBuilder, final WebServiceConfigurationData config)
            throws WebServiceSecurityException {

        if (!config.isUseTrustStore()) {
            return;
        }
        try {
            sslContextBuilder.loadTrustMaterial(this.createTrustStore(config), new TrustSelfSignedStrategy());
        } catch (final GeneralSecurityException e) {
            LOGGER.error("Exception loading trust material", e);
            throw new WebServiceSecurityException("Unable to load trust material for created trust store", e);
        }
    }

    private KeyStore createTrustStore(final WebServiceConfigurationData config) {
        final KeyStoreFactoryBean trustStoreFactory = new KeyStoreFactoryBean();
        trustStoreFactory.setType(config.getTrustStoreType());
        trustStoreFactory.setLocation(new FileSystemResource(config.getTrustStoreLocation()));
        trustStoreFactory.setPassword(config.getTrustStorePassword());
        return trustStoreFactory.getObject();
    }

    private void preventDuplicateHeaders(final HttpClientBuilder clientBuilder) {
        // Add intercepter to prevent issue with duplicate headers.
        // See also:
        // http://forum.spring.io/forum/spring-projects/web-services/118857-spring-ws-2-1-4-0-httpclient-proxy-content-length-header-already-present
        clientBuilder.addInterceptorFirst(new HttpComponentsMessageSender.RemoveSoapHeadersInterceptor());
    }

    private void setRequestConfig(final HttpClientBuilder clientBuilder, final WebServiceConfigurationData config) {
        final RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(config.getConnectionTimeout())
                .build();
        clientBuilder.setDefaultRequestConfig(requestConfig);
    }

}
