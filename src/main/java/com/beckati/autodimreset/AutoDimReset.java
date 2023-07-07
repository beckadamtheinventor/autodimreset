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

@Mod(
        modid = AutoDimReset.MOD_ID,
        name = AutoDimReset.MOD_NAME,
        version = AutoDimReset.VERSION
)
public class AutoDimReset {

    public static final String MOD_ID = "autodimreset";
    public static final String MOD_NAME = "Auto Dimension Reset";
    public static final String VERSION = "1.0-SNAPSHOT";


    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MOD_ID)
    public static AutoDimReset INSTANCE;

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {

    }

    /**
     * This is the second initialization event. Register custom recipes
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

    }

    /**
     * This is the final initialization event. Register actions from other mods here
     */
    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {

    }

    @Mod.EventHandler
    public void serverPreload(FMLServerStartingEvent event) {
        String dirName;
        if (event.getServer().isDedicatedServer()) {
            dirName = event.getServer().getFile(event.getServer().getFolderName()).getAbsolutePath().concat(File.separator);
        } else {
            dirName = event.getServer().getFile("saves".concat(File.separator).concat(event.getServer().getFolderName())).getAbsolutePath().concat(File.separator);
        }
        String fileName = dirName.concat("dimresetcfg.txt");
        Clock clock = Clock.systemUTC();
        int configYear = Year.now(ZoneId.of("UTC")).getValue();
        int configMonthOfYear = MonthDay.now(clock).getMonthValue();
        int configDayOfMonth = MonthDay.now(clock).getDayOfMonth();
        int configHourOfDay = LocalTime.now(clock).getHour();
        boolean resetNetherDim = false;
        boolean resetEndDim = false;
        try {
            FileInputStream stream = new FileInputStream(fileName);
            // Just implement my own code to read
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
                } else if (c == 'n' || c == 'N') {
                    resetNetherDim = true;
                } else if (c == 'e' || c == 'E') {
                    resetEndDim = true;
                }
            }
            stream.close();
        } catch (FileNotFoundException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }
/*        System.out.println("Last automatic dimension reset:");
        System.out.print("Year ");
        System.out.print(configYear);
        System.out.print(", Month ");
        System.out.print(configMonthOfYear);
        System.out.print(", Day ");
        System.out.print(configDayOfMonth);
        System.out.print(", Hour ");
        System.out.print(configHourOfDay);
        System.out.println();*/
        int year = Year.now(ZoneId.of("UTC")).getValue();
        int month = MonthDay.now(clock).getMonthValue();
        int day = MonthDay.now(clock).getDayOfMonth();
        int hour = LocalTime.now(clock).getHour();
        int currentTime = year * 366 * 31 * 24 + month * 31 * 24 + day * 24 + hour;
        int configTime = configYear * 366 * 31 * 24 + configMonthOfYear * 31 * 24 + configDayOfMonth * 24 + configHourOfDay;
        if (currentTime >= configTime) {
            System.out.println("Maybe resetting dimensions");
            if (month++ > 12) {
                year++;
                month = 0;
            }
            if (resetNetherDim) {
                System.out.println("Resetting the Nether");
                deleteDir(new File(dirName.concat("DIM-1")));
                new File(dirName.concat("DIM-1")).mkdir();
                new File(dirName.concat("DIM-1").concat(File.separator).concat("data")).mkdir();
            }
            if (resetEndDim) {
                System.out.println("Resetting the End");
                deleteDir(new File(dirName.concat("DIM1")));
                new File(dirName.concat("DIM1")).mkdir();
                new File(dirName.concat("DIM1").concat(File.separator).concat("data")).mkdir();
            }
            try {
                FileOutputStream outputStream = new FileOutputStream(fileName);
                if (resetNetherDim) {
                    outputStream.write('n');
                }
                if (resetEndDim) {
                    outputStream.write('e');
                }
                writeInteger(outputStream, year);
                outputStream.write('y');
                writeInteger(outputStream, month);
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

    // not the most efficient way to write a base-10 integer, but it'll do for now
    void writeInteger(FileOutputStream stream, int num) throws IOException {
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

    boolean deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (! Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f);
                }
            }
        }
        if (!file.delete()) {
            return false;
        }
        return true;
    }
}
