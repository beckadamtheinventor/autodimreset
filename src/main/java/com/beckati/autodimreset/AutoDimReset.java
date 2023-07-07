package com.beckati.autodimreset;

import net.minecraft.client.main.GameConfiguration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.registries.GameData;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

@Mod(
        modid = AutoDimReset.MOD_ID,
        name = AutoDimReset.MOD_NAME,
        version = AutoDimReset.VERSION,
        serverSideOnly = true,
        acceptableRemoteVersions = "*"
)
public class AutoDimReset {

    public static final String MOD_ID = "autodimreset";
    public static final String MOD_NAME = "Auto Dimension Reset";
    public static final String VERSION = "1.1-SNAPSHOT";


    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MOD_ID)
    public static AutoDimReset INSTANCE;

    @Mod.EventHandler
    public void serverPreload(FMLServerStartingEvent event) {
        String dirName;
        if (event.getServer().isDedicatedServer()) {
            dirName = event.getServer().getFile(event.getServer().getFolderName()).getAbsolutePath().concat(File.separator);
        } else {
            dirName = event.getServer().getFile("saves".concat(File.separator).concat(event.getServer().getFolderName())).getAbsolutePath().concat(File.separator);
        }
        List<String> removeDirs = new ArrayList<>();
        String fileName = dirName.concat("last_dim_reset.txt");
        String fileName2 = event.getServer().getFile("config").getName().concat(File.separator).concat("auto_dim_reset.txt");
        String fileName3 = event.getServer().getFile("config").getName().concat(File.separator).concat("auto_dim_reset_time.txt");
        Clock clock = Clock.systemUTC();
        boolean needInit = false;
        boolean ly = Year.now(ZoneId.of("UTC")).isLeap();
        int configYear = Year.now(ZoneId.of("UTC")).getValue();
        int configMonthOfYear = MonthDay.now(clock).getMonthValue();
        int configDayOfMonth = MonthDay.now(clock).getDayOfMonth();
        int configHourOfDay = LocalTime.now(clock).getHour();
        try {
            FileInputStream stream = new FileInputStream(fileName);
            // Just implement my own code to read timestamp
            int val = 0;
            while (stream.available() > 0) {
                int c = stream.read();
                if (c >= '0' && c <= '9') {
                    val = val * 10 + c - '0';
                } else if (c == 'y' || c == 'Y') {
                    configYear = val;
                    val = 0;
                } else if (c == 'm' || c == 'M') {
                    configMonthOfYear = val;
                    val = 0;
                } else if (c == 'd' || c == 'D') {
                    configDayOfMonth = val;
                    val = 0;
                } else if (c == 'h' || c == 'H') {
                    configHourOfDay = val;
                    val = 0;
                }
            }
            stream.close();
        } catch (FileNotFoundException ignored) {
            needInit = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            FileInputStream stream = new FileInputStream(fileName2);
            char[] buffer = new char[256];
            int bufferIndex = 0;
            while (stream.available() > 0) {
                int c = stream.read();
                if (c == '\n') {
                    if (bufferIndex > 0) {
                        removeDirs.add(String.copyValueOf(buffer, 0, bufferIndex));
                    }
                    bufferIndex = 0;
                } else if (c >= 0x20 && c <= 0x7E && c != '\\' && c != '/') {
                    buffer[bufferIndex++] = (char)c;
                }
            }
            if (bufferIndex > 0) {
                removeDirs.add(String.copyValueOf(buffer, 0, bufferIndex));
            }
            stream.close();
        } catch (FileNotFoundException ignored) {
            try {
                FileOutputStream stream = new FileOutputStream(fileName2);
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.printf("Last automatic dimension reset:%nYear %d, Month %d, Day %d, Hour %d%n",
                configYear, configMonthOfYear, configDayOfMonth, configHourOfDay);
        int year = 0, month = 0, day = 0, hour = 0;
        try {
            FileInputStream stream = new FileInputStream(fileName3);
            // Just implement my own code to read timestamp
            int val = 0;
            while (stream.available() > 0) {
                int c = stream.read();
                if (c >= '0' && c <= '9') {
                    val = val * 10 + c - '0';
                } else if (c == 'y' || c == 'Y') {
                    year = val;
                    val = 0;
                } else if (c == 'm' || c == 'M') {
                    month = val;
                    val = 0;
                } else if (c == 'd' || c == 'D') {
                    day = val;
                    val = 0;
                } else if (c == 'h' || c == 'H') {
                    hour = val;
                    val = 0;
                }
            }
            stream.close();
        } catch (FileNotFoundException ignored) {
            month = 1;
            try {
                FileOutputStream stream = new FileOutputStream(fileName3);
                writeInteger(stream, year);
                stream.write('y');
                writeInteger(stream, month);
                stream.write('m');
                writeInteger(stream, day);
                stream.write('d');
                writeInteger(stream, hour);
                stream.write('h');
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        hour += configHourOfDay;
        if (hour >= 24) {
            hour = 0;
            day++;
        }
        day += configDayOfMonth;
        if (day > MonthDay.now().getMonth().length(ly)) {
            day = 1;
            month++;
        }
        month += configMonthOfYear;
        if (month > 12) {
            month = 1;
            year++;
        }
        year += configYear;
        System.out.printf("Next automatic dimension reset:%nYear %d, Month %d, Day %d, Hour %d%n",
                year, month, day, hour);
        int currentHour = LocalTime.now(clock).getHour();
        int currentDay = MonthDay.now(clock).getDayOfMonth();
        int currentMonth = MonthDay.now(clock).getMonthValue();
        int currentYear = Year.now(ZoneId.of("UTC")).getValue();
        int currentTime = currentYear * 366 * 31 * 24 + currentMonth * 31 * 24 + currentDay * 24 + currentHour;
        int configTime = year * 366 * 31 * 24 + month * 31 * 24 + day * 24 + hour;
        if (currentTime >= configTime || needInit) {
            if (!needInit) {
                System.out.println("Resetting dimensions listed in config/auto_dim_reset.txt");
                for (String dim : removeDirs) {
                    System.out.printf("Resetting Dimension %s%n", dim);
                    if (!deleteDir(new File(dirName.concat(dim)))) {
                        System.out.println("Failed to reset dimension.");
                    }
                    new File(dirName.concat(dim)).mkdir();
                    new File(dirName.concat(dim).concat(File.separator).concat("data")).mkdir();
                }
            }
            try {
                FileOutputStream outputStream = new FileOutputStream(fileName);
                writeInteger(outputStream, currentYear);
                outputStream.write('y');
                writeInteger(outputStream, currentMonth);
                outputStream.write('m');
                writeInteger(outputStream, day);
                outputStream.write('d');
                writeInteger(outputStream, hour);
                outputStream.write('h');
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // probably not the most efficient way to write a base-10 integer, but it'll do for now
    void writeInteger(FileOutputStream stream, int num) throws IOException {
        if (num == 0) {
            stream.write('0');
        } else {
            int div = (int) Math.pow(10, Math.floor(Math.log10(Integer.MAX_VALUE)) - 1);
            int mod = div * 10;
            while (div > 0) {
                if (num >= div) {
                    stream.write('0' + ((num % mod) / div));
                }
                div /= 10;
                mod /= 10;
            }
        }
    }

    boolean deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (! Files.isSymbolicLink(f.toPath())) {
                    if (!deleteDir(f)) {
                        return false;
                    }
                }
            }
        }
        try {
            Files.delete(file.toPath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
