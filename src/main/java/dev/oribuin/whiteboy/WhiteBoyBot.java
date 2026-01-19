package dev.oribuin.whiteboy;

import dev.oribuin.whiteboy.listener.BotListeners;
import dev.oribuin.whiteboy.manager.CommandManager;
import dev.oribuin.whiteboy.manager.DataManager;
import dev.oribuin.whiteboy.manager.WhiteBoyManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.incendo.cloud.discord.jda5.JDA5CommandManager;
import org.incendo.cloud.discord.jda5.JDAInteraction;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class WhiteBoyBot extends ListenerAdapter {

    private static WhiteBoyBot instance;
    private JDA application;
    private CommandManager commandManager;
    private DataManager dataManager;
    private WhiteBoyManager whiteBoyManager;

    public static WhiteBoyBot getInstance() {
        return instance;
    }

    /**
     * load the bot instance... white boy
     *
     * @param args The arguments passed to the program.
     */
    public static void main(String[] args) {
        new WhiteBoyBot(args);
    }

    /**
     * setup the original white boy... white boy...
     *
     * @param args white boy stuff i dont remember
     */
    public WhiteBoyBot(String[] args) {
        String token = args.length == 0 ? this.loadToken() : args[0];
        if (token == null) {
            System.out.println("* Failed to load the token. Are you sure you provided one?");
            return;
        }

        instance = this;

        this.dataManager = new DataManager(this);
        this.commandManager = new CommandManager(this);
        this.whiteBoyManager = new WhiteBoyManager(this);

        JDABuilder builder = JDABuilder.createDefault(token, List.of(GatewayIntent.values()));
        builder.addEventListeners(this,
                        new BotListeners(this),
                        this.commandManager.getBuilder().createListener()
                )
                .setEnabledIntents(Arrays.asList(GatewayIntent.values()))
                .enableCache(CacheFlag.MEMBER_OVERRIDES)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setActivity(Activity.customStatus("yearn for me... white boy"));

        this.application = builder.build();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        JDA5CommandManager<JDAInteraction> manager = this.commandManager.getBuilder();
        manager.registerGlobalCommands(this.application);
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        JDA5CommandManager<JDAInteraction> manager = this.commandManager.getBuilder();
        manager.registerGuildCommands(event.getGuild());
    }


    /**
     * @return The bot token from the token.txt file.
     */
    private String loadToken() {
        try {
            // Read first line of token.txt that starts with token:
            return Files.lines(Paths.get("token.txt"))
                    .filter(line -> line.startsWith("token:"))
                    .findFirst()
                    .map(line -> line.replace("token: ", ""))
                    .orElse(null);
        } catch (IOException ex) {
            return null;
        }
    }

    public JDA getApplication() {
        return application;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public WhiteBoyManager getWhiteBoyManager() {
        return whiteBoyManager;
    }
}
