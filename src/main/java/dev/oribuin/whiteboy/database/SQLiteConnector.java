package dev.oribuin.whiteboy.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class SQLiteConnector implements DatabaseConnector {

    private Connection connection;
    private final String connectionString;
    private final AtomicInteger openConnections;
    private final Object lock;


    public SQLiteConnector(File file) {
        this.connectionString = "jdbc:sqlite:" + file.getAbsolutePath();
        this.openConnections = new AtomicInteger();
        this.lock = new Object();

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connect(ConnectionCallback callback) {
        try {
            if (this.connection == null || this.connection.isClosed()) {
                this.connection = DriverManager.getConnection(this.connectionString);
                this.connection.setAutoCommit(false);
            }
        } catch (SQLException ex) {
            System.out.println(" * Failed to connect to the database: " + ex.getMessage());
        }

        this.openConnections.incrementAndGet();
        try {
            if (this.connection.getAutoCommit())
                this.connection.setAutoCommit(false);

            callback.accept(this.connection);
            try {
                this.connection.commit();
            } catch (SQLException ex) {
                if (ex.getMessage() != null && !ex.getMessage().contains("transaction"))
                    throw ex;

                try {
                    this.connection.rollback();
                } catch (SQLException ignored) {
                }
            }
        } catch (Exception ex) {
            System.out.println(" * Failed to execute database query: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            int open = this.openConnections.decrementAndGet();
            if (open == 0) {
                synchronized (this.lock) {
                    this.lock.notify();
                }
            }
        }
    }

    /**
     * Executes a callback with a Connection passed and automatically closes it when finished
     *
     * @param callback       The callback to execute once the connection is retrieved
     * @param useTransaction If true, the query will be executed within a transaction. Not all connectors will use this.
     */
    @Override
    public void connect(ConnectionCallback callback, boolean useTransaction) {
        if (useTransaction) {
            this.connect(callback);
            return;
        }

        try {
            if (this.connection == null || this.connection.isClosed())
                this.connection = DriverManager.getConnection(this.connectionString);
        } catch (SQLException ex) {
            System.out.println("* An error occurred retrieving the SQLite database connection: " + ex.getMessage());
        }

        this.openConnections.incrementAndGet();
        try {
            if (!this.connection.getAutoCommit())
                this.connection.setAutoCommit(true);

            callback.accept(this.connection);
        } catch (Exception ex) {
            System.out.println("* An error occurred executing an SQLite query: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            int open = this.openConnections.decrementAndGet();
            synchronized (this.lock) {
                if (open == 0)
                    this.lock.notify();
            }
        }
    }

    /**
     * Aquires a database connection.
     * You must close this connection by calling {@link Connection#close()}.
     *
     * @return The database connection.
     */
    @Override
    public Connection connect() throws SQLException {
        try {
            if (this.connection == null || this.connection.isClosed())
                this.connection = DriverManager.getConnection(this.connectionString);
        } catch (SQLException ex) {
            System.out.println("* An error occurred retrieving the SQLite database connection: " + ex.getMessage());
        }
        return this.connection;
    }

    @Override
    public void closeConnection() {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (SQLException ex) {
            System.out.println(" * Failed to close the database connection: " + ex.getMessage());
        }

    }

    @Override
    public Object getLock() {
        return this.lock;
    }

    @Override
    public boolean isFinished() {
        return this.openConnections.get() == 0;
    }

    @Override
    public void cleanup() {
        this.connect(connection -> {
            try {
                connection.prepareStatement("VACUUM").execute();
            } catch (SQLException ignored) {
            }
        });

    }

}
