package ru.kotlix.frame.session.server.service

import org.springframework.stereotype.Service
import ru.kotlix.frame.parties.client.PartiesCommunityClient

@Service
class MessagePreferencesValidatorImpl(
    private val communityClient: PartiesCommunityClient,
) : MessagePreferencesValidator {
    override fun canBeNotifiedBy(
        userId: Long,
        community: List<Long>,
    ): Boolean {
        val communities = communityClient.findAllByUserId(userId).map { it.id }
        return communities.containsAll(community)
    }
}
