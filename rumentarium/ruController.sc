
// takes the data from datasender, remaps data, handle Arduino communication
RuController {
	
	// on init:
	// - gets a dataSender, generico object sending "changed" with pollable array
	// - becomes a dependant ans start listening
	var <>dataSender ;
	var <>array ; // the actual arr in scheduling. 
	var <>mapMethod ; // the mapping method. The idea is that in the class you write
	 			   // the mapping methods you need and select them via setter
	var <>arduino ; // arduino via SMS interface
	
	*new { arg dataSender ; 
		^super.new.initRuController(dataSender) 
	}

	initRuController { arg aDataSender ;
		dataSender = aDataSender ;
		dataSender.addDependant(this) ;
		arduino = ArduinoSMS("/dev/tty.usbserial-A1001N7X", 115200);
	}
	
	// saver
	zero {
		[3, 5, 6, 9, 10, 11].do{ |port| arduino.send($w, $a, port, 0) }
	}
	
	update { arg theChanged, theChanger, more;
	// it assumes that the sender's more is an arg for the chosen method
		var theArg = more[0] ;
		this.mapToRumentarium(theArg)
	}
	
	mapToRumentarium { arg theArg ;
			this.perform(mapMethod, theArg)
	}
	
	braille { arg brailleArray ;
		var port ;
		// if you pass an array of floats, they can act as amp scale
		brailleArray.postln.do({ arg val, index ;
			port = [3, 5, 6, 9, 10, 11][index] ;
			arduino.send($w, $a, port, 255*val) ;
		}) ;
	}

	tst { arg val ;
		"I'm a test".postln ;
		val.postln
	}

	bassListener { arg analysisArr ; // hasFreq, pitch as MIDI, loud range: [0,1]
		var port, arr ;
		var hasFreq, pitch, loud, registers, ext ;
		# hasFreq, pitch, loud = analysisArr ;
		pitch = pitch-35 ;
		// Range: 35 - 72: 37
		registers = [
			[1,0,0,0,0,0],
			[1,1,0,0,0,0],
			[1,1,1,0,0,0],
			[0,1,1,1,0,0],
			[0,0,1,1,1,0],
			[0,0,0,0,1,0]
			] ;
		ext = 37/6 ;
		arr = if (hasFreq == 0) 
			{ [0,0,0,0,0,1] } 
			{ 	ext = 37/6 ;
				arr = registers[(pitch/ext).asInteger]
			} ;
		arr.postln.do({ arg val, index ;
			port = [3, 5, 6, 9, 10, 11][index] ;
			arduino.send($w, $a, port, val*255*loud) ;
		}) ;

	}

}