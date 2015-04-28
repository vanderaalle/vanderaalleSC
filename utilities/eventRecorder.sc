/* 
EventRecorder can be made dependant to the Analyzer

every time it gets onset/silence couple, it rec a buffer
Plus, a loop reads the buffer chain

to control from outside:
	- bufDur
	- bufListSegment
	- something to free something sooner or later...

The idea is that we have:
Analyzer --> 	EventRecPlayer2 --> 	FxMan (i.e. a bank of fx) --> out 
			|	here the controller works			|

Usage:

s.boot ;
u = Bus.audio(Server.local) ; 
x = { Out.ar(u, SoundIn.ar(0))}.play ;
a = Analyzer(u) ;
b = EventRecorder2(a, u) ;
b.bufDur = 0.5
b.bufList[0].play;


*/

// maybe the following should become an abstract class and then we subclass it

EventRecorder {
	
	var <>analyzer ;
	var <>on, <>off ;
	var <>sy ;
	var <>rec ;
	var <>buf, <>bufList ;
	var <>num ;
	var <>bus ;
	var <>bufDur ;
	var <>bufNum ;
	var <>numChannels ;
		
	*new { arg analyzer, bus, bufNum, numChannels = 2 ;
		^super.new.initERP(analyzer, bus, bufNum, numChannels)
	}

	initERP { arg anAna, aBus, aBufNum, aNumChannels ;
		analyzer = anAna ;
		this.connect(analyzer) ;
		bufNum = aBufNum ;
		bufNum ? bufNum = Server.local.options.numBuffers ;
		bufDur = 1 ;
		numChannels = aNumChannels ;
		bus = aBus ; //Bus.audio(Server.local, 1) ;
		buf = Buffer.alloc(Server.local, Server.local.sampleRate * bufDur, numChannels) ; // alloc the bus
		bufList = [] ;
		// this will record to the buf
		rec = SynthDef(\recout, { arg bufnum, bus, offset = 0;
			RecordBuf.ar(In.ar(bus,numChannels), bufnum, offset) })
				.play(Server.local, [\bufnum, buf.bufnum, \bus, bus], \addToTail);
		// a lock
		on = false ;
	}	
	
	
	// register to a model
	connect { arg analyzerModel ;
		analyzerModel.addDependant(this) ;
	}
	// do we need unconnect?

	update { arg theChanged, theChanger, more ;
		case 
			{ more[0] == \onset  } 
				{ 	
				if (on == false){
					if (bufList.size >= bufNum)
						{
						bufList[0].free ;
						bufList = bufList[1..] ;
						} ; // we get the last buffers
					bufList = bufList.add
						(Buffer.alloc(Server.local, Server.local.sampleRate * bufDur, numChannels)) ;
					buf.zero ;
					// from the beginning
					rec.set(\offset, 0) ;
					// go
					rec.run ;			
					// update lock
					on = true ;  "start".postln;
					//"bufList size is:".post; bufList.size.postln
					}
				 } 		
	
			{ more[0] == \silence && on == true }
				{ 
				"pause".postln ; // pause
				rec.run(false) ;
				// rec to a progressive buffer
				buf.copyData(bufList[bufList.size-1]) ;
				on = false ; 
				this.changed(this, [\newBuf]) ;
				}	
	}


}



// this variant uses fixed duration, instead of detectSilence
// good for unknown dense sound materials
EventRecorder2 : EventRecorder {
	
	update { arg theChanged, theChanger, more ;
		if ( more[0] == \onset )
				{ 
			if (on == false) {
				on = true ;
			Routine.run({
					if (bufList.size >= bufNum)
						{
						bufList[0].free ;
						bufList = bufList[1..] ;
						} ; // we get the last buffers
					bufList = bufList.add
					// we need to hardcode numchans
						(Buffer.alloc(Server.local, Server.local.sampleRate * bufDur,numChannels)) ;
					buf.zero ;
					// from the beginning
					rec.set(\offset, 0) ;
					// go
					rec.run ;					
					"bufList size is:".post; bufList.size.postln ;
					bufDur.wait ;
				rec.run(false) ;
				// rec to a progressive buffer
				buf.copyData(bufList[bufList.size-1].normalize) ;
				this.changed(this, [\newBuf]) ;
				on = false
				})
			}
				}	
	}
}











EventRecPlayer {
	
	var <>ana ;
	var <>on, <>off ;
	var <>sy ;
	var <>rec ;
	var <>buf, <>buf2 ;
	var <>num ;
	var <>bus ;

	*new { arg ana ;
		^super.new.initERP(ana)
	}

	initERP { arg anAna;
		ana = anAna ;
		bus = Bus.audio(Server.local, 1) ;
		buf = Buffer.alloc(Server.local, Server.local.sampleRate * 1.0) ; // alloc the bus
		buf2 = Buffer.alloc(Server.local, Server.local.sampleRate * 1.0) ; // alloc the bus
		num = 0 ;
		// this will record to the buf
		rec = SynthDef(\recout, { arg bufnum, bus, offset = 0;
			RecordBuf.ar(SoundIn.ar(0), bufnum, offset) })
				.play(Server.local, [\bufnum, buf.bufnum, \bus, buf], \addToTail);
		// a lock
		on = false ;
		{PlayBuf.ar(1, buf, loop:-1)}.play
	}	


	update { arg theChanged, theChanger, more ;
		case 
			{ more[0] == \onset  } 
				{ 	
				if (on == false){
					num.postln ;
					buf.zero ;
					// from the beginning
					rec.set(\offset, 0) ;
					// go
					rec.run ;					
					// update lock
					on = true ;  "start".postln;}
				 } 		
	
			{ more[0] == \silence && on == true }
				{ 
				"pause".postln ; // pause
				rec.run(false) ;
				// rec to a progressive filename
				buf.copyData(buf2) ;
				buf2.write
			("/test/"++num.asString++".aiff".standardizePath, "aiff", "int16") ;
				on = false ; 
				num = num+1 ;
				}	
	}

}
