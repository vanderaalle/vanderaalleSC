// The idea is that we pass a UGen name and the class returns a SynthDef


Wrapper {

	// uGen is the uGen to be wrapped into a synthDef
	// index is an index in order to have a reference for the def
	var <>uGen, <>index ;
	
	
	*new { arg uGen, rate = \k, index = 0 ; 
		^super.new.initDefWrapper(uGen, rate, index).createSynthDef(rate) 
	}

	initDefWrapper { arg anUGen, rate, anIndex ;
		uGen = anUGen ;	
		index = anIndex
	}

	createArgStr {
		var argStr = "" ;
		uGen.class.findRespondingMethodFor('ar').argNames[1..]
			.do({ |n| argStr = argStr+n.asString++"," }) ;
		^argStr[..argStr.size-2]	
	}
	
	// main method: it creates and return the synthDef
	createSynthDef { arg rate = \k ;
		var synthDef ;
		var argStr = this.createArgStr ;
		synthDef = format("
SynthDef(\"%_%\", { arg out, % ;
	Out.%r(out, %.%r(%))
	 })
		", 
		uGen.name, index, 
		argStr,
		rate.asString,
		uGen.name,
		rate.asString,
		argStr 
		)
		^synthDef.interpret
	}
	
	
}