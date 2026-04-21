package dev.oribuin.whiteboy.manager;

import dev.oribuin.whiteboy.WhiteBoyBot;
import dev.oribuin.whiteboy.command.argument.TimezoneArgumentHandler;
import dev.oribuin.whiteboy.command.impl.InspireCommand;
import dev.oribuin.whiteboy.command.impl.SetupCommand;
import dev.oribuin.whiteboy.command.impl.SubmissionCommand;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.discord.jda6.JDA6CommandManager;
import org.incendo.cloud.discord.jda6.JDAInteraction;
import org.incendo.cloud.discord.jda6.annotation.ReplySettingBuilderModifier;
import org.incendo.cloud.discord.slash.DiscordSetting;
import org.incendo.cloud.discord.slash.annotation.CommandScopeBuilderModifier;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.setting.ManagerSetting;

import java.util.TimeZone;

public class CommandManager {

    private final WhiteBoyBot bot;
    private final JDA6CommandManager<JDAInteraction> manager = new JDA6CommandManager<>(
            ExecutionCoordinator.simpleCoordinator(),
            JDAInteraction.InteractionMapper.identity()
    );

    private final AnnotationParser<JDAInteraction> parser = new AnnotationParser<>(
            this.manager,
            JDAInteraction.class
    );

    public CommandManager(WhiteBoyBot bot) {
        this.bot = bot;

        // Register @CommandScope Annotation
        CommandScopeBuilderModifier.install(this.parser);

        // Register @ReplySetting Annotation
        ReplySettingBuilderModifier.install(this.parser);

        this.manager.settings().set(ManagerSetting.ALLOW_UNSAFE_REGISTRATION, true);
        this.manager.settings().set(ManagerSetting.OVERRIDE_EXISTING_COMMANDS, true);
        this.manager.discordSettings().set(DiscordSetting.EPHEMERAL_ERROR_MESSAGES, true);
        this.manager.exceptionController().registerHandler(Exception.class, x -> {
            System.out.println(" * Exception Occurred: " + x.exception().getMessage());
        });

//        this.manager.discordSettings().set(DiscordSetting.AUTO_REGISTER_SLASH_COMMANDS, true);

        // Parser Registry
        this.manager.parserRegistry().registerParser(
                ParserDescriptor.of(new TimezoneArgumentHandler(), TimeZone.class)
        );

        // Register The Commands
        this.parser.parse(new SetupCommand(), new InspireCommand(), new SubmissionCommand());
    }

    public JDA6CommandManager<JDAInteraction> getBuilder() {
        return manager;
    }

    public AnnotationParser<JDAInteraction> getParser() {
        return parser;
    }

}
