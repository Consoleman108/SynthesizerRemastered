package com.e23.synth;

import com.e23.synth.utils.Utils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.system.CallbackI;

import java.util.function.Supplier;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;

public class AudioThred extends Thread
{
    static final int BUFFER_SIZE = 512;
    static final int BUFFER_COUNT = 8;

    private final Supplier<short[]> bufferSupplier;
    private final int[] buffers = new int[BUFFER_COUNT];
    private final long  device = alcOpenDevice(alcGetString(0,ALC_DEFAULT_DEVICE_SPECIFIER));
    private final long  context = alcCreateContext(device,new int[1]);
    private final int   source;

    private int bufferIndex;
    private boolean closed;
    private boolean running;

    AudioThred(Supplier<short[]> bufferSupplier)
    {
        System.out.println("device = "+ device);
        System.out.println("context = " + context);

        this.bufferSupplier = bufferSupplier;
        alcMakeContextCurrent(context);
        AL.createCapabilities(ALC.createCapabilities(device));
        source = alGenSources();
        System.out.println("sourse = " + source);

        for (int i = 0; i < BUFFER_COUNT; i++)
        {
            //buffer samples
            bufferSamples(new short[0]);
        }

        alSourcePlay(source);
        //catch internel exceptions
        catchInternalException();
        start();
        System.out.println("AudioThred start");
    }

    boolean isRunning()
    {
        return running;
    }

    @Override
    public synchronized void run()
    {
        System.out.println("Close = " + closed);
        System.out.println("running = " + running);
        while (!closed) {
            System.out.println("In while loop close");
            while (!running) {
                System.out.println("In while loop running");
                /*try
                {
                    wait();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }*/
                Utils.handleProcedure(this::wait, false);
                }
                int processedBuff = alGetSourcei(source, AL_BUFFERS_PROCESSED);
                for (int i = 0; i < processedBuff; ++i) {
                    short[] samples = bufferSupplier.get();
                    System.out.println("samples = " + samples);
                    if (samples == null) {
                        running = false;
                        break;
                    }
                    //short samples
                    //if (samples == null)
                    //running = false
                    //brake
                    alDeleteBuffers(alSourceUnqueueBuffers(source));
                    buffers[bufferIndex] = alGenBuffers();
                    //bufferSamples(samples);
                    bufferSamples(samples);
                }
                if (alGetSourcei(source, AL_SOURCE_STATE) != AL_PLAYING) {
                    alSourcePlay(source);
                }
                catchInternalException();
            }
            alDeleteSources(source);
            alDeleteBuffers(buffers);
            alcDestroyContext(context);
            alcCloseDevice(device);
    }

    synchronized void triggerPlayback()
    {
        running = true;
        notify();
        System.out.println("AudioThread triggerPlayback");
    }

    void close()
    {
        closed = true;
        //break out of the loop
        triggerPlayback();
    }

    private void bufferSamples(short[] samples)
    {
        int buf = buffers[bufferIndex++];
        alBufferData(buf, AL_FORMAT_MONO16, samples, Synthesizer.AudioInfo.SAMPLE_RATE);
        alSourceQueueBuffers(source, buf);
        bufferIndex %= BUFFER_COUNT; // 0 % 8 = 0;
        // 1 % 8 = 1;
        // 2 % 8 = 2;
        // 3 % 8 = 3;
        // ...
        // 8 % 8 = 0.
        System.out.println("BufferSamples");
    }

    private void catchInternalException()
    {
        int err = alcGetError(device);
        if (err != ALC_NO_ERROR)
        {
            throw new OpenALException(err);
        }
    }
}
