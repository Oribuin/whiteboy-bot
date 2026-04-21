package dev.oribuin.whiteboy.manager;

import dev.oribuin.whiteboy.WhiteBoyBot;
import dev.oribuin.whiteboy.database.DatabaseConnector;
import dev.oribuin.whiteboy.database.SQLiteConnector;
import dev.oribuin.whiteboy.model.InspiredServer;
import dev.oribuin.whiteboy.model.Submission;
import net.dv8tion.jda.api.entities.Guild;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class DataManager {

    private final WhiteBoyBot bot;
    private DatabaseConnector connector;
    private Map<Long, InspiredServer> servers;
    private Map<Long, Submission> submissions;

    /**
     * load the sql database... white boy
     *
     * @param bot the white boy
     */
    public DataManager(WhiteBoyBot bot) {
        this.bot = bot;
        this.servers = new HashMap<>();
        this.submissions = new HashMap<>();

        File file = new File("database.db");
        try {
            if (!file.exists()) {
                file.createNewFile();
                System.out.println("* Created new database file");
            }
        } catch (IOException ex) {
            System.out.println("* Failed to create a new database file: " + ex.getMessage());
            return;
        }

        this.connector = new SQLiteConnector(file);
        this.connector.cleanup();

        System.out.println("* Connected to the database successfully");
        this.connector.connect(connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.addBatch(CREATE_TABLE);
                statement.addBatch(CREATE_SUBMISSION_TABLE);
                statement.executeBatch();
            }
        });

        this.loadSubmissions();
    }

    /**
     * load the server... white boy
     *
     * @param guild the server to load
     */
    public void loadServer(Guild guild) {
        this.connector.connect(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(LOAD_SERVER)) {
                statement.setLong(1, guild.getIdLong());

                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) this.updateServer(InspiredServer.from(resultSet));
            }
        });
    }

    public void loadSubmissions() {
        this.connector.connect(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(LOAD_SUBMISSIONS)) {
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    Submission submission = new Submission(
                            resultSet.getLong("id"),
                            resultSet.getString("username"),
                            resultSet.getString("path"),
                            resultSet.getLong("time")
                    );

                    submission.setMessage(resultSet.getLong("message"));
                    this.submissions.put(submission.getMessage(), submission);
                }
            }

            // Scan for unavailable submissions
            List<Submission> toRemove = new ArrayList<>();
            this.submissions.entrySet().removeIf(entry -> {
                Submission submission = entry.getValue();
                Duration month = Duration.ofDays(30);
                if (submission.getTime() + month.toMillis() > System.currentTimeMillis()) {
                    toRemove.add(submission);
                    return true;
                }

                File target = submission.getFile();
                if (!target.exists()) {
                    target.delete();
                    toRemove.add(submission);
                    return true;
                }

                return false;
            });

            toRemove.forEach(this::deleteSubmission);
        });
    }

    /**
     * get the inspired server... white boy
     *
     * @param guild The guild to get
     * @return The returning inspired server or new one
     */
    public InspiredServer getServer(Guild guild) {
        InspiredServer inspired = this.servers.get(guild.getIdLong());
        if (inspired != null) return inspired;

        return new InspiredServer(guild.getIdLong(), TimeZone.getTimeZone("Europe/London"), 0L, 0L);
    }

    /**
     * Inspire a server... white boy
     *
     * @param server The server being inspired
     */
    public void inspireServer(InspiredServer server) {
        this.servers.put(server.getId(), server);
        this.connector.connect(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(UPDATE_SERVER)) {
                statement.setLong(1, server.getId());
                statement.setString(2, server.getTimeZone().getID());
                statement.setString(3, server.getInspiration());
                statement.setLong(4, server.getRole());
                statement.setLong(5, server.getChannel());
                statement.setLong(6, server.getLastInspiration());
                statement.executeUpdate();
            }
        });
    }

    /**
     * Inspire multiple servers... white boy
     *
     * @param servers The servers being inspired
     */
    public void saveServers(List<InspiredServer> servers) {
        for (InspiredServer server : servers) {
            this.servers.put(server.getId(), server);
        }

        this.connector.connect(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(UPDATE_SERVER)) {
                for (InspiredServer server : servers) {
                    statement.setLong(1, server.getId());
                    statement.setString(2, server.getTimeZone().getID());
                    statement.setString(3, server.getInspiration());
                    statement.setLong(4, server.getRole());
                    statement.setLong(5, server.getChannel());
                    statement.setLong(6, server.getLastInspiration());
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        });
    }

    /**
     * update the server in the cache... white boy
     *
     * @param server the server to be updated
     */
    public void updateServer(InspiredServer server) {
        this.servers.put(server.getId(), server);
    }

    /**
     * Save a submission into the database
     *
     * @param submission The submission to save
     */
    public void saveSubmission(Submission submission) {
        this.submissions.put(submission.getMessage(), submission);

        this.connector.connect(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(ADD_SUBMISSION)) {
                statement.setLong(1, submission.getId());
                statement.setString(2, submission.getUsername());
                statement.setString(3, submission.getPath());
                statement.setLong(4, submission.getTime());
                statement.setLong(5, submission.getMessage());
                statement.executeUpdate();
            }
        });
    }

    /**
     * Delete a submission from the database
     *
     * @param submission The submission to delete
     */
    public void deleteSubmission(Submission submission) {
        this.submissions.remove(submission.getMessage());

        this.connector.connect(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(DELETE_SUBMISSION)) {
                statement.setString(1, submission.getPath());
                statement.setLong(2, submission.getTime());
                statement.setLong(3, submission.getMessage());
                statement.executeUpdate();
            }
        });
    }

    public Map<Long, InspiredServer> getServers() {
        return servers;
    }

    public Map<Long, Submission> getSubmissions() {
        return submissions;
    }

    // region setup the tables... white boy
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `whiteboy_servers` (" +
            "`serverId` LONG NOT NULL PRIMARY KEY," +
            "`timezone` TEXT NOT NULL, " +
            "`inspiration` TEXT NULL, " +
            "`role` LONG NULL, " +
            "`channel` LONG NULL, " +
            "`lastInspiration` LONG NOT NULL" +
            ")";

    public static final String LOAD_SERVER = "SELECT * FROM `whiteboy_servers` WHERE `serverId` = ?";
    public static final String UPDATE_SERVER = "REPLACE INTO `whiteboy_servers` (`serverId`, `timezone`, `inspiration`, `role`, `channel`, `lastInspiration`) VALUES (?, ?, ?, ?, ?, ?)";
    public static final String CLEAR_SERVER = "DELETE FROM `whiteboy_servers` WHERE `serverId` = ?";
    // endregion

    // region User Submissions
    public static final String CREATE_SUBMISSION_TABLE = "CREATE TABLE IF NOT EXISTS `whiteboy_submissions` (" +
            "`id` LONG NOT NULL, " +
            "`username` TEXT NOT NULL, " +
            "`path` TEXT NOT NULl, " +
            "`time` LONG NOT NULL, " +
            "`message` LONG NULL)";

    public static final String LOAD_SUBMISSIONS = "SELECT * FROM `whiteboy_submissions`";
    public static final String ADD_SUBMISSION = "INSERT INTO `whiteboy_submissions` (" +
            "`id`, " +
            "`username`, " +
            "`path`, " +
            "`time`, " +
            "`message`) " +
            "VALUES (?, ?, ?, ?, ?)";
    public static final String DELETE_SUBMISSION = "DELETE FROM `whiteboy_submissions` WHERE " +
            "`path` = ? AND " +
            "`time` = ? AND " +
            "`message` = ?";
    // endregion

    // region Banned/Restricted Users
    // endregion

}
