ListenerAlpha {

	var <>loudBuffer ; 
	var <>server, <>synth ;
	var <amp, <freq, <hasFreq ;
	
	*new { arg server, loudRate = 10, freqRate = 11, hasFreqRate = 12 ; 
		^super.new.initListener(server, loudRate, freqRate, hasFreqRate) 
	}

	initListener { arg aServer, lr, fr, hfr ;
		server = aServer ;
		server.boot.doWhenBooted({
			loudBuffer = Buffer.alloc(server,1024,1) ;
			synth = SynthDef(\listener,{
				var soundIn = SoundIn.ar ;
				var fft = FFT(loudBuffer, soundIn) ;
				SendTrig.kr(LFPulse.kr(lr), 100, 
					Loudness.kr(fft))
				;
				SendTrig.kr(LFPulse.kr(fr), 200, Pitch.kr(soundIn, initFreq:0)[0]);
				SendTrig.kr(LFPulse.kr(hfr), 300, Pitch.kr(soundIn)[1]);
			}).play(server)  ;
			this.createResponder 
		})
	}

	createResponder {
		OSCresponder(server.addr,'/tr',{ arg time,responder,msg;
			case 
				{ msg[2] == 100 } { amp = msg[3]/20 }
				{ msg[2] == 200 } { freq = msg[3]  }
				{ msg[2] == 300 } { hasFreq = msg[3] }
			// the OSCresp should set a series of data
			// to be used in a routine
		}).add;
	}
}


