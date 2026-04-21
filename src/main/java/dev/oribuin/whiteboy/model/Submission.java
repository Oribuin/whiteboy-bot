package dev.oribuin.whiteboy.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class Submission {

    private final long id;
    private final String username;
    private final String path;
    private final long time;
    private Long message;

    public Submission(long id, String username, String path, long time) {
        this.id = id;
        this.username = username;
        this.path = path;
        this.time = time;
        this.message = null;
    }

    /**
     * Transfer the submission file to the target destination
     *
     * @param destination The destination folder for the new file
     * @return Whether the transfer was successful
     * @throws IOException Any exceptions that may occur
     */
    public boolean transfer(File destination) throws IOException {
        File current = this.getFile();
        if (!current.exists()) return false;
        if (!destination.isDirectory()) return false;

        File target = new File(destination, current.getName());
        Files.copy(current.toPath(), target.toPath());
        Files.delete(current.toPath());
        return true;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPath() {
        return path;
    }

    public File getFile() {
        return new File(this.path);
    }

    public long getTime() {
        return time;
    }

    public Long getMessage() {
        return message;
    }

    public void setMessage(Long message) {
        this.message = message;
    }
}
