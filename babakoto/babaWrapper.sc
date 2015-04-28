/*
THE BABAKOTO PROJECT: BabaWrapper
-a- started: ~20/06/08

Utilty to generate baba SynthDefs from UGens.
You pass a UGen, it returns a baba synthDef

Last updated: 030708

// andrea valle
// http://www.cirma.unito.it/andrea/
// andrea.valle@unito.it

*/


BabaWrapper {

	// uGen is the uGen to be wrapped into a synthDef
	var <>uGen, <>name ;
	
	
	*new { arg uGen, name ; 
		^super.new.initDefWrapper(uGen, name).createSynthDef 
	}

	initDefWrapper { arg anUGen, aName ;
		uGen = anUGen ;	
		name = aName ? uGen.name ;
	}


/*

// this version includes default args
// doesn't make much sense if you look to the synthDefs, even if it makes it 
// if you immediately consider the GUI

	createArgStr {
		var argStr = "" ;
		var inStr = "" ;
		var chosen ;
		// felix
		var defaults = uGen.class.findMethod(\ar).prototypeFrame ;
		defaults = if(defaults.notNil,{defaults.copyRange(1, defaults.size-1)},{[]});
		//
		uGen.class.findRespondingMethodFor('ar').argNames[1..]
			.do({ |n, i|
			 chosen = defaults[i] ;
			argStr = argStr+n.asString+"="+chosen.asString++"," ;
			inStr = inStr+"In.ar("++n.asString++")," ;
			 }) ;
			 
		^[argStr[..argStr.size-2], inStr[..inStr.size-2]].postln	
	}
	
*/	


	createArgStr {
		var argStr = "" ;
		var inStr = "" ;
		uGen.class.findRespondingMethodFor('ar').argNames[1..]
			.do({ |n, i|
			argStr = argStr+n.asString++"," ;
			inStr = inStr+"In.ar("++n.asString++")," ;
			 }) ;
		^[argStr[..argStr.size-2], inStr[..inStr.size-2]]	
	}
	
	// main method: it creates and return the synthDef
	createSynthDef {
		var synthDef ;
		var argStr, inStr;
		#argStr, inStr = this.createArgStr ;
		synthDef = format("
SynthDef(\"%\", { arg out, % ;
	Out.ar(out, %.ar(%))
	 })
		", 
		name,
		argStr,
		uGen.name,
		inStr 
		)
		^synthDef.interpret
	}
	
	
}