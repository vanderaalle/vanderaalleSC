// follows pitches in input by associating each quarter tone a rumentarium sound body.

TrilobiteFollower {

	var <>analyzer, <>array, <>flagArray, <>evtDur ;
	var <>pitch, <>loud ;
	var <>ruMaster ;
	var <>min, <>max ;
	
	*new { arg analyzer, array, evtDur = 1, ruMaster ;
		^super.new.initRuFoll(analyzer, array, evtDur, ruMaster) ;
	}
	
	initRuFoll { arg anAnalyzer, anArray, anEvtDur, aRuMaster ;
		array = anArray ;
		analyzer = anAnalyzer ;
		analyzer.addDependant(this) ;
		evtDur = anEvtDur ;
		if (array.isNil) { array = Array.series(24,1) } ;
		flagArray = Array.fill(24, {0}) ;
		ruMaster = aRuMaster ;
		// better init
		pitch = 60 ;
		loud = 40 ;
		min = 0; max = 1 ;
	}
	
	generateEvent {
		var id = (pitch.round(0.5).postln%12*2).asInteger ;
		var port = array[id] ;
		var flag = flagArray[id] ;
//		("port:"+port+", id: "+id).postln ;
		if (flagArray[id] == 0)
			{ 	{
				flagArray[id] = 1 ;
				ruMaster.set(port, loud.linlin(-90, 0, min, max).postln) ;
				evtDur.wait ;
				ruMaster.set(port, 0) ;
				flagArray[id]  = 0 ;
				}.fork
			}
		
		}
		
	update { arg theChanged, theChanger, more ;
		//more.postln ;
		case 
		{ more[0] == \onset }
			 { "here".postln ;
				this.generateEvent 
			 }
		{ more[0] == \pitch }
			{ pitch = more[1] }		
		{ more[0] == \amplitude }
			{ loud = more[1].ampdb.round }		

	}
	
}


/*

s.reboot ;

~inBus = Bus.audio(Server.local, 1) ;  // where we write in order to analyze

// 
~sx = {Out.ar(~inBus, SoundIn.ar(2)*MouseX.kr(0,30))}.play ;
~dx = {Out.ar(~inBus, SoundIn.ar(3)*MouseX.kr(0,30))}.play ;

~analyzer = Analyzer(~inBus) ; // we get the inbus
(
~analyzer.onsets.run ;
~analyzer.pitchCont.run ;
~analyzer.amplitudeCont.run ;
)
// k = RuMaster([r,o,p,q]) ;

t = RuFollower(analyzer: ~analyzer, ruMaster:k)
t.min_(0.0)
t.evtDur_(1.5)
*/

