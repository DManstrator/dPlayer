package tk.dmanstrator.audioplayer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Audio Player which can play local resources, {@link File}s or general {@link InputStream}s.
 */
public class DAudioPlayer {
    
    private DAudioPlayer()  {
        // hide default constructor
    }
    
    //private static final AudioFormat DEFAULT_FORMAT = new AudioFormat(44_100f, 16, 2, true, true);
    private static final Map<String, FilePackage> PATH_PACKAGE_MAP = new HashMap<>();
    
    /**
     * Plays a local resource file.
     * This will play the sound at 100% volume and will not wait until the sound was fully played.
     * @param localResourcePath Path to the resource file to play.
     * @throws AudioPlayerException In case something goes wrong.
     */
    public static void playSound(String localResourcePath) {
        playSound(localResourcePath, 1);
    }
    
    /**
     * Plays a local resource file.
     * This will not wait until the sound was fully played.
     * @param localResourcePath Path to the resource file to play.
     * @param volume Volume to play the file at.
     * @throws IllegalArgumentException In case the volume is less than 0 or greater than 1.
     * @throws AudioPlayerException In case something goes wrong.
     */
    public static void playSound(String localResourcePath, float volume) {
        playSound(localResourcePath, volume, false);
    }
    
    /**
     * Plays a local resource file.
     * @param localResourcePath Path to the resource file to play.
     * @param volume Volume to play the file at.
     * @param waitUntilFinish Flag telling whether playing the sound should be a blocking call or not.
     * @throws IllegalArgumentException In case the volume is less than 0 or greater than 1.
     * @throws AudioPlayerException In case something goes wrong.
     */
    public static void playSound(String localResourcePath, float volume, boolean waitUntilFinish) {
        checkWaveFile(localResourcePath);
        
        //String pathWithPrefix = "/" + path;  // prefix is important for getting it as a resource via. getClass()
        InputStream resourceStream = ClassLoader.getSystemClassLoader().getResourceAsStream(localResourcePath);
        if (resourceStream == null)  {
            throw new AudioPlayerException("Given file '" + localResourcePath + "' cannot be found.");
        }
        
        playSound(resourceStream, localResourcePath, volume, waitUntilFinish);
    }
    
    /**
     * Plays the given {@link File}.
     * This will play the sound at 100% volume and will not wait until the sound was fully played.
     * @param file File to play.
     * @throws AudioPlayerException In case something goes wrong.
     */
    public static void playSound(File file) {
        playSound(file, 1);
    }
    
    /**
     * Plays the given {@link File}.
     * This will not wait until the sound was fully played.
     * @param file File to play.
     * @param volume Volume to play the file at.
     * @throws IllegalArgumentException In case the volume is less than 0 or greater than 1.
     * @throws AudioPlayerException In case something goes wrong.
     */
    public static void playSound(File file, float volume) {
        playSound(file, volume, false);
    }
    
    /**
     * Plays the given {@link File}.
     * @param file File to play.
     * @param volume Volume to play the file at.
     * @param waitUntilFinish Flag telling whether playing the sound should be a blocking call or not.
     * @throws IllegalArgumentException In case the volume is less than 0 or greater than 1.
     * @throws AudioPlayerException In case something goes wrong.
     */
    public static void playSound(File file, float volume, boolean waitUntilFinish) {
        String filePath = file.getAbsolutePath();
        checkWaveFile(filePath);
        
        try {
            FileInputStream fis = new FileInputStream(file);
            playSound(fis, filePath, volume, waitUntilFinish);
        } catch (FileNotFoundException e) {
            throw new AudioPlayerException("Given file '" + filePath + "' cannot be found.", e);
        }
    }
    
    /**
     * Plays the given {@link InputStream}.
     * This will play the sound at 100% volume and will not wait until the sound was fully played.
     * @param is InputStream to play.
     * @param fileName Filename used for caching and logging.
     * @throws AudioPlayerException In case something goes wrong.
     */
    public static void playSound(InputStream is, String fileName) {
        playSound(is, fileName, 1);
    }
    
    /**
     * Plays the given {@link InputStream}.
     * This will not wait until the sound was fully played.
     * @param is InputStream to play.
     * @param fileName Filename used for caching and logging.
     * @param volume Volume to play the file at.
     * @throws IllegalArgumentException In case the volume is less than 0 or greater than 1.
     * @throws AudioPlayerException In case something goes wrong.
     */
    public static void playSound(InputStream is, String fileName, float volume) {
        playSound(is, fileName, volume, false);
    }
    
    /**
     * Plays the given {@link InputStream}.
     * @param is InputStream to play.
     * @param fileName Filename used for caching and logging.
     * @param volume Volume to play the file at.
     * @param waitUntilFinish Flag telling whether playing the sound should be a blocking call or not.
     * @throws IllegalArgumentException In case the volume is less than 0 or greater than 1.
     * @throws AudioPlayerException In case something goes wrong.
     */
    public static void playSound(InputStream is, String fileName,
                                 float volume, boolean waitUntilFinish) {
        checkWaveFile(fileName);
        
        FilePackage filePackage = PATH_PACKAGE_MAP.computeIfAbsent(fileName,
                                                                   key -> getDataFromStream(is, key));
        
        AudioFormat audioFormat = filePackage.getAudioFormat();
        int bufferSize = filePackage.getBufferSize();

        try {
            Clip clip = AudioSystem.getClip();
            
            clip.open(audioFormat, filePackage.getBytes(), 0, bufferSize);
            
            // From https://stackoverflow.com/a/40698149.
            if (volume < 0f || volume > 1f)  {
                throw new IllegalArgumentException("Volume " + volume + "exceeds valid range.");
            }
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);        
            gainControl.setValue(20f * (float) Math.log10(volume));
            
            clip.start();
            
            if (waitUntilFinish)  {
                // From https://stackoverflow.com/a/46724857.
                CountDownLatch syncLatch = new CountDownLatch(1);
                
                clip.addLineListener(e -> {
                  if (e.getType() == LineEvent.Type.STOP) {
                    syncLatch.countDown();
                  }
                });
                
                try {
                    syncLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            throw new AudioPlayerException("Cannot play audio for '" + fileName + "'.", e);
        }
    }
    
    private static void checkWaveFile(String path)  {
     // Only .wav files are supported by default, see https://stackoverflow.com/a/31850267.
        if (!path.endsWith(".wav"))  {
            throw new AudioPlayerException("Cannot play '" + path + "', only .wav files are supported. "
                    + "Please convert your audio into a .wav file first.");
        }
    }

    private static FilePackage getDataFromStream(InputStream is, String fileName) {       
        try {
            // Make sure it can be fully played, see https://stackoverflow.com/a/38339938.
            BufferedInputStream bis = new BufferedInputStream(is);
            AudioInputStream ais = AudioSystem.getAudioInputStream(bis);
            
            return new FilePackage(ais);
        } catch (UnsupportedAudioFileException | IOException e) {
            String errorMsg = String.format("Failed to read the existing audio file at '%s'.", fileName);
            throw new AudioPlayerException(errorMsg, e);
        }
    }

}