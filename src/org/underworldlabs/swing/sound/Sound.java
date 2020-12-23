package org.underworldlabs.swing.sound;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

public class Sound {
    public static float SAMPLE_RATE = 8000f;


    public static void tone(int note, int duration, int instrument) {
        try {
            Synthesizer synth = MidiSystem.getSynthesizer();
            synth.open();
            MidiChannel[] channels = synth.getChannels();
            channels[0].programChange(instrument);
            channels[0].noteOn(note, 80);
            Thread.sleep(duration);
            channels[0].noteOff(note);
        } catch (MidiUnavailableException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}