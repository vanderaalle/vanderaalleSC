// real time analysis extension
+ SonaGraph {

	// here anBus is passed from outside
	analyzeRT { |dur = 4.0, rate = 10, rq = 0.01, anBus|
		//var anBus, x, y, z, buffer, recorder ;
		if (recPath.isNil) {recPath = "/tmp/sonaRec.aiff"} ;
		x = Synth(\bank, [\freq, rate, \rq, rq], addAction: 'addToTail') ;
		y = Synth(\pitch, [\freq, rate],  addAction: 'addToTail') ;
		{
			//anBus = Bus.audio(Server.local, 1) ;
			Server.local.sync ;
			buffer =  Buffer.alloc(Server.local,
				Server.local.sampleRate.nextPowerOfTwo, 1) ;
			// check the path
			buffer.write(recPath,
				"aiff", "int16", 0, 0, true) ; // open buffer
			// we must record in order to have playback
			recorder = Synth.tail(nil, \sonaRecord, [\bufnum, buffer, \in, anBus]);
			Server.local.sync ;
			amp = [] ; pitch = []; hasPitch = []; anRate = rate ;
			buf = buffer ;
			ampResp = OSCFunc({ |msg|  amp = amp.add(msg[3..]) }, '/amp');
			pitchResp = OSCFunc({ |msg|  pitch = pitch.add(msg[3..]) }, '/pitch');
			hasPitchResp = OSCFunc({ |msg|  hasPitch = hasPitch.add(msg[3..]) }, '/hasPitch');
			x.set(\in, anBus) ; y.set(\in, anBus) ;
		}.fork ;
	}

	stopAnalyzingRT {
		{
			// this is fine but we need to stop recording
			//(buffer.numFrames/Server.local.sampleRate).round.wait ;
			ampResp.free ; pitchResp.free ;
			x.free; y.free ; recorder.free ;
			buffer.close ; buffer.free ;
			//clean up
			// avoid -inf
			amp = amp.collect{ |i|
				if(i.includes(-inf)){
					i = Array.fill(88, {-96})}{i}
			} ;
			// if you get strange values
			amp = amp.collect{|p| p.collect{|j| if(j < 96.neg){-96}{j}}} ;
			// flat and remove strange values
			pitch = pitch.flat.postln.collect{|i|
				case {i < 21} {i = 21 }
				{i > (88+21)} {i = (88+21) }
				{(i >=21)&&(i<=(88+21))} {i}}.postln ;
			hasPitch = hasPitch.flat.postln ;
			Server.local.sync ;
			buf = Buffer.read(Server.local,recPath) ;
			Server.local.sync ;
			amp = amp[..
				(buf.numFrames/Server.local.sampleRate*anRate).asInteger
			] ;
			pitch = pitch[..
				(buf.numFrames/Server.local.sampleRate*anRate).asInteger
			] ;
			hasPitch = hasPitch[..
				(buf.numFrames/Server.local.sampleRate*anRate).asInteger
			] ;
		}.fork ;
	}

	// this write the sound analysis buffer to file
	writeBufferToFile { |path|
		buf.write(path, sampleFormat: 'int16') ;
	}
}

// here we start up server and defs
/*
SonaGraph.prepare ;

b = Bus.audio(s, 1) ;
x = {Out.ar([0, b], Mix(SoundIn.ar([0,1])))}.play
// an istance
a = SonaGraph.new ;
// now analyzing in real-time
a.analyzeRT(4,15, anBus:b) ; // rate depends on dur etc
x.free

a.stopAnalyzingRT
a.gui
a.playSonoChord(-30)
*/
