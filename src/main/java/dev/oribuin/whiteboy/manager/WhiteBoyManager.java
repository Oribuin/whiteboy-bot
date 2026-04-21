package dev.oribuin.whiteboy.manager;

import dev.oribuin.whiteboy.WhiteBoyBot;
import dev.oribuin.whiteboy.model.InspiredServer;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class WhiteBoyManager {

    public static final File INSPIRATION_FOLDER = new File("inspirations");
    public static final File SUBMISSION_FOLDER = new File("submissions");
    private static final Duration FIVE_MINUTES = Duration.ofMinutes(1);

    private final WhiteBoyBot bot;
    private final List<File> availableInspirations;
    private final Timer timer;
    private final TimerTask timerTask;

    public WhiteBoyManager(WhiteBoyBot bot) {
        this.bot = bot;
        this.availableInspirations = new ArrayList<>();
        this.loadInspirations();
        this.timerTask = new TimerTask() {
            @Override
            public void run() {
                checkHour();
            }
        };
        this.timer = new Timer("white-boy-inspire");
        this.timer.scheduleAtFixedRate(this.timerTask, FIVE_MINUTES.toMillis(), FIVE_MINUTES.toMillis());
    }

    public void loadInspirations() {
        if (!INSPIRATION_FOLDER.exists()) {
            System.out.println("* Error: Could not find an 'inspirations' folder, white boy cannot inspire...");
            return;
        }

        File[] valueStream = INSPIRATION_FOLDER.listFiles();
        if (valueStream == null || valueStream.length == 0) {
            System.out.println("* Error: Could not find anything in the 'inspirations' folder");
            return;
        }

        // remove the values from stream
        List<File> values = Arrays.stream(valueStream)
                .filter(x -> x.getName().endsWith(".png") || x.getName().endsWith(".jpg") || x.getName().endsWith(".gif"))
                .toList();

        if (values.isEmpty()) {
            System.out.println("* Error: Could not find anything in the 'inspirations' folder");
            return;
        }

        // add all the images into the bot
        this.availableInspirations.clear();
        this.availableInspirations.addAll(values);
    }

    public void checkHour() {
        Map<TimeZone, List<InspiredServer>> servers = new HashMap<>();

        // sort all the inspired servers by timezone first... white boy
        this.bot.getDataManager().getServers()
                .values()
                .stream()
                .filter(InspiredServer::canInspire) // check if server can be inspired
                .forEach(server -> {
                    List<InspiredServer> inspiredServers = new ArrayList<>(servers.getOrDefault(server.getTimeZone(), new ArrayList<>()));
                    inspiredServers.add(server);
                    servers.put(server.getTimeZone(), inspiredServers);
                });

        if (this.availableInspirations.isEmpty()) return;

        // Find out what timezone it is presently 6am 
        TimeZone selectedZone = null;
        for (TimeZone zone : servers.keySet()) {
            Calendar calendar = Calendar.getInstance(zone);
            if (calendar.get(Calendar.HOUR_OF_DAY) != 6) continue; // todo: let the server pick the hour

            selectedZone = zone;
            break;
        }


        // Get the available servers in the whatever
        List<InspiredServer> available = servers.get(selectedZone);
        if (selectedZone == null || available == null || available.isEmpty()) return;

        available.forEach(InspiredServer::inspire);
        this.bot.getDataManager().saveServers(available);

        System.out.println("* White Boy Up: Inspiring a total of [" + available.size() + "] servers in Timezone [" + selectedZone.toZoneId() + "]");
    }

    public File generateInspiration() {
        int random = (int) (Math.random() * this.availableInspirations.size());
        return this.availableInspirations.get(random);
    }

    public List<File> getAvailableInspirations() {
        return availableInspirations;
    }

    public Timer getTimer() {
        return timer;
    }

    public TimerTask getTimerTask() {
        return timerTask;
    }
}
