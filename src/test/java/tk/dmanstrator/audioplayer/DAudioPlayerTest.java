package tk.dmanstrator.audioplayer;

import java.io.File;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class DAudioPlayerTest {
    
    @Test
    void testAudio() throws InterruptedException {
        DAudioPlayer.playSound("Kobe.wav", 0, false);
        DAudioPlayer.playSound(new File("src/test/resources/Xbox360-Achievement-Sound.wav"), 1, true);
        DAudioPlayer.playSound("Kobe.wav", 0, true);
        
        Assertions.assertThatExceptionOfType(AudioPlayerException.class)
          .isThrownBy(() -> DAudioPlayer.playSound("does-not-exist.wav"))
          .withMessage("Given file 'does-not-exist.wav' cannot be found.");
        Assertions.assertThatExceptionOfType(AudioPlayerException.class)
            .isThrownBy(() -> DAudioPlayer.playSound("Pikachu_Irontail.mp3"))
            .withMessageContaining("Cannot play 'Pikachu_Irontail.mp3', only .wav files are supported.");
    }
    
}
