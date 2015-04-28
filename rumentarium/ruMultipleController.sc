
// takes the data from datasender, remaps data, handle Arduino communication
RuMultipleController {
	
	// on init:
	// - gets a dataSender, generico object sending "changed" with pollable array
	// - becomes a dependant ans start listening
	var <>dataSender ;
	var <>array ; // the actual arr in scheduling. 
	var <>mapMethod ; // the mapping method. The idea is that in the class you write
	 			   // the mapping methods you need and select them via setter
	var <>arduinoList ; // an array containing arduino via SMS interface
	var portIndex ;
	
	*new { arg dataSender, arduinoList ; 
		^super.new.initRuController(dataSender, arduinoList) 
	}

	initRuController { arg aDataSender,anArduinoList ;
		dataSender = aDataSender ;
		dataSender.addDependant(this) ;
		arduinoList = anArduinoList	;
		portIndex = [3, 5, 6, 9, 10, 11].wrapExtend([3, 5, 6, 9, 10, 11].size*arduinoList.size)
	}
	
	// saver
	zero {
		[3, 5, 6, 9, 10, 11].do{ |port| arduinoList.do({|ard| ard.send($w, $a, port, 0)}) }
	}
	
	update { arg theChanged, theChanger, more;
	// it assumes that the sender's more is an arg for the chosen method
		var theArg = more[0] ;
		this.mapToRumentarium(theArg)
	}
	
	mapToRumentarium { arg theArg ;
			this.perform(mapMethod, theArg)
	}
	
	amp { arg ampArray ;
		var port, board ;
		// if you pass an array of floats, they can act as amp scale
		ampArray.postln.do({ arg val, index ;
			port = portIndex[index] ;
			arduinoList[(index/(arduinoList.size*6)).asInteger].send($w, $a, port, 255*val) ;
		}) ;
	}

	tst { arg val ;
		"I'm a test".postln ;
		val.postln
	}


}