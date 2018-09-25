package core;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import org.tonyhsu17.shanaProjectParser.ShanaProjectParser;
import org.tonyhsu17.shanaProjectParser.poms.Season;
import org.tonyhsu17.shanaProjectParser.poms.SeriesInfo;
import org.tonyhsu17.utilities.HistoryLog;
import org.tonyhsu17.utilities.Logger;



public class RunHeadlessMode implements Logger {
    private String src;
    private String des;
    private String url;
    private Hashtable<String, String> seriesToFolder;

    private final String SPACING_REGEX = "[ ;-]*";
    private String unknownPath;
    private HistoryLog history;

    public RunHeadlessMode(String src, String des, String url) throws IOException {
        this.src = src;
        this.des = des;
        this.url = url;
        seriesToFolder = new Hashtable<String, String>();
        unknownPath = des + File.separator + "Unknown";
        history = new HistoryLog(des);
        initFolders();
    }

    public void run() throws IOException {
        copy();
    }

    /**
     * Initializes each Series folder. WIll not recreate the folder if it exist in history. Meaning
     * folder was already deleted.
     * 
     * @throws IOException
     */
    private void initFolders() throws IOException {
        File seriesFolder;
        List<Season> seasons = ShanaProjectParser.parse(url);

        // for each season, grab series info
        for(Season season : seasons) {
            String seasonPath = Paths.get(des, season.getSeason()).toString();
            List<SeriesInfo> series = season.getSeries();
            for(SeriesInfo ser : series) {
                // for each series, sanitize bad file characters
                String sanitizedName = ser.getSeries().replaceAll("[/\\:*?\"<>|]*", "");
                String seriesPath = Paths.get(seasonPath, sanitizedName).toString();
                // if series folder is not in history, create folders and save to history
                if(!history.isInHistory(sanitizedName)) {
                    seriesFolder = new File(seriesPath);
                    seriesFolder.mkdirs();
                    history.addToWriteList(sanitizedName);
                }
                seriesToFolder.put(sanitizedName.replaceAll(SPACING_REGEX, ""), seriesPath);
            }
        }
        history.writeToFile();
        new File(unknownPath).mkdirs();
    }

    /**
     * Copies file from source to a specific folder in destination.
     * 
     * @throws IOException
     */
    private void copy() throws IOException {
        File srcFolder = new File(src);
        for(File file : srcFolder.listFiles()) {
            if(file.isDirectory() || history.isInHistory(file.getName(), file.lastModified())) {
                continue;
            }
            String desPath = getFolderForName(file.getName());
            if(!desPath.isEmpty() && Files.exists(Paths.get(desPath), LinkOption.NOFOLLOW_LINKS)) {
                desPath = Paths.get(desPath, file.getName()).toString();
            }
            else {
                desPath = Paths.get(unknownPath, file.getName()).toString();
                info("No folder found for file: " + file.getName() + " using unknown folder.");
            }

            try {
                info("Copying: " + file.getPath() + " to: " + desPath);
                Files.copy(Paths.get(file.getPath()), Paths.get(desPath), StandardCopyOption.REPLACE_EXISTING);
                history.addToWriteList(file.getName(), file.lastModified());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        history.writeToFile();
    }

    /**
     * Returns folder path to use.
     * 
     * @param fileName
     * @return
     * @throws IOException
     */
    private String getFolderForName(String fileName) throws IOException {
        String returnVal = "";
        fileName = fileName.replaceAll(SPACING_REGEX, "");
        for(Entry<String, String> entry : seriesToFolder.entrySet()) {
            if(fileName.contains(entry.getKey())) {
                returnVal = entry.getValue();
            }
        }
        // if none found from shanaproject, check local folders
        if(returnVal.isEmpty()) {
            initFolders(); // get shanaproject to see if there is something new

            List<File> folders = new ArrayList<File>();
            // recurse through all folders and get folders
            Files.walkFileTree(new File(des).toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    folders.add(dir.toFile());
                    return FileVisitResult.CONTINUE;
                }
            });
            for(File f : folders) {
                if(fileName.contains(f.getName().replaceAll(SPACING_REGEX, ""))) {
                    returnVal = f.getPath();
                }
            }
        }
        return returnVal;
    }

    /**
     * Returns name of history log.
     * 
     * @return
     */
    public String getHistoryLogName() {
        return history.getName();
    }
}
