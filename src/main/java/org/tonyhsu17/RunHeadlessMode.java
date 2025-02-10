package org.tonyhsu17;

import org.tonyhsu17.shanaProjectParser.ShanaProjectParser;
import org.tonyhsu17.shanaProjectParser.poms.Season;
import org.tonyhsu17.shanaProjectParser.poms.SeriesInfo;
import org.tonyhsu17.utilities.HistoryLog;
import org.tonyhsu17.utilities.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;



public class RunHeadlessMode implements Logger {
    private String src;
    private String des;
    private String url;
    private Hashtable<String, String> seriesToFolder;

    private final String SPACING_REGEX = "[ ;-]*";
    private String unknownPath;
    private HistoryLog history;

    public RunHeadlessMode(String src, String des, String url) throws IOException {
        this(src, des, url, -1);
    }

    public RunHeadlessMode(String src, String des, String url, int logSize) throws IOException {
        this.src = src;
        this.des = des;
        this.url = url;
        seriesToFolder = new Hashtable<String, String>();
        unknownPath = des + File.separator + "Unknown";
        if(logSize == -1) {
            history = new HistoryLog(des);
        } else {
            history = new HistoryLog(des, logSize);
        }
        initFolders();
    }

    public void run() throws IOException, InterruptedException {
        copy();
    }

    /**
     * Initializes each Series folder. WIll not recreate the folder if it exist in history. Meaning
     * folder was already deleted.
     * 
     * @return List of paths for each series
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
                    history.add(sanitizedName);
                }
                seriesToFolder.put(sanitizedName.replaceAll(SPACING_REGEX, ""), seriesPath);
            }
        }
        history.save();
        new File(unknownPath).mkdirs();
    }

    /**
     * Copies file from source to a specific folder in destination.
     * 
     * @throws IOException
     */
    private void copy() throws IOException, InterruptedException {
        File srcFolder = new File(src);
        for(File file : getAllFiles(srcFolder)) {
            if(file.length() == 0 || file.isDirectory() || history.isInHistory(file.getName(), file.lastModified())) {
                continue;
            }
            long startLen = file.length();
            Thread.sleep(Duration.ofSeconds(5));
            if(file.length() != startLen) {
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
                history.add(file.getName(), file.lastModified()+"");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        history.save();
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

        // check local folders
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

        // if no folder found locally, refresh shanaproject follows for possible new seasons.
        if(returnVal.isEmpty()) {
            initFolders(); // get shanaproject to see if there is something new
            for(Entry<String, String> entry : seriesToFolder.entrySet()) {
                if(fileName.contains(entry.getKey())) {
                    returnVal = entry.getValue();
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
    
    /**
     * Returns list of files including sub directories.
     * 
     * @return List of all files
     */
    private List<File> getAllFiles(File root) {
    	List<File> allFiles = new ArrayList<File>();
    	Queue<File> workingSet = new PriorityQueue<File>();
    	workingSet.add(root);
    	while(!workingSet.isEmpty()) {
    		File file = workingSet.remove();
    		if(file.isDirectory()) {
    			workingSet.addAll(Arrays.asList(file.listFiles()));
    		} else {
    			allFiles.add(file);
    		}
    	}
    	return allFiles;
    }
}
