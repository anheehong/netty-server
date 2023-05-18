/*
 * Project automation-gw
 *
 * Copyright (c) RNR Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are strictly prohibited without permission.
 */

package com.netty.server.config

import com.netty.server.netty.NetworkServer
import okhttp3.OkHttpClient
import org.jasypt.encryption.StringEncryptor
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import javax.annotation.PreDestroy


/**
 *
 * @author anakin
 * @date 2022/10/19
 */
@Configuration
class BaseConfig {
    @Autowired
    private lateinit var networkServer: NetworkServer

    @Autowired
    private lateinit var nettyConf: NettyConf

    @Bean
    fun restTemplate() = RestTemplate(
            OkHttp3ClientHttpRequestFactory(
                OkHttpClient().newBuilder().followRedirects(false).build()).apply {
        setReadTimeout(10_000)
        setConnectTimeout(3_000)
    })

    /**
     * shutdown the server.
     */
    @PreDestroy
    fun stop() {
        networkServer.stop()
    }
}

@Configuration
class JasyptConfig {
    @Bean("jasyptStringEncryptor")
    fun stringEncryptor():StringEncryptor = StandardPBEStringEncryptor().apply {
        setPassword(JasyptConfig::class.qualifiedName)
    }
}
