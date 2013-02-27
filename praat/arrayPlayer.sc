ArrayPlayer {
	
	var <>s, <>r, <>arr;
	
	*new { arg anArr, dur = 0.5, e, factor = 1 ; ^super.new.init(anArr, dur, e, factor) ;
	}	

// you can pass e = Env.perc. Works fine

	init { arg anArr, dur, e, factor ;  
		var att = 0.1*dur*factor, sus = 0.6*dur*factor, rel = 0.3*dur*factor, lev = 0.6 ;
		e = e ? Env.perc;
		arr = anArr ; 
		if (arr[0].isArray.not, { arr = arr.clump(1) }) ;
				// you can pass sequences
		if (dur.isArray.not, { dur = Array.fill(arr.size, {dur})} ) ;
				// you can pass a seq of durs or just one dur
		dur = dur*factor ;
		s = Server.local.boot ; 
		// sinusoide
		s.doWhenBooted(
		SynthDef("sine", { arg out = 0, freq = 100,  amp = 0.1 ;  
			Out.ar(out, 
				SinOsc.ar(freq, 0, amp)*
				EnvGen.kr(e, doneAction:2)
				)
			}).send(s) ;
			
			r = Routine({ 
				arr.do({ arg frame, index;
					// frame.postln ;
					frame.do({ arg freq ;
						// freq = freq.cpsmidi.round.midicps ; // rounded to semitone
						Synth("sine", [\freq, freq]);
						}) ;
					dur[index].wait;
				}) ;
			}) ;
			)
	}



	play { r.reset ; SystemClock.play(r) ; }

}
