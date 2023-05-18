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
import com.netty.server.netty.ServerHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.DelimiterBasedFrameDecoder
import io.netty.handler.codec.Delimiters
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.SelfSignedCertificate
import io.netty.handler.timeout.IdleStateHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.convert.DurationUnit
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.validation.annotation.Validated
import java.security.cert.CertificateException
import java.time.Duration
import java.time.temporal.ChronoUnit
import javax.net.ssl.SSLException

@Configuration
class NetAppConfig {
    @Lazy
    @Autowired
    private lateinit var serverHandler: ServerHandler

    @Autowired
    private lateinit var nettyConf: NettyConf

    @Bean
    fun networkServer(serverBootstrap: ServerBootstrap): NetworkServer {
        return NetworkServer(serverBootstrap).apply { serverPort = nettyConf.tcpPort }
    }

    @Bean
    fun serverBootstrap(nettyConf: NettyConf): ServerBootstrap {
        return ServerBootstrap().channel(NioServerSocketChannel::class.java) // default server channel.
            .option(ChannelOption.SO_BACKLOG, nettyConf.backlog)
            .childOption(ChannelOption.SO_KEEPALIVE, nettyConf.keepAlive)
            .childOption(ChannelOption.TCP_NODELAY, nettyConf.tcpNoDelay)
            .group(NioEventLoopGroup(nettyConf.bossCount), NioEventLoopGroup(nettyConf.workerCount))
            .childHandler(channelHandler())
    }

    @Bean
    fun channelHandler(): ChannelHandler {
        return object : ChannelInitializer<SocketChannel>() {
            @Throws(Exception::class)
            override fun initChannel(channel: SocketChannel) {
                if (nettyConf.secure) {
                    channel.pipeline().addLast(sslContext().newHandler(channel.alloc()))
                }
                channel.pipeline().addLast(DelimiterBasedFrameDecoder(1024, *Delimiters.lineDelimiter()))
                    .addLast(StringDecoder())
                    .addLast(StringEncoder())
                    //.addLast(MessageDecoder())
                    .addLast("idleStateHandler", IdleStateHandler(nettyConf.idleTimeout.toSeconds().toInt(), 0, 0))
                    .addLast("handler", serverHandler)
            }
        }
    }

    /**
     * make a self-signed certificated ssl context.
     * @author anakin
     * @date 2019. 1. 10.
     * @return
     * @throws CertificateException
     * @throws SSLException
     */
    @Throws(CertificateException::class, SSLException::class)
    protected fun sslContext(): SslContext {
        val ssc = SelfSignedCertificate()
        return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build()
    }

}

/* configuration for netty bootstrap. */
@Configuration
@ConfigurationProperties(prefix = "netty", ignoreUnknownFields = true)
@Validated
data class NettyConf(var tcpPort: Int = PORT_DEFAULT) {
    var bossCount: Int = BOSS_COUNT_DEFAULT
    var workerCount: Int = WORKER_COUNT_DEFAULT
    var keepAlive: Boolean = KEEP_ALIVE_DEFAULT
    var backlog: Int = BACK_LOG_DEFAULT

    @DurationUnit(ChronoUnit.SECONDS)
    var idleTimeout: Duration = IDLE_TIMEOUT_DEFAULT
    var tcpNoDelay: Boolean = TCP_NO_DELAY_DEFAULT
    var secure: Boolean = SECURE_DEFAULT

    companion object {
        const val PORT_DEFAULT = 8099
        const val BOSS_COUNT_DEFAULT = 1
        const val WORKER_COUNT_DEFAULT = 100
        const val KEEP_ALIVE_DEFAULT = true
        const val BACK_LOG_DEFAULT = 100
        val IDLE_TIMEOUT_DEFAULT: Duration = Duration.ofSeconds(0)
        const val TCP_NO_DELAY_DEFAULT = true
        const val SECURE_DEFAULT = false
    }
}
