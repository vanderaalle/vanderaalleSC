/*
An analysis utility class
- sends back some data
- all the synths are indepedent and can be paused
- work as a model for some dependants


s.boot ;
u = Bus.audio(Server.local) ; 
x = { Out.ar(u, SoundIn.ar(0))}.play ;
a = Analyzer(u) ;

a.onsets.run(true)
b = EventRecorder2(a,u) ;

Do we need a GUI?

{inf.do{b.bufList[b.bufList.size.rand].play; 0.2.wait}}.fork
*/

Analyzer {

	var <>inBus, <>sig ; 
	var <>pitch, <>onsets, <>detectSilence, <>amplitude, <>amplitudeCont, <>loudness ; 
	var <>centroid, <>flatness ;
	var <>pitchCont, <>centroidCont, <>loudnessCont ; 
	var resp ;

	*new { arg inBus ;
		^super.new.initAnalyzer(inBus)
	}


	initAnalyzer { arg anIn ;
		inBus = anIn ;
	
// TODO: better quantize data, otherwise they always change
	// her we probably need syncing
		// Pitch and HasPitch
		Routine.run {
		var c = Condition.new ;
		SynthDef(\pitchDet, { arg roundFact = 1 ;
			var pt, hpt;
			#pt, hpt = Tartini.kr(In.ar(inBus)) ;
			SendTrig.kr( HPZ1.kr(pt.cpsmidi.round(roundFact)).abs,  100, pt) ;
			SendTrig.kr( HPZ1.kr(hpt.round).abs,  101, hpt) ;
		}).add;
		
		SynthDef(\pitchDetCont, { arg rate = 5 ; // continuous
			var pt, hpt;
			#pt, hpt = Tartini.kr(In.ar(inBus)) ;
			SendTrig.kr( LFPulse.kr(rate),  102, pt) ;
			SendTrig.kr( LFPulse.kr(rate),  103, hpt) ; 
		}).add;
				
		// Amplitude
		SynthDef(\ampDet, { 
			var amp = Amplitude.kr(In.ar(inBus));
			SendTrig.kr( HPZ1.kr(amp).abs,  200, amp) ;
		}).add;
		SynthDef(\ampDetCont, { arg rate = 5 ; // continuous
			var amp = Amplitude.kr(In.ar(inBus));
			SendTrig.kr( LFPulse.kr(rate),  200, amp) ;
		}).add;				

		// Loudness
		SynthDef(\loud, {  
			var loc = LocalBuf(1024, 1) ;
			var sones, chain, input = In.ar(inBus) ;
			chain = FFT(loc, input) ;	
			sones = Loudness.kr(chain) ;
			SendTrig.kr( HPZ1.kr(sones).abs,  300, sones) ;
		}).add ;
		// Loudness, continuous
		SynthDef(\loudCont, {  arg rate = 5 ; // continuous
			var loc = LocalBuf(1024, 1) ;
			var sones, chain, input = In.ar(inBus) ;
			chain = FFT(loc, input) ;	
			sones = Loudness.kr(chain) ;
			SendTrig.kr( LFPulse.kr(rate),  301, sones) ;
		}).add ;

		// SpecCentroid --> brightness
		SynthDef(\centrDet, { arg roundScale = 0.001 ;
			var loc = LocalBuf(2048, 1) ; 
			var centre, chain, input = In.ar(inBus) ;
			chain = FFT(loc, input) ;	
			centre = SpecCentroid.kr(chain) ;
			SendTrig.kr( HPZ1.kr((centre*roundScale).round).abs,  400, centre) ;
		}).add ;
		// SpecCentroid --> brightness, continuous
		SynthDef(\centrDetCont, { arg rate = 5 ;
			var loc = LocalBuf(2048, 1) ; 
			var centre, chain, input = In.ar(inBus) ;
			chain = FFT(loc, input) ;	
			centre = SpecCentroid.kr(chain) ;
			SendTrig.kr( LFPulse.kr(rate),  401, centre) ;
		}).add ;
		
		// SpecFlatness
		SynthDef(\flatDet, {  
			var loc = LocalBuf(2048, 1) ;
			var flat, chain, input = In.ar(inBus) ;
			chain = FFT(loc, input) ;	
			flat = SpecFlatness.kr(chain) ;
			// see help
			flat = LinLin.kr(10 * flat.log;, -45, -1.6, 0, 1).max(-10) ; 
			SendTrig.kr( HPZ1.kr(flat).abs,  500, flat) ;
		}).add ;

		// Onsets
		SynthDef(\onsets, { arg thresh = 0.25 ;
			var loc = LocalBuf(512, 1) ;
			var onsets, chain, input = In.ar(inBus) ;
			chain = FFT(loc, input) ;	
			onsets = Onsets.kr(chain, thresh) ; 
			SendTrig.kr(onsets, 600, 0) ;
		}).add ;
		
		// Silence
		SynthDef(\silence, { arg thresh = 0.005;
			// thresh was: 0.005
			SendTrig.kr(A2K.kr(DetectSilence.ar(In.ar(inBus), thresh, 0.001)), 601, 0) ;
 		}).add ;
		Server.local.sync(c) ;		
		// paused synths
		pitch = Synth.newPaused(\pitchDet, addAction: \addToTail) ;
		pitchCont = Synth.newPaused(\pitchDetCont, addAction: \addToTail) ;
		amplitude = Synth.newPaused(\ampDet, addAction: \addToTail) ; 
		amplitudeCont = Synth.newPaused(\ampDetCont, addAction: \addToTail) ; 
		loudness = Synth.newPaused(\loud, addAction: \addToTail) ;
		loudnessCont = Synth.newPaused(\loudCont, addAction: \addToTail) ;
		centroid = Synth.newPaused(\centrDet, addAction: \addToTail) ;
		centroidCont = Synth.newPaused(\centrDetCont, addAction: \addToTail) ;
		flatness = Synth.newPaused(\flatDet, addAction: \addToTail) ;
		onsets = Synth.newPaused(\onsets, addAction: \addToTail) ;
		detectSilence = Synth.newPaused(\silence, addAction: \addToTail) ;//		// creating the CC
		this.createResponder 
		}
	}

	createResponder {
		resp = OSCresponderNode(Server.local.addr,'/tr',{ arg time,responder,msg;
			case 
				{ msg[2] == 100 } 
					{ this.changed(this, [\pitch, msg[3]]) }
				{ msg[2] == 101 } 
					{ this.changed(this, [\hasPitch, msg[3]]) }
				// the same. Discrimination depends on running synth
				{ msg[2] == 102 } 
					{ this.changed(this, [\pitch, msg[3]]) }
				{ msg[2] == 103 } 
					{ this.changed(this, [\hasPitch, msg[3]]) }
				{ msg[2] == 200 } 
					{ this.changed(this, [\amplitude, msg[3]]) }
				{ msg[2] == 300 } 
					{ this.changed(this, [\loudness, msg[3]]) }
				{ msg[2] == 301 } 
					{ this.changed(this, [\loudness, msg[3]]) }
				{ msg[2] == 400 } 
					{ this.changed(this, [\centroid, msg[3]]) }
				{ msg[2] == 401 } 
					{ this.changed(this, [\centroid, msg[3]]) }
				{ msg[2] == 500 } 
					{ this.changed(this, [\flatness, msg[3]]) }
				{ msg[2] == 600 } 
					{ this.changed(this, [\onset]) }
				{ msg[2] == 601 } 
					{ this.changed(this, [\silence]) }
					
		}).add;
	}

	removeResponder {
		resp.remove
		}
	
	// remove responders?

}
