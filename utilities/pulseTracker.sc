/*
Idea here is to detect onsets and to infer a tempo
for the beat.

*/

PulseTracker {

	var <>analyzer, <>actual, <>start, <>tempo ;
	var <>quant, <>min, <>max ;

	*new { arg analyzer ;
		^super.new.initPulseTracker(analyzer)
	}

	initPulseTracker{ arg anAnalyzer ;
		analyzer = anAnalyzer ;
		analyzer.addDependant(this) ;
		// first tempo will be surely wrong
		start = thisThread.seconds ;
		actual = thisThread.seconds+1 ;
		quant = 1 ;
		tempo = 60 ;
		// we assume 60 bpm as ref
		min = 30 ;
		max = 60*16 ;
		}	

	update { arg theChanged, theChanger, more ;
		if ( more[0] == \onset  )
		 { this.count}
	}

	count {
		actual  = thisThread.seconds - start ;
		tempo = (60/actual).round(quant).min(max).max(min) ;
//		tempo.postln ;
		start = thisThread.seconds ;
		this.changed(this, [\tempo, tempo])
		}
	

}

Tester {
	
	var <>pulseTracker ;

	*new { arg pulseTracker ;
		^super.new.initTester(pulseTracker)
	}

	initTester { arg aPulseTracker ;
		pulseTracker = aPulseTracker ;
		pulseTracker.addDependant(this) ;
		// first tempo will be surely wrong
		{ inf.do{
//			(\freq: Array.series(24, 60).midicps.choose).play ;
			(\freq: ([0, 2,3,5,6,8,10]+60).midicps.choose).play ;

			(60/pulseTracker.tempo).wait ;
			}
		}.fork ;

	}	


}



Tester2 {
	
	var <>pulseTracker, <>analyzer, <>pitch ;

	*new { arg pulseTracker, analyzer ;
		^super.new.initTester(pulseTracker, analyzer)
	}

	initTester { arg aPulseTracker, anAnalyzer ;
		pulseTracker = aPulseTracker ;
		pulseTracker.addDependant(this) ;
		analyzer = anAnalyzer ;
		analyzer.addDependant(this) ;
		pitch = 440 ;
		// first tempo will be surely wrong
		{ inf.do{
			(\freq: pitch).play ;
			(60/pulseTracker.tempo).wait ;
			}
		}.fork ;

	}	

	update { arg theChanged, theChanger, more ;
		if ( more[0] == \pitch  )
		 { pitch = more[1].cpsmidi.round ;
			pitch = [pitch, pitch+3, pitch+4, pitch+7, pitch+10].choose.midicps
			  }
	}


}


/*


s.reboot ;

~inBus = Bus.audio(Server.local, 1) ;  // where we write in order to analyze

{Out.ar(~inBus, SoundIn.ar)}.play

~analyzer = Analyzer(~inBus) ; // we get the inbus

~analyzer.onsets.run
~analyzer.pitch.run


~tracker = PulseTracker(~analyzer) 

t = Tester(~tracker)

t = Tester2(~tracker, ~analyzer)

*/