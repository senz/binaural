package com.github.senz.binaural;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

public class Binaural implements BeatsEngine {

	private final int SAMPLE_RATE = 44100;
	private int sampleCount;
	private boolean doRelease;
	private AudioTrack mAudio;
	private boolean isPlaying;

	public Binaural(float frequency, float isoBeat) {
		int amplitudeMax = Helpers.getAdjustedAmplitudeMax(frequency);

		float freqLeft = frequency - (isoBeat/2);
		float freqRight = frequency + (isoBeat/2);

		//period of the sine waves
		int sCountLeft = (int) ((float) SAMPLE_RATE / freqLeft);
		int sCountRight = (int) ((float) SAMPLE_RATE / freqRight);

		sampleCount = Helpers.getLCM(sCountLeft, sCountRight) * 2;
		int buffSize = sampleCount * 4;

		mAudio = new AudioTrack.Builder()
				.setAudioAttributes(new AudioAttributes.Builder()
						.setUsage(AudioAttributes.USAGE_MEDIA)
						.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
						.build())
				.setAudioFormat(new AudioFormat.Builder()
						.setEncoding(AudioFormat.ENCODING_PCM_16BIT)
						.setSampleRate(SAMPLE_RATE)
						.setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
						.build())
				.setBufferSizeInBytes(buffSize)
				.setTransferMode(AudioTrack.MODE_STATIC)
				.build();

		short samples[] = new short[sampleCount];
		int amplitude = amplitudeMax;
		double twopi = 8. * Math.atan(1.);
		double leftPhase = 0.0;
		double rightPhase = 0.0;

		for (int i = 0; i < sampleCount; i = i + 2) {

			samples[i] = (short) (amplitude * Math.sin(leftPhase));
			samples[i + 1] = (short) (amplitude * Math.sin(rightPhase));

			if (i/2 % sCountLeft == 0) {
				leftPhase = 0.0;
			}
			leftPhase += twopi * freqLeft / SAMPLE_RATE;
			if (i/2 % sCountRight == 0) {
				rightPhase = 0.0;
			}
			rightPhase += twopi * freqRight / SAMPLE_RATE;
		}
		mAudio.write(samples, 0, sampleCount);
		mAudio.setVolume(0f);
		Helpers.napThread();
	}

	public void release() {
		doRelease = true;
		stop();
	}

	public void start() {
		mAudio.reloadStaticData();
		mAudio.setLoopPoints(0, sampleCount / 2, -1);
		isPlaying = true;
		mAudio.play();
		Helpers.napThread();
		mAudio.setVolume(1f);
	}

	public void stop() {

		mAudio.setVolume(0f);
		Helpers.napThread();
		mAudio.stop();
		isPlaying = false;
		if (doRelease) {
			mAudio.flush();
			mAudio.release();
		}

	}

	public boolean getIsPlaying() { return isPlaying; }


}
