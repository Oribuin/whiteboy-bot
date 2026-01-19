package dev.oribuin.whiteboy.manager;

import dev.oribuin.whiteboy.WhiteBoyBot;
import dev.oribuin.whiteboy.database.DatabaseConnector;
import dev.oribuin.whiteboy.database.SQLiteConnector;
import dev.oribuin.whiteboy.model.InspiredServer;
import net.dv8tion.jda.api.entities.Guild;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class DataManager {

    private final WhiteBoyBot bot;
    private DatabaseConnector connector;
    private Map<Long, InspiredServer> servers;

    /**
     * load the sql database... white boy
     *
     * @param bot the white boy
     */
    public DataManager(WhiteBoyBot bot) {
        this.bot = bot;
        this.servers = new HashMap<>();

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
                statement.executeBatch();
            }
        });
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
                if (resultSet.next()) this.update(InspiredServer.from(resultSet));
            }
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
    public void update(InspiredServer server) {
        this.servers.put(server.getId(), server);
    }

    public Map<Long, InspiredServer> getServers() {
        return servers;
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

}
