/*
 * Project automation-gw
 *
 * Copyright (c) RNR Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are strictly prohibited without permission.
 */

package com.netty.server.netty

import com.netty.server.netty.message.MessageCoder
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder

@Sharable
class MessageDecoder : MessageToMessageDecoder<String>() {
    override fun decode(ctx: ChannelHandlerContext, msg: String, out: MutableList<Any>) {
        out.add(MessageCoder.decode(msg))
    }
}
