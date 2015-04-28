// to interface dc motors thru rectifier
// generate synths


ToDCMotor  {
	
	*add {
			SynthDef(\adaDC, { arg out = 0,  amp = 1, freq = 500, dur = 0.2 ; 
			Out.ar(out, 
				Pulse.ar(freq)*Line.kr(amp,amp, dur, doneAction:2))
				}).add ;	
	}
	
	*play { arg out = 0, amp = 1, freq = 500, dur = 0.2 ;
		Synth(\adaDC, [\out, out, \amp, amp, \freq, freq, \dur, dur])
	}
	
	*addAndPlay { arg out = 0, amp = 1, freq = 500, dur = 0.2 ; 
			{Out.ar(out, Pulse.ar(freq)*Line.kr(amp,amp,dur, doneAction:2))}.play
	}
	
}

ToLoudspeaker  {
	
	*add {
			SynthDef(\adaLdsp, { arg out = 0, amp = 1, freq = 440, dur = 0.2 ; 
			Out.ar(out, 
				SinOsc.ar(freq)*Line.kr(amp, amp, dur, doneAction:2))
				}).add ;
	}
	
	// single allocation
	*play { arg out = 0, amp = 1, freq = 440, dur = 0.2 ;
		Synth(\adaLdsp, [\out, out, \amp, amp, \freq, freq, \dur, dur])
	}
	
	// temp def
	*addAndPlay { 
		{ arg out = 0, amp = 1, freq = 440, dur = 0.2 ;
			Out.ar(out, SinOsc.ar(freq)*Line.kr(amp, amp, dur, doneAction:2))}.play
	}

}

ToSolenoid {

	*add {
		SynthDef(\adaSol, { arg out = 0, amp = 1, freq = 3, dur = 0.2 ; 
			Out.ar(out,				
				(Pulse.ar(freq)*Line.kr(amp,amp,dur, doneAction:2)).unipolar)
				}).add ;	
	}
	
	*play { arg out = 0, amp = 1, freq = 3, dur = 0.2 ; 
		Synth(\adaSol, [\out, out, \amp, amp, \freq, freq, \dur, dur])
	}
	
	*addAndPlay  { arg out = 0, amp = 1, freq = 3, dur = 0.2 ; 
			{Out.ar(out,Pulse.ar(freq).unipolar*Line.kr(amp,amp,dur, doneAction:2))}.play
	}	

}
