package tk.dmanstrator.audioplayer;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

/**
 * POJO containing all information about an {@link AudioInputStream}.
 */
public class FilePackage {
    
    private final AudioFormat audioFormat;
    private final byte[] bytes;
    private final int bufferSize;
    private final AudioInputStream audioStream;
    
    /**
     * Constructs a new FilePackage for the given {@link AudioInputStream}.
     * @param audioStream Audio Stream to read and get information from.
     * @throws IOException In case the InputStream cannot be read.
     */
    public FilePackage(AudioInputStream audioStream) throws IOException  {
        AudioFormat af = audioStream.getFormat();
        int size = (int) (af.getFrameSize() * audioStream.getFrameLength());
        byte[] audio = new byte[size];
        audioStream.read(audio, 0, size);
        
        this.audioFormat = af;
        this.bytes = audio;
        this.bufferSize = size;
        this.audioStream = audioStream;
    }

    /**
     * Returns the {@link AudioFormat}.
     * @return The {@link AudioFormat}.
     */
    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    /**
     * Returns the bytes of the Stream.
     * @return The bytes of the Stream.
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Returns the buffer size. This is {@link AudioFormat#getFrameSize()}
     * multiplied with {@link AudioInputStream#getFrameLength()}.
     * @return The buffer size.
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Returns the hold {@link AudioInputStream}.
     * @return The hold {@link AudioInputStream}.
     */
    public AudioInputStream getAudioStream() {
        return audioStream;
    }
    
}