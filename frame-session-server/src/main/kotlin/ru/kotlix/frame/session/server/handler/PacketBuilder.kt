package ru.kotlix.frame.session.server.handler

import ru.kotlix.frame.session.api.proto.SessionContract
import ru.kotlix.frame.session.api.proto.SessionContract.ServerPacket.ServerResponse.PacketStatus
import ru.kotlix.frame.session.api.proto.SessionContract.ServerPacket.SessionBreak.BreakCause
import ru.kotlix.frame.session.server.service.dto.Attendant
import ru.kotlix.frame.session.server.service.dto.VoiceNotification

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

fun voiceNotify(
    voiceId: Long,
    party: List<Attendant>,
    changed: Attendant,
    action: VoiceNotification.Action,
): SessionContract.ServerPacket =
    SessionContract.ServerPacket.newBuilder()
        .setVoiceNotify(
            SessionContract.VoiceNotify.newBuilder()
                .setVoiceId(voiceId)
                .addAllParty(
                    party.map { att ->
                        SessionContract.VoiceAttendant.newBuilder()
                            .setUserId(att.userId)
                            .setShadowId(att.shadowId)
                            .build()
                    },
                )
                .setChanged(
                    SessionContract.VoiceAttendant.newBuilder()
                        .setUserId(changed.userId)
                        .setShadowId(changed.shadowId)
                        .build(),
                )
                .setAction(
                    when (action) {
                        VoiceNotification.Action.JOINED -> SessionContract.VoiceNotify.Action.JOINED
                        VoiceNotification.Action.LEFT -> SessionContract.VoiceNotify.Action.LEFT
                    },
                ),
        ).build()
