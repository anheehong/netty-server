/*
 * Project automation-gw
 *
 * Copyright (c) RNR Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are strictly prohibited without permission.
 */

package com.netty.server.netty

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Netty 서버를 object 화 한다.
 * @author simonyi
 * @date 2016. 5. 11.
 */
open class NetworkServer(private val serverBootstrap: ServerBootstrap) {
    /** Netty's Server Channel. */
    private var serverChannel: Channel? = null
    var serverPort:Int = 0

    fun start() {
        try {
            logger.info("Server Started. Listen TCP port : {}", serverPort)
            serverChannel = serverBootstrap.bind(serverPort).sync().channel().closeFuture().sync().channel()
        } catch (e: InterruptedException) {
            logger.error("Server Start Failed. [{}]", e.message)
            Thread.currentThread().interrupt()
        }
    }

    /**
     * shutdown the server.
     */
    fun stop() {
        logger.info("Server ShutDown. {}", serverChannel ?: "")
        serverChannel?.let {
            it.close()
            it.parent().close()
        }
    }

    companion object{
        val logger: Logger = LoggerFactory.getLogger("NetworkServer")
    }
}
