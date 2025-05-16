package ru.kotlix.frame.session.server.handler

import ru.kotlix.frame.session.api.SessionContract
import ru.kotlix.frame.session.api.SessionContract.ServerPacket.ServerResponse.PacketStatus
import ru.kotlix.frame.session.api.SessionContract.ServerPacket.SessionBreak.BreakCause

fun serverResponse(
    packetStatus: PacketStatus,
    pid: Long,
): SessionContract.ServerPacket =
    SessionContract.ServerPacket.newBuilder()
        .setServerResponse(
            SessionContract.ServerPacket.ServerResponse.newBuilder()
                .setPacketStatus(packetStatus)
                .setPid(pid)
                .build(),
        ).build()

fun sessionBreak(breakCause: BreakCause): SessionContract.ServerPacket =
    SessionContract.ServerPacket.newBuilder()
        .setSessionBreak(
            SessionContract.ServerPacket.SessionBreak.newBuilder()
                .setReason(breakCause)
                .build(),
        ).build()

fun messageNotify(
    fromChatId: Long,
    fromCommunityId: Long,
    fromUserId: Long,
    content: String,
): SessionContract.ServerPacket =
    SessionContract.ServerPacket.newBuilder()
        .setMessageNotify(
            SessionContract.ServerPacket.MessageNotify.newBuilder()
                .setFromChatId(fromChatId)
                .setFromCommunityId(fromCommunityId)
                .setFromUserId(fromUserId)
                .setContent(content)
                .build(),
        ).build()
