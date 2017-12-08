package pokeraidbot.commands;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import main.BotServerMain;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import pokeraidbot.BotService;
import pokeraidbot.domain.gym.GymRepository;
import pokeraidbot.domain.tracking.TrackingService;
import pokeraidbot.infrastructure.jpa.config.ServerConfigRepository;
import pokeraidbot.infrastructure.jpa.config.UserConfig;
import pokeraidbot.infrastructure.jpa.config.UserConfigRepository;

import java.util.List;

public class AdminCommands extends Command {
    private final UserConfigRepository userConfigRepository;
    private final ServerConfigRepository serverConfigRepository;
    private final GymRepository gymRepository;
    private final BotService botService;
    private final TrackingService trackingCommandListener;

    public AdminCommands(UserConfigRepository userConfigRepository, ServerConfigRepository serverConfigRepository,
                         GymRepository gymRepository, BotService botService,
                         TrackingService trackingCommandListener) {
        this.userConfigRepository = userConfigRepository;
        this.serverConfigRepository = serverConfigRepository;
        this.gymRepository = gymRepository;
        this.botService = botService;
        this.trackingCommandListener = trackingCommandListener;
        this.guildOnly = false;
        this.name = "admin";
        this.help = "Admin commands, only for Bot creator.";
    }

    @Override
    protected void execute(CommandEvent event) {
        final User author = event.getAuthor();
        if (author == null || author.getId() == null || (!author.getId().equals(BotServerMain.BOT_CREATOR_USERID))) {
            event.replyInDM("This command is reserved only for bot creator. Hands off! ;p Your user ID was: " +
                    String.valueOf(author.getId()));
            return;
        } else {
            if (event.getArgs().startsWith("userconfig")) {
                String userId = event.getArgs().replaceAll("userconfig\\s{1,3}", "");
                final UserConfig userConfig = userConfigRepository.findOne(userId);
                if (userConfig == null) {
                    event.replyInDM("No user with ID " + userId);
                    return;
                } else {
                    userConfigRepository.delete(userConfig);
                    event.replyInDM("Removed user configuration for user with ID " + userId);
                    return;
                }
            } else if (event.getArgs().startsWith("permissions")) {
                final JDA bot = botService.getBot();
                final List<Guild> guilds = bot.getGuilds();
                StringBuilder sb = new StringBuilder();
                sb.append("**Permissions for bot across servers:**\n\n");
                for (Guild guild : guilds) {
                    final Member member = guild.getMember(bot.getSelfUser());
                    if (member == null) {
                        event.replyInDM("Could not get bot as servermember!");
                        return;
                    }
                    sb.append("*").append(guild.getName()).append("*\n");
                    for (Permission p : member.getPermissions()) {
                        sb.append(p.getName()).append("(Guild: ").append(p.isGuild())
                                .append(" Channel: ").append(p.isChannel()).append(")\n");
                    }
                    sb.append("\n\n");
                }
                event.replyInDM(sb.toString());
                return;
            } else if (event.getArgs().startsWith("clear tracking")) {
                trackingCommandListener.clearCache();
                event.replyInDM("Cleared tracking cache.");
                return;
            } else if (event.getArgs().startsWith("announce")) {
                final JDA bot = botService.getBot();
                final List<Guild> guilds = bot.getGuilds();
                StringBuilder sb = new StringBuilder();
                for (Guild guild : guilds) {
                    try {
                        guild.getDefaultChannel().sendMessage(event.getArgs()
                                .replaceAll("announce\\s{1,3}", "")).queue();
                        sb.append("Sent message for guild ").append(guild.getName()).append("\n");
                    } catch (Throwable t) {
                        sb.append("Failed to send message for guild ").append(guild.getName())
                                .append(": ").append(t.getMessage()).append("\n");
                    }
                }
                event.replyInDM(sb.toString());
                return;
            } else if (event.getArgs().startsWith("ismember")) {
                String userIdAndGuildName = event.getArgs().replaceAll("ismember\\s{1,3}", "");
                String[] args = userIdAndGuildName.split(" ");
                if (args.length < 2) {
                    event.reply("Bad syntax, should be something like: !raid admin ismember {userid} {guildname}");
                    return;
                } else {
                    final JDA bot = botService.getBot();
                    Guild guild = null;
                    final List<Guild> guilds = bot.getGuilds();
                    for (Guild guildToCheck : guilds) {
                        if (guildToCheck.getName().equalsIgnoreCase(args[1])) {
                            guild = guildToCheck;
                        }
                    }
                    if (guild != null) {
                        final Member memberById = guild.getMemberById(args[0]);
                        if (memberById != null) {
                            event.reply("User is a member of server " + guild.getName());
                        } else {
                            event.reply("User is not a member of server " + guild.getName());
                        }
                    }
                    return;
                }
            } else if (event.getArgs().startsWith("guilds")) {
                final JDA bot = botService.getBot();
                final List<Guild> guilds = bot.getGuilds();
                StringBuilder sb = new StringBuilder();
                for (Guild guildToCheck : guilds) {
                    sb.append(guildToCheck.getName().toLowerCase()).append("\n");
                }
                event.reply(sb.toString());
                return;
            }
        }
        event.reply("No such command. Existing ones are:\n- userconfig {userid}\n- permissions\n" +
                "- clear tracking\n- announce {message}\n- ismember {userid} {guild name}\n- guilds");
    }
}
