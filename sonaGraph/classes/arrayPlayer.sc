// a player for easy seq playback

+ SequenceableCollection {
	/// in case it's not loaded
	*prepare {
		Server.local.waitForBoot{
			SynthDef(\mdaPiano, { |out=0, freq=440, gate=1,
				vel = 100, decay  = 0.5, thresh = 0.01, mul = 0.1|
				var son = MdaPiano.ar(freq, gate, vel, decay,
					release: 0.5, stereo: 0.3, sustain: 0);
				DetectSilence.ar(son, thresh, doneAction:2);
				Out.ar(out, son * mul);
			}).add
		}
	}

	play {|seq = true, transp = 0, time = 0.5, def = \mdaPiano|
		{
			this.do{|i|
			Synth(def, [\freq, (i+transp).midicps]) ;
			// time applies only if seq
			if(seq){ time.wait }
			}
		}.fork
	}

}

