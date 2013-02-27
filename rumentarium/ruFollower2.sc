// follows pitches continuously in input by associating each quarter tone a rumentarium sound body.

RuFollower2 {

	var <>analyzer, <>array, <>flagArray, <>evtDur, <tempo ;
	var <>pitch, <>loud ;
	var <>ruMaster ;
	var <>min, <>max ;
	var <>task ;
	
	
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
		task = Task({inf.do{
			this.generateEvent ;
			(evtDur+0.001).wait ;
			}})
	}
	
	tempo_ { arg bpm = 60 ; evtDur = 60/bpm }
	
	play { task.play }
	pause { task.pause ; ruMaster.zero ;}
	
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
		// pity, this worked fine
//		{ more[0] == \onset }
//			 { 	this.generateEvent }
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
~bsj = {Out.ar(~inBus, SoundIn.ar)}.play ;

~analyzer = Analyzer(~inBus) ; // we get the inbus
(
~analyzer.onsets.run ;
~analyzer.pitchCont.run ;
~analyzer.amplitudeCont.run ;
)
// k = RuMaster([r,o,p,q]) ;

t = RuFollower2(analyzer: ~analyzer, ruMaster:k)
t.min_(0.0) ;
t.tempo_(120) ; // this is tempo

t.play
*/

