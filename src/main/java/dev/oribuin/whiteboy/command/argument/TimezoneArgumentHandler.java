package dev.oribuin.whiteboy.command.argument;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.caption.StandardCaptionKeys;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.discord.jda5.JDAInteraction;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.time.ZoneId;
import java.util.List;
import java.util.TimeZone;

public class TimezoneArgumentHandler implements ArgumentParser<JDAInteraction, TimeZone> {

    private static final List<String> DEFAULT_TIMEZONES = List.of(
            "America/Los_Angeles",
            "America/Phoenix",
            "America/Texas",
            "America/New_York",
            "America/Chicago",
            "Asia/Tokyo",
            "Australia/Sydney",
            "Europe/London",
            "Europe/Paris",
            "Europe/Dublin",
            "Pacific/Honolulu",
            "Europe/Kyiv",
            "Europe/Madrid,",
            "Europe/Rome",
            "Europe/Berlin",
            "Africa/Cairo",
            "Asia/Bangkok",
            "Europe/Istanbul"
    );

    @Override
    public @NonNull ArgumentParseResult<@NonNull TimeZone> parse(
            @NonNull CommandContext<@NonNull JDAInteraction> commandContext,
            @NonNull CommandInput commandInput
    ) {
        String input = commandInput.peekString();
        if (input.isEmpty()) {
            System.out.println("* Error: Could not find TimeZone because input was not provided");
            return ArgumentParseResult.failure(new TimeZoneParserException(input, commandContext));
        }
        
        TimeZone timeZone = TimeZone.getTimeZone(ZoneId.of(input));
        if (timeZone == null) {
            System.out.println("* Error: Could not find TimeZone by name: " + input);
            return ArgumentParseResult.failure(new TimeZoneParserException(input, commandContext));
        }

        return ArgumentParseResult.success(timeZone);
    }

    @Override
    public @NonNull SuggestionProvider<JDAInteraction> suggestionProvider() {
        return SuggestionProvider.blocking((context, input) ->
                DEFAULT_TIMEZONES.stream()
                        .sorted()
                        .map(Suggestion::suggestion)
                        .toList()
        );
    }

    public static final class TimeZoneParserException extends ParserException {

        private final String input;

        public TimeZoneParserException(String input, CommandContext<?> context) {
            super(TimezoneArgumentHandler.class, context, StandardCaptionKeys.EXCEPTION_INVALID_SYNTAX, CaptionVariable.of("input", input));

            this.input = input;
        }

        public String getInput() {
            return input;
        }
    }
}
