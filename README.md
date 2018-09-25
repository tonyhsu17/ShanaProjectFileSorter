[![Release](https://jitpack.io/v/tonyhsu17/ShanaProjectFileShorter.svg)](https://jitpack.io/#tonyhsu17/ShanaProjectFileShorter)
# Helpful Utilities
Sorts anime files into Season Year/Series based on your followed list on ShanaProject.  
If file's name does not match listing on Shanaproject, renaming the series' folder will allow subsequent files to be moved to appropriate folder. 

Files unable to be sorted into the proper folder will be copied into "Unknown" folder.
A history log file will be generated in destination to keep track of files copied.

## How to use
```
-s <path> | source path
-d <path> | destination path
-u <url>  | shanaproject url
-t        | refresh time
-once     | run a single time (overrides -t)
```
```
ShanaProjectFileOrganizer runner = new ShanaProjectFileOrganizer(args);
runner.run();
```

### Where can I get the latest release?
You can download source and binaries from releases page.

Alternatively you can pull it from the central Jitpack repositories:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
 
<dependency>
    <groupId>com.github.tonyhsu17</groupId>
    <artifactId>ShanaProjectFileShorter</artifactId>
    <version>1.0</version>
</dependency>
```
