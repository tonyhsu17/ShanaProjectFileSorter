package test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.tonyhsu17.RunHeadlessMode;


/**
 * Unit Tests for {@link RunHeadlessMode}
 * 
 * @author Tony Hsu
 *
 */
public class HeadlessModeTests {
    private RunHeadlessMode headless;
    private File testFolder;
    private String src;
    private String des;
    private String url;

    @BeforeClass
    public void beforeClass() throws IOException {
        testFolder = new File("testFolder");
        testFolder.mkdir();

        src = testFolder + "/src";
        des = testFolder + "/dest";
        url = "http://www.shanaproject.com/user/ikersaro/";

        new File(src).mkdir();
        new File(des).mkdir();
        try {
            headless = new RunHeadlessMode(src, des, url);
        }
        catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @AfterClass
    public void afterClass() throws IOException {
        // delete testFolder and files
        testFolder = new File("testFolder");
        if(testFolder.exists()) {
            Files.walkFileTree(testFolder.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    Files.delete(file); // this will work because it's always a File
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir); //this will work because Files in the directory are already deleted
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private enum DirectoriesDefault {
        BLUE_STEEL("Fall 2013/Aoki Hagane no Arpeggio -Ars Nova-"),
        AOT("Summer 2018/Shingeki no Kyojin 3"),
        OVERLORD("Summer 2018/Overlord S3"),
        RAILGUN("Unknown/A Certain Scientific Railgun - Toaru Kagaku no Railgun"),
        RAILGUN_S("Unknown/A Certain Scientific Railgun S",
            "[deanzel] A Certain Scientific Railgun S - 01 [1080p BD Hi10p Dual Audio FLAC-AC3][E731441F]"),
        UNKNOWN("Unknown", "[MD] Arpeggio of Blue Steel 01.mkv",
            "[MD] Arpeggio of Blue Steel 02.mkv",
            "[MD] Arpeggio of Blue Steel 03.mkv",
            "[HorribleSubs] Shingeki no Kyojin S3 - 30 [720p].mkv", "[HorribleSubs] Overlord III - 01 [720p]");

        private String path;
        private String[] files;

        private DirectoriesDefault(String path, String... files) {
            this.path = path;
            this.files = files;
        }

        public String path() {
            return path;
        }

        public boolean contains(String str) {
            for(String file : files) {
                if(file.equals(str)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Test
    public void testDefault() throws IOException {
        String[] files = {"[MD] Arpeggio of Blue Steel 01.mkv",
            "[MD] Arpeggio of Blue Steel 02.mkv",
            "[MD] Arpeggio of Blue Steel 03.mkv",
            "[deanzel] A Certain Scientific Railgun S - 01 [1080p BD Hi10p Dual Audio FLAC-AC3][E731441F]",
            "[HorribleSubs] Shingeki no Kyojin S3 - 30 [720p].mkv",
            "[HorribleSubs] Overlord III - 01 [720p]"};
        FileWriter fw;
        for(String str : files) {
            fw = new FileWriter(src + "/" + str);
            fw.close();
        }
        headless.run();

        SoftAssert softAssert = new SoftAssert();
        for(DirectoriesDefault dir : DirectoriesDefault.values()) {
            File f = new File(des + "/" + dir.path());
            softAssert.assertTrue(f.exists() && f.isDirectory(), "Directory: " + dir.path() + " does not exist or isn't a directory");
            for(File subF : f.listFiles()) {
                if(subF.isFile()) {
                    softAssert.assertTrue(dir.contains(subF.getName()), "Directory: " + dir.path() + " missing: " + subF.getName());
                }
            }
        }
        softAssert.assertAll();
    }

    @Test(dependsOnMethods = {"testDefault"})
    public void testFolderRename() throws IOException {
        String[] files = {"[MD] Arpeggio of Blue Steel 04.mkv",
            "[MD] Arpeggio of Blue Steel 05.mkv",
            "[MD] Arpeggio of Blue Steel 06.mkv",
            "[deanzel] A Certain Scientific Railgun S - 01 [1080p BD Hi10p Dual Audio FLAC-AC3][E731441F]",
            "[HorribleSubs] Shingeki no Kyojin S3 - 31 [720p].mkv",
            "[HorribleSubs] Overlord III - 02 [720p].mkv",
            "[HorribleSubs] Shingeki no Kyojin III - 29 [720p].mkv"};
        FileWriter fw;
        for(String str : files) {
            fw = new FileWriter(src + "/" + str);
            fw.close();
        }

        for(DirectoriesRenamed rename : DirectoriesRenamed.values()) {
            new File(des + "/" + rename.beforePath()).renameTo(new File(des + "/" + rename.afterPath()));
        }

        headless.run();

        SoftAssert softAssert = new SoftAssert();
        for(DirectoriesModified dir : DirectoriesModified.values()) {
            File f = new File(des + "/" + dir.path());
            System.out.println(des + "/" + dir.path());
            softAssert.assertTrue(f.exists() && f.isDirectory(), "Directory: " + dir.path() + " does not exist or isn't a directory");
            for(String fileStr : dir.files()) {
                File[] filtedFiles = f.listFiles((file, str) -> {
                    return str.equals(fileStr);
                });
                System.out.println(Arrays.toString(filtedFiles));
                softAssert.assertEquals(filtedFiles.length, 1, "File: " + fileStr + " not found");
            }
        }
        softAssert.assertAll();
    }

    private enum DirectoriesRenamed {
        BLUE_STEEL(DirectoriesDefault.BLUE_STEEL.path(), DirectoriesModified.BLUE_STEEL.path()),
        AOT(DirectoriesDefault.AOT.path(), DirectoriesModified.AOT.path()),
        OVERLORD(DirectoriesDefault.OVERLORD.path(), DirectoriesModified.OVERLORD.path());

        private String beforePath;
        private String afterPath;

        private DirectoriesRenamed(String before, String after) {
            beforePath = before;
            afterPath = after;
        }

        public String beforePath() {
            return beforePath;
        }

        public String afterPath() {
            return afterPath;
        }
    }

    private enum DirectoriesModified {
        BLUE_STEEL("Fall 2013/Arpeggio of Blue Steel", "[MD] Arpeggio of Blue Steel 04.mkv"),
        AOT("Summer 2018/Shingeki no Kyojin S3", "[HorribleSubs] Shingeki no Kyojin S3 - 31 [720p].mkv"),
        OVERLORD("Summer 2018/Overlord III", "[HorribleSubs] Overlord III - 02 [720p].mkv"),
        RAILGUN("Unknown/A Certain Scientific Railgun - Toaru Kagaku no Railgun"),
        RAILGUN_S("Unknown/A Certain Scientific Railgun S"),
        UNKNOWN("Unknown", "[HorribleSubs] Shingeki no Kyojin III - 29 [720p].mkv");

        private String path;
        private String[] files;

        private DirectoriesModified(String path, String... files) {
            this.path = path;
            this.files = files;
        }

        public String path() {
            return path;
        }

        public String[] files() {
            return files;
        }
    }
}
