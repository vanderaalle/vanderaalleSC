// Aide-mémoire interface for M-Audio Xsession pro

/*
works as follows:

a = XSession.new ; // a new instance
x = Synth(\default) ; // what you prefer


// then:
// you set a var with the name of the controller
// providing a func, where val is the value of the controller


a.aHead = { |val| val.postln } ;
a.aPitch = { |val| x.set(\freq, 200+val*10) } ; 
a.bPitch = { |val| x.set(\freq, 400+val*10) } ;
*/

XSession {
	
	var aPitch, aVol, a1, a2, a3, aLo, aMid, aHi ;
	var bPitch, bVol, b1, b2, b3, bLo, bMid, bHi ;
	var aHead, bHead ;
	var cross ;
	var <numArray, <nameArray ;
	var <controlDict, <noteOnDict ;
	
	
	*new { arg inports = 2 , outports = 2 ;
		// assuming 2 ports: US-122 + Xsession 
			^super.new.initXSession(inports, outports);
	}	

	initXSession { arg inPorts, outPorts ;
		controlDict = IdentityDictionary.new ;
		noteOnDict =  IdentityDictionary.new ;
		MIDIClient.init(inPorts,outPorts);	// explicitly intialize the client
			inPorts.do({ arg i; 
			MIDIIn.connect(i, MIDIClient.sources.at(i));
		});
		numArray = [12, 11, 24, 25, 26, 29, 28, 27].addAll([15, 14, 34, 35, 36, 33, 32, 31]) ;
		nameArray =[\aPitch, \aVol, \a1, \a2, \a3, \aLo, \aMid, \aHi]
			.addAll([\bPitch, \bVol, \bHi, \bMid, \bLo, \b3, \b2, \b1]) ;
		[12, 11, 24, 25, 26, 29, 28, 27].do({ arg item ;
			controlDict[item] = {} ;
			 }) ;
		[15, 14, 34, 35, 36, 33, 32, 31].do({ arg item ;
			controlDict[item] = {} ;
			}) ;
		// heads
		nameArray = nameArray.addAll([\aHead, \bHead]) ; 
		numArray = numArray.addAll([44, 45]) ;
		noteOnDict[44] = {} ;
		noteOnDict[45] = {} ;
		cross =  [20, 17]; // problematic
		this.map ;
	}	
	

	map { 
		MIDIIn.control = { arg src, chan, num, val ;
			controlDict[num].value(val)
		} ;
		MIDIIn.noteOn = { arg src, chan, num, val ;
			noteOnDict[num].value(val)
		}
	}

// so boring
	
	aHead_ { arg func ;
		var num = numArray[nameArray.indexOf(\aHead)] ;
		noteOnDict[num] = func ;
		aHead = func ; 		
		}
	
	aPitch_ { arg func ; 
		var num = numArray[nameArray.indexOf(\aPitch)] ;
		controlDict[num] = func ;
		aPitch = func ; 
		}

	aVol_  { arg func ; 
		var num = numArray[nameArray.indexOf(\aVol)] ;
		controlDict[num] = func ;
		aVol = func ; 
		}

	a1_ 	{ arg func ; 
		var num = numArray[nameArray.indexOf(\a1)] ;
		controlDict[num] = func ;
		a1 = func ; 
		}

	a2_ 	{ arg func ; 
		var num = numArray[nameArray.indexOf(\a2)] ;
		controlDict[num] = func ;
		a2 = func ; 
		}

	a3_ 	{ arg func ; 
		var num = numArray[nameArray.indexOf(\a3)] ;
		controlDict[num] = func ;
		a3 = func ; 
		}

	aLo_ 	{ arg func ; 
		var num = numArray[nameArray.indexOf(\aLo)] ;
		controlDict[num] = func ;
		aLo = func ; 
		}

	aMid_ 	{ arg func ; 
		var num = numArray[nameArray.indexOf(\aMid)] ;
		controlDict[num] = func ;
		aMid = func ; 
		}

	aHi_ 	{ arg func ; 
		var num = numArray[nameArray.indexOf(\aHi)] ;
		controlDict[num] = func ;
		aHi = func ; 
		}
		
// B starts here
		
	bHead_ { arg func ;
		var num = numArray[nameArray.indexOf(\bHead)] ;
		noteOnDict[num] = func ;
		bHead = func ; 		
		}
	

	bPitch_ { arg func ; 
		var num = numArray[nameArray.indexOf(\bPitch)] ;
		controlDict[num] = func ;
		bPitch = func ; 
		}

	bVol_  { arg func ; 
		var num = numArray[nameArray.indexOf(\bVol)] ;
		controlDict[num] = func ;
		bVol = func ; 
		}

	b1_ 	{ arg func ; 
		var num = numArray[nameArray.indexOf(\b1)] ;
		controlDict[num] = func ;
		b1 = func ; 
		}

	b2_ 	{ arg func ; 
		var num = numArray[nameArray.indexOf(\b2)] ;
		controlDict[num] = func ;
		b2 = func ; 
		}

	b3_ 	{ arg func ; 
		var num = numArray[nameArray.indexOf(\b3)] ;
		controlDict[num] = func ;
		b3 = func ; 
		}

	bLo_ 	{ arg func ; 
		var num = numArray[nameArray.indexOf(\bLo)] ;
		controlDict[num] = func ;
		bLo = func ; 
		}

	bMid_ 	{ arg func ; 
		var num = numArray[nameArray.indexOf(\bMid)] ;
		controlDict[num] = func ;
		bMid = func ; 
		}

	bHi_ 	{ arg func ; 
		var num = numArray[nameArray.indexOf(\bHi)] ;
		controlDict[num] = func ;
		bHi = func ; 
		}

// problematic
	cross_ 	{ arg func ; cross = func ; 
					MIDIIn.control = func }


}