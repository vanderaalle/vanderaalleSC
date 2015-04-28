

/*

  Extensions follow, implementing a asPraat method
 return a Praat object

andrea valle: last update 30/01/07
*/


+ ArrayedCollection {

	asPraat { arg sndFileName, append = false ;
			var praat = Praat.new;
			praat.array = praat.array.addAll(this);
			^praat.init(sndFileName, append) ;

	}


}




+ Wavetable {

	asPraat {	 arg sndFileName, append = false ;
			var praat = Praat.new, sig ;
			sig = this.asSignal ;
			praat = praat.addAll(sig);
			^praat.init(sndFileName, append) ;

	}

}


+ Signal {


	asPraat { arg sndFileName, append = false ;
			var praat = Praat.new;
			praat.array = praat.array.addAll(this);
			^praat.init(sndFileName, append) ;

	}

}




+ Buffer {

	fillData {
		// works with mono
		this.loadToFloatArray(action: { |array, buf|
			{ ~praatArray = array ; // oh boy! this is pure shit
			}.defer;
			});
	}

	asPraat { arg sndFileName, append = false ;
		var praat = Praat.new ;
		praat.array = praat.array.addAll(~praatArray) ;
		~praatArray = nil ;
		^praat.init(sndFileName, append) ;
		}

}



+ Function {

/*

// Historical

	fillData {arg duration  = 1.0, server ; // duration: if too short Praat can't work
		//var praat = Praat.new, r;
		this.loadToFloatArray(duration, server, { |array, buf|
			// works with mono
			{ ~praatArray = array ; // oh boy! this is pure shit
			}.defer;
			})
		}


	asPraat { arg sndFileName, append = false;
		var praat = Praat.new ;
		praat.array = praat.array.addAll(~praatArray) ;
		~praatArray = nil ;
		^praat.init(sndFileName, append) ;
		}

*/
	asPraat { arg sndName, append = false, duration  = 1.0, server ;
		var praat = Praat.new, r ;
		sndName = sndName ? ("/tmp/praatTmp.aiff") ;
		r = Routine.new({
		this.loadToFloatArray(duration, server, { |array, buf|
			// works with mono
			~praatArray = array ; // oh boy! this is pure shit

			}) ;
		(duration*2).wait // twice is just to be sure
		}).play ;
		"fill done".postln ;
		praat.array = praat.array.addAll(~praatArray) ;
		~praatArray = nil ;
		^praat.init(sndName, append) ;

		}

}



+ Env {

	asPraat { arg sndFileName, append = false, size = 44100 ;	 // if too short Praat can't work
			var praat = Praat.new, sig ;
			sig = this.asSignal(size) ;
			praat = sig.asPraat;
			^praat.init(sndFileName, append) ;
	}

}



+ SoundFile{

// It is supposed you have already called openRead(path)
// not the best efficiency to rewrite the file...(but same interface at least)

	asPraat { arg sndFileName, append = false ;
	 	var sig = Signal.newClear(this.numFrames), praat ;
	 	this.readData(sig) ;
	 	praat = sig.asPraat ;
		^praat.init(sndFileName, append) ;
	 		}


}
