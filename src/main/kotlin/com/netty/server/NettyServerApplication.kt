package com.netty.server

import com.netty.server.netty.NetworkServer
import com.netty.server.service.ServerService
import org.springframework.beans.factory.getBean
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@SpringBootApplication
class NettyServerApplication

fun main(args: Array<String>) {

    val context = SpringApplication.run(NettyServerApplication::class.java, *args)

    val serverService = context.getBean<ServerService>()
    val networkServer = context.getBean<NetworkServer>()

    serverService.init()
    networkServer.start()
}

@Configuration
@Profile("local")
@ComponentScan(lazyInit = true)
class LocalProfile // lazyInit overriding. spring bean 이 많지 않을때는 효과가 미미하다.
