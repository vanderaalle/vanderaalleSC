// follows pitches continuously in input by associating each quarter tone a rumentarium sound body.

RuFollower3 {

	var <>analyzer, <>array, <>flagArray, <>evtDur, <tempo ;
	var <>pitch, <>loud ;
	var <>ruMaster ;
	var <>min, <>max ;
	var <>counter, <>items, item;
	
	
	*new { arg analyzer, array, evtDur = 1, ruMaster ;
		^super.new.initRuFoll3(analyzer, array, evtDur, ruMaster) ;
	}
	
	initRuFoll3 { arg anAnalyzer, anArray, anEvtDur, aRuMaster ;
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
		counter = 0 ;
		items = 24 ;
		item = 0 ;
	}
		
	next { counter = (counter+1) % array.size ; 
		item = (item + 1) % items ; 
		"will reset after: ".post ; (items - item).postln
	}
	
	
	generateEvent { arg id ;
//		var id = (pitch.round(0.5).postln%12*2).asInteger ;
		var port = array[id] ;
		var flag = flagArray[id] ;
//		("port:"+port+", id: "+id).postln ;
		if (flagArray[id] == 0)
			{ 	{
				flagArray[id] = 1 ;
				ruMaster.set(port, loud.linlin(-90, 0, min, max)) ;
				evtDur.wait ;
				ruMaster.set(port, 0) ;
				flagArray[id]  = 0 ;
				}.fork
			}
		
		}
	
	drummer {
		var set, available = [], elements ;
		items = rrand(4, 124) ;
		min = rrand(0.4,1) ; evtDur = rrand(0.25,1) ;
		set = [0,1,2,3].scramble[..rrand(0,3)] ;
		set.do{|i| available = available.addAll(Array.series(6, i*6+1))} ;
		elements = rrand(2,12).clip2(available.size) ;
		this.array_(available.scramble[..elements])
	} 

		
	update { arg theChanged, theChanger, more ;
		//more.postln ;
		var id ;
		case 
		{ more[0] == \onset }
			 {  this.next ; this.generateEvent(counter) ; 
				 if (item == 0) { this.drummer } ;
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
~bsj = {Out.ar(~inBus, SoundIn.ar)}.play ;
~bsjO = {Out.ar(0, SoundIn.ar)}.play ;

~analyzer = Analyzer(~inBus) ; // we get the inbus
(
~analyzer.onsets.run ;
~analyzer.pitchCont.run ;
~analyzer.amplitudeCont.run ;
)
// k = RuMaster([r,o,p,q]) ;

(
t = RuFollower3(analyzer: ~analyzer, ruMaster:k) ;

t.min_(0.5) ; t.evtDur_(0.25) ; // this is tempo
t.items = 48

*/


