VepDSP {

	var <>vepRunner ; // the runner to be processed
	var <>in0, <>in1, <>in2, <>in3, <>in4, <>in5, <>in6, <>in7 ;
	var <>mixer, <>noisePlayer ;
	var <>noiseBuf, <>folder, <>project ; // where we put the background noise 
			// Controls
	var <vol ; 		// the general vol, a multiplier for panning
	var <subVol ; 	// vol for sub
	var <noiseVol ; 	// vol for noise
	
	*new { arg vepRunner ;
	^super.new.initVepDSP(vepRunner, true) 
	}

	*newPaused { arg vepRunner ;
	^super.new.initVepDSP(vepRunner, false) 
	}

	initVepDSP { arg aVepRunner, sync ;
		vepRunner = aVepRunner ;
		vepRunner.addDependant(this) ; // here we register 
		#in0, in1, in2, in3, in4, in5, in6, in7 = vepRunner.outBusArr ;
		folder = vepRunner.folder ;
		project = vepRunner.project ;
		if (sync) {this.syncToServer}
	}

	syncToServer{
		Server.local.waitForBoot({
		{
		SynthDef(\mixer, { arg in0, in1, in2, in3, in4, in5, in6, in7,
				subVol = 0.25, vol = 1, subOut = 9 ;
			// here we scale and forward to public busses 
			Out.ar(0, [
				In.ar(in0),
				In.ar(in1),
				In.ar(in2),
				In.ar(in3),
				In.ar(in4),
				In.ar(in5),
				In.ar(in6),
				In.ar(in7)
				]*vol) ;
			// here we mix and send to sub 
			Out.ar(subOut, Mix.new([
				In.ar(in0),
				In.ar(in1),
				In.ar(in2),
				In.ar(in3),
				In.ar(in4),
				In.ar(in5),
				In.ar(in6),
				In.ar(in7)
				]*subVol*vol) ;				
				)
		}).send(Server.local)  ;

		// we keep it separated because we need to trigger it
//		noiseBuf = Buffer.read(Server.local, folder++"/"++project++"/other/noise.wav") ;
		SynthDef(\noisePlayer, { arg noiseBuf, noiseVol = 0.1, t_trig = 1 ;
			Out.ar(Array.series(8), PlayBuf.ar(1, noiseBuf, trigger:t_trig)*noiseVol)
		}).send(Server.local)  ;

		Server.local.sync ;
		// mixer is always up, it gets what comes from busses 
		mixer = Synth(\mixer, [\in0, in0, \in1, in1, \in2, in2, \in3, in3,  			\in4, in4, \in5, in5, \in6, in6, \in7, in7], addAction:\addToTail) ;
		// noisePlayer is reset as it's up only for poeme
		noisePlayer = Synth.newPaused(\noisePlayer) ;
		this.changed(this, [\initOk]) ;
		}.fork
		})
	}

	loadNoise {
		//loading the buf and setting the player 
		"\n\nLoading noise for Poeme".postln ;
		noiseBuf = Buffer.read(Server.local, folder++"/"++project++"/other/noise.wav") ;
		noisePlayer.set(\noiseBuf, noiseBuf) ;
		}

	// SETTERS
	vol_ { arg newVol ;
		vol = newVol  ;
		mixer.set(\vol, vol) ;
		this.changed(this, [\vol]) ;
	}

	subVol_ { arg newSubVol ;
		subVol = newSubVol  ;
		mixer.set(\subVol, subVol) ;
		this.changed(this, [\subVol]) ;
	}

	noiseVol_ { arg newNoiseVol ;
		noiseVol = newNoiseVol  ;
		noisePlayer.set(\noiseVol, noiseVol) ;
		this.changed(this, [\noiseVol]) ;
	}  
	
	update { arg theChanged, theChanger, more ;
		project = vepRunner.project ;
		case 
		/*
		{ more[0] == \startPlayer && more[1] == 1 }
			{ noisePlayer.set(\t_trig, 1).run }
		{ more[0] == \stopPlayer && more[1] == 1 }
			{ noisePlayer.run(false) }	
		*/
		{ more[0] == \startPlayer && project.asSymbol == \poeme }
			{ noisePlayer.set(\t_trig, 1).run }
		{ more[0] == \stopPlayer  && project.asSymbol == \poeme}
			{ noisePlayer.run(false) }	
		{ more[0] == \setup_audio_player && project.asSymbol == \poeme  }
			{ this.loadNoise }
	}

}