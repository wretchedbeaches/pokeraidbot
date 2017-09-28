package pokeraidbot.jda;

import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.GuildMessageEmbedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EventLoggingListener implements EventListener{
    private static final Logger LOGGER = LoggerFactory.getLogger(EventLoggingListener.class);

    @Override
    public void onEvent(Event event) {
        if (LOGGER.isDebugEnabled()) {
            if (event instanceof GuildMessageReactionAddEvent) {
                final GuildMessageReactionAddEvent reactionAddEvent = (GuildMessageReactionAddEvent) event;
                LOGGER.debug("Reaction: " + reactionAddEvent.getUser() + " - " + reactionAddEvent.getReaction() +
                        " - " + reactionAddEvent.getReactionEmote());
            } else if (event instanceof GuildMessageReceivedEvent) {
                final GuildMessageReceivedEvent guildMessageReceivedEvent = (GuildMessageReceivedEvent) event;
                LOGGER.debug("Message from " + guildMessageReceivedEvent.getAuthor() + ": " + guildMessageReceivedEvent.getMessage());
            } else if (event instanceof GuildMessageEmbedEvent) {
                final GuildMessageEmbedEvent guildMessageReceivedEvent = (GuildMessageEmbedEvent) event;
                final List<MessageEmbed> messageEmbeds = guildMessageReceivedEvent.getMessageEmbeds();
                for (MessageEmbed embed : messageEmbeds)
                    LOGGER.debug("Embed message from " + embed.getAuthor() + ": " + String.valueOf(embed.getTitle()) +
                            " - " + String.valueOf(embed.getDescription()));
            }
        }
    }
}
