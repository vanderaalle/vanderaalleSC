OnsetAnalyzer {
	
	var <>path, <>logPath ;	
	var <>bus, <>buf, <>player, <>startTime, <>thresh, <>listener, <>onsets, <>resp ;
		
	// constructor: you can start with an existing graphDict
	*new { arg path, logPath, thresh = 0.5 ; 
		^super.new.initOA(path, logPath, thresh) 	
	}

	initOA { arg aPath, aLogPath, aThresh ;
		path = aPath ;
		logPath = aLogPath ;
		if (logPath.isNil) {logPath = path.split($.)[0]++".on"} ;
		thresh = aThresh ;
		{
		SynthDef(\listener, { arg in, thresh = 0.5;	
				var sig = In.ar(in) ;
				var loc = LocalBuf(1024, 1) ;
				var chain = FFT(loc, sig);
				SendTrig.kr(Onsets.kr(chain, thresh), 999, Loudness.kr(chain));
			}).add ;
		buf = Buffer.read(Server.local, path);
		Server.local.sync ;
		
		bus = Bus.audio(Server.local, buf.numChannels) ;
		Server.local.sync ;
		startTime = thisThread.seconds ; 
		thresh = 0.3 ; 
		listener = Synth(\listener, [\in, bus, \thresh, thresh]).run(false) ; 
		onsets = [] ;
		resp = OSCFunc({ arg msg, time;
			onsets = onsets.add((time-startTime).postln) ;
			{Impulse.ar(1000)*Line.kr(1, 1, 0.1, doneAction:2)}.play
			},'/tr', Server.local.addr); 

		}.fork
	}
	
	go {
		{
		listener.run ;
		player = {Out.ar([bus, 0], PlayBuf.ar(buf.numChannels, buf))}.play ;
		startTime = thisThread.seconds ; 
		(buf.numFrames/Server.local.sampleRate).wait ;
		onsets.writeArchive(logPath.postln) ;
		[listener, player, bus, buf, resp].do{|e| e.free} ;
		"done".postln ;
		}.fork
	}
	
}

/*

o = OnsetAnalyzer("/musica/pseudo/sketchBook/vocalism/timb.wav", thresh:1) ;
o.go
*/
