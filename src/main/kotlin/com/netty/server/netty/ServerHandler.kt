/*
 * Project automation-gw
 *
 * Copyright (c) RNR Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are strictly prohibited without permission.
 */

package com.netty.server.netty

import com.netty.server.netty.message.Message
import com.netty.server.service.ServerService
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import io.netty.util.AttributeKey
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * @author simonyi
 * @date 2016. 4. 15.
 */
@Component
@Sharable
class ServerHandler(
    @Autowired private val serverService: ServerService
) : SimpleChannelInboundHandler<Message>() {
    private val counter = AtomicInteger()
    override fun channelActive(ctx: ChannelHandlerContext) {
        logger.info(
            "Client Connected. ctx : {}, channel : {}, address : {}",
            ctx,
            ctx.channel(),
            ctx.channel().remoteAddress()
        )
        ctx.channel().attr(KEY_COUNTER).set(counter.getAndIncrement())
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        logger.info(
            "Client Disconnected. ctx : {}, channel : {}, counter : {}, address : {}",
            ctx,
            ctx.channel(),
            ctx.channel().attr(KEY_COUNTER).get(),
            ctx.channel().remoteAddress()
        )
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, event: Any) {
        (event as? IdleStateEvent)?.let {
            if (it.state() == IdleState.READER_IDLE) {
                logger.info("Connection read idle timeout. [{}]", ctx)
                ctx.close()
            }
        }
    }

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: Message?) {
        logger.info("fun : channelRead0, Read message : {}", msg)
        serverService.onMessage(msg)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        logger.info("fun : channelRead, Read message : {}", msg)
        serverService.onMessage( msg )
    }

    // Deprecated 되었다면서 어떻게 바꾸라는 말은 한마디도 없다. ㅠㅠ
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.error("Channel [{}] has risen exception [{}].", ctx, cause.message)
        ctx.close()
    }

    companion object{
        private val KEY_COUNTER = AttributeKey.valueOf<Int>("counter")
        val logger: Logger = LoggerFactory.getLogger("ServerHandler")
    }
}
