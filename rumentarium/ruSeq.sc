// a sequencer, sort of, tempo depends on pulse rec on input

RuSeq {

	var <>ruMaster, <>analyzer ;
	var <>seq, <tempo, <>vol, amp ;
	var <>taskArr, <>clock ;
	var <maximum ;
	var <>chain, events ;
	var <>measure, <>howMany ;
	var <>maxv, <>minv ;
	
	
	*new { arg tempo = 60, chain = 48, analyzer, ruMaster ;
		^super.new.initRuFoll(tempo, chain, analyzer, ruMaster) ;
	}
	
	initRuFoll { arg aTempo, aChain, anAnalyzer, aRuMaster ;
		tempo = aTempo ;
		ruMaster = aRuMaster ;	
		chain = aChain ;
		analyzer = anAnalyzer ;
		analyzer.addDependant(this) ;
		vol = 1 ;
		taskArr = [] ;
		clock = TempoClock.new ;
		this.tempo_(tempo) ;
		maximum = 1 ;
		events = 0 ;
		measure = 4; howMany = 4 ;
		maxv = 1; minv = 0.5 ;
	}
	
	// task interface
	play { taskArr.do{|t| t.play(clock)} }
	pause { taskArr.do{|t| t.pause} ; ruMaster.zero ; }
	
	
	tempo_{|t| clock.tempo_(t/60) }
	maximum_{|m| maximum = tempo/60*m}
	
	
	generateSeq { arg which = 0;
		// reset all		
		this.pause ;
		taskArr = [] ;
		// new parallel tasks
		if (which.isNil)
			{ Array.series(24,1).scramble[..howMany-1].do{|id| this.createTask(measure, id) } }
			{ Array.series(6,which*6+1).scramble[..howMany-1]
					.do{|id| this.createTask(measure, id) } } ;
		this.play ;
	}
	
	createTask { arg measure, id ;
		var minimum = tempo/60 ;
		var available = measure - minimum ;
		var start = rrand(0, available) ;
		var end = rrand(start+minimum, (start+maximum).clip(start+minimum, measure)) ;
		var dur = end - start ;
		var rest = measure - end ;
		var x ;
		[minimum, available, start, end, dur, rest].postln ;
		taskArr = taskArr.add(Task({inf.do{ 
			start.wait ; 
			ruMaster.set(id, vol) ;
//		id.postln ;x = {SinOsc.ar((id+60).midicps, mul:vol)*Line.kr(1,1,dur, doneAction:2)}.play ;
			dur.wait ;
			ruMaster.set(id, 0) ;
			rest.wait ;
			}
		})) ;
		
		}
	
	
	addEvent {
		events = events +1 ;		
		if (events >= chain ) 
			{
			measure = rrand(4, 16) ; howMany = rrand(4, 32) ;
			vol = rrand(0.5, 1) ;
			tempo = rrand(60, 240) ; 
			[measure, howMany, vol, tempo].postln ;
			"NEW Pattern".postln ;
			 events = 0;
			 this.generateSeq([nil, 0,1,2,3].choose) }
		}
		
	update { arg theChanged, theChanger, more ;
		case
			{ more[0] == \onset }
		 		{ this.addEvent ; (chain-events).postln ; }
/*
			{ more[0] == \amplitude }
				{ amp = more[1].ampdb.linlin(-90, 0, minv, maxv) }
*/		
		
	}
	
}


/* 

s.reboot ;

~inBus = Bus.audio(Server.local, 1) ;  // where we write in order to analyze

{Out.ar(~inBus, SoundIn.ar)}.play ;
{Out.ar(0, SoundIn.ar)}.play ; // to out

~analyzer = Analyzer(~inBus) ; // we get the inbus
(
~analyzer.onsets.run ;
~analyzer.pitchCont.run ;
~analyzer.amplitudeCont.run ;
)
// k = RuMaster([r,o,p,q]) ;

t = RuSeq(ruMaster:k,  analyzer:~analyzer).generateSeq ;
t.play

t.measure_(4).howMany_(8) ;
t.chain_(40)
t.tempo_(60)
t.maximum_(2)

t.pause
*/

