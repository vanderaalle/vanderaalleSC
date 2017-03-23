// an implementation of the spectrum analyzer
// in the Kay Sonogram, as a bank of filters
// with piano-based frequency
// provided with interactive GUIs
// it exports spectral data in a musical format
// AV since 15/11/2016

SonaGraph {

	var ampResp, pitchResp, hasPitchResp ;
	var <>pitch, <>hasPitch, <>amp, <>anRate ;
	var <>buf ;
	var synths, synthRt ;
	var pianoRt ;
	var <>att ;

	*prepare {

		Server.local.waitForBoot{
			SynthDef(\pitch, { arg in, out, freq = 10 ;
				var pt, hpt;
				#pt, hpt = Lag3.kr(Tartini.kr(In.ar(in))) ;
				SendReply.ar(Impulse.ar(freq), '/pitch', values:  pt.cpsmidi.round) ;
				SendReply.ar(Impulse.ar(freq), '/hasPitch', values:  hpt)
			}).add ;

			SynthDef(\bank, {arg in = 0, out = 0, dbGain = 0, freq = 10, rq = 0.001;
				var amp;
				var source = In.ar(in,1) * dbGain.dbamp;
				amp = Array.fill(88, {|i|
					Lag.kr(Amplitude.kr(
						BPF.ar(source, (21+i).midicps, rq))
				).ampdb}) ;
				SendReply.ar(Impulse.ar(freq), '/amp', values:  amp)
			}).add ;

			SynthDef(\sine, {|freq = 440, out = 0, amp = 0.1 |
				Out.ar(out, SinOsc.ar(freq, mul:amp)*EnvGen.kr(Env.perc, doneAction:2))
			}).add ;

			SynthDef(\player, {|buf, start = 0, out = 0, amp = 1 |
				Out.ar(out, PlayBuf.ar(1, buf, startPos:start)*amp)
			}).add ;
		} ;

		SynthDef(\mdaPiano, { |out=0, freq=440, gate=1,
			vel = 100, decay  = 0.5, thresh = 0.01, mul = 0.1|
			var son = MdaPiano.ar(freq, gate, vel, decay,
				release: 0.5, stereo: 0.3, sustain: 0);
			DetectSilence.ar(son, thresh, doneAction:2);
			Out.ar(out, son * mul);
		}).add ;

		SynthDef(\sinePlay, { arg freq, amp;
			Out.ar(0, SinOsc.ar(freq, mul:amp))
		}).add ;
	}

	analyze { |buffer, rate = 10, rq = 0.01|
		var x = Synth(\bank, [\freq, rate, \rq, rq]) ;
		var y = Synth(\pitch, [\freq, rate]) ;
		var anBus = Bus.audio(Server.local, 1) ;
		var z = {Out.ar([anBus,0], PlayBuf.ar(1,buffer, doneAction:2))}.play ;
		amp = [] ; pitch = []; hasPitch = []; anRate = rate ;
		buf = buffer ;
		ampResp = OSCFunc({ |msg|  amp = amp.add(msg[3..]) }, '/amp');
		pitchResp = OSCFunc({ |msg|  pitch = pitch.add(msg[3..].postln) }, '/pitch');
		hasPitchResp = OSCFunc({ |msg|  hasPitch = hasPitch.add(msg[3..].postln) }, '/hasPitch');

		x.set(\in, anBus) ; y.set(\in, anBus) ;
		{
			(buffer.numFrames/Server.local.sampleRate).round.wait;
			ampResp.free ; pitchResp.free ;
			x.free; y.free ;
			//clean up
			// avoid -inf
			amp = amp.collect{ |i|
				if(i.includes(-inf)){
					i = Array.fill(88, {-96})}{i}
			} ;
			// flat and remove strange values
			pitch = pitch.flat.postln.collect{|i|
				case {i < 21} {i = 21 }
				{i > (88+21)} {i = (88+21) }
				{(i >=21)&&(i<=(88+21))} {i}}.postln ;
			hasPitch = hasPitch.flat.postln ;
			amp = amp[..
				(buffer.numFrames/Server.local.sampleRate*rate).asInteger
			] ;
			pitch = pitch[..
				(buffer.numFrames/Server.local.sampleRate*rate).asInteger
			] ;
			hasPitch = hasPitch[..
				(buffer.numFrames/Server.local.sampleRate*rate).asInteger
			] ;
		}.fork
	}

	playBuf {|db = 0| Synth(\player, [\buf, buf, \amp, db.dbamp]) }

	// spectral slice methods are intended for short
	// as they work by averaging data

	// set average pitch of the sound
	calculateAvPitch {|fromBin = 0, toBin|
		toBin = if (toBin.isNil){amp.size-1}{toBin} ;
		^pitch[fromBin..toBin].sum/pitch[fromBin..toBin].size }

	// set average pitchedness of the sound
	calculateAvHasPitch {|fromBin = 0, toBin|
		toBin = if (toBin.isNil){amp.size-1}{toBin} ;
		^hasPitch[fromBin..toBin].sum/hasPitch[fromBin..toBin].size }

	// spectrum is the average db of the db seq
	calculateAvSpectrum {|fromBin = 0, toBin|
		toBin = if (toBin.isNil){amp.size-1}{toBin} ;
		// spectrum sum and average, from 21 to 88+21
		^amp[fromBin..toBin].flop.collect{|i| i.sum/i.size}
	}

	calculateMaximaSpectrumChord {|num = 4, fromBin = 0, toBin|
		^HarmoSpectrum.newFrom(this.calculateAvSpectrum(fromBin,toBin))
		.maximaChord(num);
	}

	calculateOverSpectrumChord {|thresh = -30, fromBin = 0, toBin|
		^HarmoSpectrum.newFrom(this.calculateAvSpectrum(fromBin,toBin))
		.overChord(thresh);
	}

	// average dynamics
	calculateAvDynamics {|fromBin = 0, toBin|
		toBin = if (toBin.isNil){amp.size-1}{toBin} ;
		^this.calculateAvSpectrum(fromBin, toBin).sum/88
	}

	// redirect
	plotAvSpectrum {|fromBin = 0, toBin|
		HarmoSpectrum.newFrom(this.calculateAvSpectrum(fromBin,toBin))
		.plotSpectrum ;
	}

	showSpectrumChord {|num = 4, fromBin = 0, toBin|
		HarmoSpectrum.newFrom(this.calculateAvSpectrum(fromBin,toBin))
		.showSpectrumChord(num) ;
	}


	// calculate spectral boundaries
	// must be called before gui to show them
	calculateAtt {|thresh = 4, binDiffSec = 0.15|
		att = HarmoEvent.newFrom(amp)
		.getEvents(thresh, binDiffSec/anRate.reciprocal) ;
	}

	// this calls harmoEvent.getEvents and split files
	splitIntoFiles {|thresh = 4, binDiffSec = 0.15,
		srcPath, splitDir, attD = 0.005, relD = 0.03|
		HarmoEvent.newFrom(amp)
		.splitIntoFiles(thresh, binDiffSec, srcPath, splitDir, anRate, attD, relD)
	}


	asDatabaseItem {|thresh = -40, num = 4, fromBin = 0, toBin|
		toBin = if (toBin.isNil){amp.size-1}{toBin} ;
		^(
			\overThresh: thresh,
			\maxima: num,
			\name: this.buf.path.basename.splitext[0],
			\rangeString: fromBin.asString++"-"++toBin.asString,
			\ext: this.buf.path.basename.splitext[1],
			\dyn: this.calculateAvDynamics(fromBin, toBin),
			\pitch: this.calculateAvPitch(fromBin, toBin),
			\hasPitch: this.calculateAvHasPitch(fromBin, toBin),
			\spectrum: this.calculateAvSpectrum(fromBin, toBin),
			\path: this.buf.path,
			\overChord:
			this.calculateOverSpectrumChord(thresh,fromBin, toBin),
			\maximaChord:
			this.calculateMaximaSpectrumChord(num, fromBin, toBin)
		)

	}

	makeDatabase {|splitThresh = 4, binDiffSec = 0.15, num = 4|
		var db = [] ;
		this.calculateAtt(splitThresh, binDiffSec) ;
		att.postln[..att.size-2].do{|which,i|
			db = db.add(
				this.asDatabaseItem(splitThresh, num, which, att[i+1]-1)
			)
		} ;
		// tail
		db = db.add(
			this.asDatabaseItem(splitThresh, num, att.last, amp.size-1)
		) ;
		^db
	}

	makeDatabaseAndSplit {|thresh = -40, num = 4,
		binDiffSec = 0.15, splitThresh = 4,
		srcPath, splitDir, attD = 0.005, relD = 0.03,
		writeDb = true
		|
		var db = this.makeDatabase(splitThresh, binDiffSec,num) ;
		this.splitIntoFiles(splitThresh, binDiffSec,
			srcPath, splitDir, attD, relD) ;
		if (writeDb){
			db.writeArchive(
				splitDir++srcPath.basename.splitext[0]++"Db.arc")} ;
		^db
	}


	// converts the amp seq into a chord seq
	sonoToChord {|thresh = -30, fromBin = 0, toBin|
		toBin = if (toBin.isNil){amp.size-1}{toBin} ;
		^
		amp[fromBin..toBin].collect{|slice|
			slice.collect{|i,j|
				[j+21, i]}
		}
		.collect{|slice|
			slice.select{|pa|
				pa[1]>=thresh
			}
		}
	}


	// plays it back immediately
	// if a note is present in the previous block
	// it is not played for sake of intelligibility
	playSonoChord { |thresh = -30, fromBin = 0, toBin, boost = 15|
		if (pianoRt.notNil){pianoRt.stop} ;
		pianoRt = {
			var playing = [] ;
			this.sonoToChord(thresh, fromBin,toBin).do{|chord|
				if (chord.size>0) {
					chord.do{|note|
						if (playing.includes(note[0]).not){
							Synth(\mdaPiano,
								[\freq, note[0].midicps,
									\mul, (note[1]+boost).dbamp]
						) }
					}
				} ;
				playing = chord.collect{|i| i[0]} ;
				anRate.reciprocal.wait
			}
		}.fork ;
	}


	stopSonoChord { if (pianoRt.notNil){ pianoRt.stop } }

	showSonagram {|thresh = -30, fromBin = 0, toBin, res = 72, width = 800, height = 600|
		SonaGraphLily.new
		.showSonagram(this, thresh, fromBin, toBin, res:res, width:width, height:height)
	}

	showSonagramFile {|thresh = -30, fromBin = 0, toBin, ext = "pdf"|
		{SonaGraphLily.new
		.makeSonagram(this, thresh, fromBin, toBin, ext) ;
			3.wait ;
			("open /tmp/sonoLily."++ext).unixCmd }.fork
	}

	makeSonagramFile {|thresh = -30, fromBin = 0, toBin, ext = "pdf"|
		SonaGraphLily.new
		.makeSonagram(this, thresh, fromBin, toBin, ext) ;
	}

	showSonagramChord {|thresh = -30, fromBin = 0, toBin, res = 72, width = 900, height = 600|
		SonaGraphLily.new
		.showSonagramChord(this, thresh, fromBin, toBin, res:res, width:width, height:height, buffer:buf)
	}

	showSonagramChordFile {|thresh = -30, fromBin = 0, toBin, ext = "pdf"|
		{SonaGraphLily.new
		.makeSonagramChord(this, thresh, fromBin, toBin, ext) ;
			3.wait ;
			("open /tmp/sonoChordLily."++ext).unixCmd }.fork
	}

	makeSonagramChordFile {|thresh = -30, fromBin = 0, toBin, ext = "pdf"|
		SonaGraphLily.new
		.makeSonagramChord(this, thresh, fromBin, toBin, ext) ;
	}




	// create a voice from a sequence by grouping
	// i.e. note with att, dur, db
	// asProfile if true->  returns sequence of amps (profile)
	makeVoice {|voice, asProfile = false|
		var v = Pseq(voice).asStream ;
		var next = v.next;
		var actual = next;
		var time = 0, att = 0, dur = 0 ;
		var dyn ;
		var notes = [] ;
		voice.size.do{
			case
			{ (next.isNil).and(actual.isNil) }
			{ actual = next ; next = v.next; time = time+1 }
			// start
			{ (next.isNil.not).and(actual.isNil) }
			{ att = time ;
				dur = 1 ;
				dyn = [next] ;
				actual = next ; next = v.next; time = time+1 }
			// sus
			{ (next.isNil.not).and(actual.isNil.not) }
			{
				dur = dur+1 ;
				dyn = dyn++[next] ;
				actual = next ; next = v.next; time = time+1 }

			// end
			{ (next.isNil).and(actual.isNil.not) }
			{
				if (asProfile)
				{notes = notes.add([dyn, att, dur])}
				{notes = notes.add([(dyn.sum/dyn.size), att, dur]) } ;
				actual = next ; next = v.next; time = time+1 }
		} ;
		^notes
	}

	// create all voices from a sonagram
	makeVoices {|thresh, fromBin = 0, toBin, asProfile = false|
		var voices, vc, vDict = () ;
		toBin = if (toBin.isNil){amp.size-1}{toBin} ;
		voices = amp[fromBin..toBin].flop.collect{|i| i.collect{|i|
			if(i < thresh){nil}{i}
		}} ;
		voices.do{|i,j|
			vc = this.makeVoice(i, asProfile) ;
			if(vc.size>0){vDict[j] = vc}
		} ;
		^vDict
	}


	midiInit {|port, uid|
		MIDIClient.init;
		MIDIClient.destinations.postln ;
	}

	sendMIDIOut {|thresh = -30, fromBin = 0, toBin, port = 0, uid, chan = 0|
		var d, sorted = () ;
		var m = MIDIOut.new(port) ;
		toBin = if (toBin.isNil){amp.size-1}{toBin} ;
		d = this.makeVoices(thresh, fromBin, toBin) ;
		d.keys.collect{|k|
			d[k].do{|e|
				if(sorted[e[1]].notNil){
					sorted[e[1]] = sorted[e[1]].add([k+21, e[2], e[0]])
				}{ sorted[e[1]] = [[k+21, e[2], e[0]]] }
			}
		} ;
		// scheduling routing
		{
			(sorted.keys.asArray.sort.last + 1).do{|i|
				if(sorted.keys.asArray.includes(i)){
					sorted[i].do{|ev|
						{
							m.noteOn
							(chan, ev[0], ev[2].linlin(-96,0, 100,127));
							(ev[1]*anRate.reciprocal).wait ;
							m.noteOff
							(chan, ev[0], ev[2].linlin(-96,0, 100,127));
						}.fork
					}
				} ;
				(anRate.reciprocal).wait ;
			}
		}.fork
	}

	// writes a midi file, can be used directly
	// as it calls makeVoices
	voicesToMidi {|path, voices, thresh, fromBin = 0, toBin|
		var m = SimpleMIDIFile(path);
		// create empty file
		m.init1( 1, 120, "4/4" );
		// init for type 1 (multitrack); 3 tracks, 120bpm, 4/4 measures
		m.timeMode = \seconds;
		// change from default to something useful
		voices = if (voices.isNil)
		{ this.makeVoices(thresh, fromBin, toBin) }
		{ voices } ;
		voices.keys.do{|key,j|
			voices[key].do {|ev|
				[key, ev].postln ;
				m.addNote(
					key+21,
					ev[0].linlin(-96,0, 0, 127).asInteger,
					ev[1]*anRate.reciprocal,
					ev[2]*anRate.reciprocal
			)} ;
		} ;
		m.write
	}


	// synthesize sonagram
	synthesize  { arg thresh ;
		thresh = if (thresh.isNil) { 96.neg }{ thresh } ;
		synthRt = {
			synths = Array.fill(88, {Synth(\sinePlay)}) ;
			amp.do{|bl|
				bl.do{|v, i|
					v = if (v >= thresh){ v }{ -96} ;
					synths[i].set(
						\freq, (21+i).midicps,
						\amp, v.dbamp)
				} ;
				anRate.reciprocal.wait ;
			};
			synths.do{|i| i.free}
		}.fork;
		^synthRt
	}

	// clean out
	stopSynthesize{
		synthRt.stop; synths.do{|i| i.free}
	}

	// Archiving
	writeArchive { |path|
		[amp, pitch, hasPitch, anRate].writeArchive(path)
	}

	readArchive {|path|
		#amp, pitch, hasPitch, anRate = Object.readArchive(path)
	}


	// GUI support
	gui {|buffer, hStep = 2.5, vStep = 6,  labStep = 10, thresh, pitchOn = true|
		var bf = case
		{ buffer.notNil }{ buffer}
		{ buffer.isNil }{ buf } ;
		thresh = if(thresh.isNil){-96}{thresh} ;
		if (bf.isNil){"Please pass a buffer!".postln}{
			SonaGraphGui(this, bf, hStep, vStep).makeGui(thresh,  labStep:labStep, pitchOn:pitchOn)
		}
	}

	// TODO
	// HarmoSpectrum PS support

	// PostScript support (redirect)
	postScript { arg path, buffer, width = 600, height = 200,
		frame = 30,
		xEvery = 1,
		xGridOn = true, yGridOn = true,
		xLabelOn = true, gridCol = Color.red(0.6),
		frameCol = Color.green(0.5),
		cellType = \oval;
		var grCol = [gridCol.red, gridCol.green, gridCol.blue] ;
		var frCol = [frameCol.red, frameCol.green, frameCol.blue] ;
		var bf = case
		{ buffer.notNil }{ buffer}
		{ buffer.isNil }{ buf } ;
		if (bf.isNil){"Please pass a buffer!".postln}{
			this.ps(path, bf, width:width, height:height,
				frame:frame,
				xEvery: xEvery, xGridOn:xGridOn,
				yGridOn:xGridOn, xLabelOn:xLabelOn,
				gridCol: grCol, frameCol:frCol, cellType:cellType)
		}
	}

	ps {
		arg path, buf, width = 600, height = 200, frame = 30,
		xEvery = 2, xLabEvery = 2,
		xGridOn = true, yGridOn = true,
		xLabelOn = true, gridCol = [1, 0, 0], frameCol = [0.2,0.5,0.7],
		cellType = \oval;
		var dur = buf.numFrames/Server.local.sampleRate ;
		var xEv = xEvery.linlin(0, dur, 0, amp.size) ;
		//var xLM = xEv.linlin(0, amp.size, 0, dur);
		PsSonaGraph(amp, path, width:width, height:height,
			frame: frame,
			xEvery: xEv, xGridOn:xGridOn,
			xLabMul: xEvery,
			yGridOn:xGridOn, xLabelOn:xLabelOn,
			gridCol: gridCol, frameCol:frameCol,
			cellType:cellType
		) ;
	}

}


/*

// here we start up server and defs
SonaGraph.prepare ;

// something to analyze, i.e a buffer
~path = Platform.resourceDir +/+ "sounds/a11wlk01.wav";
~sample = Buffer.read(s, ~path).normalize ;

// an istance
a = SonaGraph.new ;
// now analyzing in real-time
a.analyze(~sample,15) ; // rate depends on dur etc

a.gui(hStep:5) ; // directly, if anRate=1 then default hStep 2.,5 fine
a.gui(hStep:8) ; // try

// writing to an archive, log extension is not necesssary
a.writeArchive("/Users/andrea/Desktop/a11.log") ;

// and again
a = SonaGraph.new ;
// read the log, may requires some time
a.readArchive("/Users/andrea/Desktop/a11.log") ;

a.gui(~sample, 5) ; // now we need the pass the sample for playback
// same as:
g = SonaGraphGui(a, ~sample,5).makeGui ;

a.plotAvSpectrum ; // all the spectrum
a.plotAvSpectrum(45,60) ; // only bins from 45 to 60
a.showSpectrumChord ; // a chord representation
// a chord representation of bins 45 to 60, 6 peaks
a.showSpectrumChord(num:6, fromBin:45, toBin:60) ;
a.showSpectrumChord(num:4, fromBin:45, toBin:60) ; // less peaks

a.showSonagram(-30, fromBin:45, toBin:60, width:1200, height:900)
a.showSonagram(-35, fromBin:45, toBin:60, width:1200, height:900)
a.showSonagramFile(-40, 45, 60, "pdf") ; // pdf rendering and display

a.showSonagramChord(-30,width:1200, height:900)

a.showSonagramChord(-30, fromBin:45, toBin:60, width:1200, height:900)
a.showSonagramFile(-40, 45, 60, "pdf") ; // pdf rendering and display



// sinusoidal resynthesis
a.synthesize ; // start synthesis
a.stopSynthesize ; // stop synth routine and free, clean if it's playing

// convert into midi, thresh is mandatory
a.voicesToMidi("~/Desktop/midifiletest.mid",thresh:-40) ;
// selecting bins
a.voicesToMidi("~/Desktop/midifiletest.mid", thresh:-40,fromBin:45,toBin:60)


// spectral event retrieval
a.calculateAtt ; // now you see them
a.gui(~sample, 7) ;
a.splitIntoFiles(thresh:4, binDiffSec: 0.15,
srcPath: ~path, splitDir: "/Users/andrea/musica/regna/fossilia/compMine/erelerichnia/testSplit/")
////////////

// OTHER EXAMPLES, internal tests
// something to analyze, i.e a buffer
~path ="/Users/andrea/musica/recordings/audioRumentario/pezzi/indie_I/fiati/chalumeau or.wav"; ~sample = Buffer.read(s, ~path).normalize ;

// an istance
a = SonaGraph.new;
// now analyzing in real-time
a.analyze(~sample,30) ; // high rate!

// here we calculate and plot the average spectrum
a.plotAvSpectrum
// GUI is interactive, click and you here the sound

// writing to an archive
a.writeArchive("/Users/andrea/Desktop/sonaChal.log")

a.gui(hStep:1, labStep:30) ; // directly, if anRate=1 then default hStep fine

// again
a = SonaGraph.new ;
// read the log, may require some time
a.readArchive("/Users/andrea/Desktop/sonaChal.log") ;

a.gui(~sample, 1, labStep:30) ; // we still need the sample for playback
// same as:
g = SonaGraphGui(a, ~sample,1).makeGui(labStep:30)

// resynthesis
a.synthesize ; // start synthesis
a.stopSynthesize ; // stop synth routine and free

// some data
a.hasPitch
a.pitch
a.calculateAvPitch
a.calculateAvHasPitch

// write to postscript
a.postScript("/Users/andrea/Desktop/sonaChal.pdf") ;

// using spectral data
a.plotAvSpectrum(500,600)
a.showSpectrumChord(4, 500,600)

// here we start up server and defs
SonaGraph.prepare ;

// something to analyze, i.e a buffer
~path ="/Users/andrea/musica/regna/fossilia/compMine/erelerichnia/fragm/snd/vareseOctandreP18M5N[8,9,0,7,11,6].aif"; ~sample = Buffer.read(s, ~path).normalize ;

// an istance
a = SonaGraph.new;
// now analyzing in real-time
a.analyze(~sample,15) ; // high rate!
a.gui(hStep:10) ;
a.showSonagram(-40)


a.plotAvSpectrum ; // takes all
a.showSpectrumChord(7) ;

// something to analyze, i.e a buffer
~path ="/Users/andrea/musica/regna/fossilia/compMine/erelerichnia/fragm/octandreExc2.aif"; ~sample = Buffer.read(s, ~path).normalize ;

// an istance
a = SonaGraph.new;
// now analyzing in real-time
a.analyze(~sample,15) ; // high rate!

a.gui(hStep:6, labStep:5) ;
a.plotAvSpectrum(95,160)
a.plotAvSpectrum(135,150)
a.showSpectrumChord
a.showSpectrumChord(1, 95,160)
a.showSpectrumChord(10, 95,160)

a.showSonagram(-40, width:1200, height:900, res:30)
a.showSonagramChord(-40, fromBin:100, toBin:160)


a.sonoToChord(thresh:-40) ; // here you get chord of peaks above thresh
a.playSonoChord(-40, 119, 150) // play it

v = a.makeVoices(-40) ; // reconstructs events from bins above -40 thresh
// write it down to MIDI
a.voicesToMidi("~/Desktop/midifiletest.mid", thresh:-40, fromBin:104,toBin:150 ) ;





*/
