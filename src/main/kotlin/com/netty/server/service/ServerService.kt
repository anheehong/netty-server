package com.netty.server.service

import com.netty.server.netty.message.Message
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ServerService {

    companion object{
        val logger: Logger = LoggerFactory.getLogger("NetworkServer")
    }

    fun init(){
        logger.info("init server")
    }

    fun onMessage(msg: Message?): Message?{
        logger.info("onMessage")
        return msg
    }

    fun onMessage(obj: Any): Any{
        logger.info("onMessage")
        return obj
    }
}