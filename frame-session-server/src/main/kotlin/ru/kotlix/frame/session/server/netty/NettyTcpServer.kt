package ru.kotlix.frame.session.server.netty

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.slf4j.LoggerFactory
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import ru.kotlix.frame.session.server.config.props.NettyProperties

@Component
class NettyTcpServer(
    private val nettyProperties: NettyProperties,
    nettyInitializer: SocketChannelInit,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val bossGroup = NioEventLoopGroup(nettyProperties.bossCount)
    private val workerGroup = NioEventLoopGroup(nettyProperties.workerCount)

    private val serverBootstrap =
        ServerBootstrap()
            .group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(nettyInitializer)
            .option(ChannelOption.SO_BACKLOG, nettyProperties.backlogSO)
    private var serverStarted = false
    private var serverClosed = false

    @EventListener
    fun contextStartup(event: ContextRefreshedEvent) {
        if (!serverStarted) {
            Thread {
                try {
                    val serverPort = nettyProperties.port
                    val serverChannelFuture = serverBootstrap.bind(serverPort).sync()
                    logger.info("Netty server started on port $serverPort.")
                    serverChannelFuture.channel().closeFuture().sync()
                } catch (ex: Exception) {
                    logger.error("Error happened during netty server startup.", ex)
                }
            }.start()
            serverStarted = true
        }
    }

    @EventListener
    fun contextStartup(event: ContextClosedEvent) {
        if (!serverClosed) {
            try {
                bossGroup.shutdownGracefully()
                workerGroup.shutdownGracefully()
                logger.info("Netty server stopped.")
            } catch (ex: Exception) {
                logger.error("Error happened during netty server shutdown.", ex)
            }
            serverClosed = true
        }
    }
}
