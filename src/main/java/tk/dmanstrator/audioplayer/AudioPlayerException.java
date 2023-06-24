package tk.dmanstrator.audioplayer;

/**
 * Exception class in case something goes wrong within the Audio Player.
 */
public class AudioPlayerException extends RuntimeException {

    private static final long serialVersionUID = 598154887450727240L;
    
    public AudioPlayerException() {
        super();
    }

    public AudioPlayerException(String message) {
        super(message);
    }

    public AudioPlayerException(String message, Throwable cause) {
        super(message, cause);
    }

}
