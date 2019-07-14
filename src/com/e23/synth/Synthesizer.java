package com.e23.synth;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Synthesizer {

    private boolean shouldGenerate;
    private int wavePos;

    private final JFrame frame = new JFrame("Synthesizer");
    private final AudioThred audioThred = new AudioThred(() ->
    {
        if(!shouldGenerate)
        {
            return null;
        }
        short[] s = new short[AudioThred.BUFFER_SIZE];
        for(int i = 0; i < AudioThred.BUFFER_SIZE; ++i)
        {
            s[i] = (short)(Short.MAX_VALUE * Math.sin((2 * Math.PI * 440) / AudioInfo.SAMPLE_RATE * wavePos++));
        }
        return s;
    }
    ) ;

        Synthesizer()
        {
            frame.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    //super.keyPressed(e);
                    if(audioThred.isRunning())
                    {
                        shouldGenerate = true;
                        audioThred.triggerPlayback();
                    }
                    System.out.println("Key is press");
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    //super.keyReleased(e);
                    shouldGenerate = false;
                    System.out.println("Key is release");
                }
            });
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    //super.windowClosing(e);
                    audioThred.close();
                }
            });

            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.setSize(613,357);
            frame.setResizable(false);
            frame.setLayout(null);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }

        public static class AudioInfo
        {
           public static final int SAMPLE_RATE = 44100;
        }
    }
