package ru.kotlix.frame.session.server.netty

import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.protobuf.ProtobufDecoder
import io.netty.handler.codec.protobuf.ProtobufEncoder
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import org.springframework.stereotype.Component
import ru.kotlix.frame.session.api.proto.SessionContract

@Component
class SocketPipeline(
    private val clientPacketsHandler: SimpleChannelInboundHandler<SessionContract.ClientPacket>,
) : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel?) {
        ch?.pipeline()?.apply {
            addLast(LoggingHandler(LogLevel.DEBUG))

            addLast(ProtobufVarint32FrameDecoder())
            addLast(ProtobufDecoder(SessionContract.ClientPacket.getDefaultInstance()))

            addLast(ProtobufVarint32LengthFieldPrepender())
            addLast(ProtobufEncoder())

            addLast(clientPacketsHandler)
        }
    }
}
