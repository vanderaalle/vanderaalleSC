/*
THE BABAKOTO PROJECT: BabaPatcher
-a- started: 12/06/08

BabaPatcher is the manager of all the (baba)synths
it handles:
- synth creations
- synth control
- synth patching
- order of execution

It can be thought as a collection of synths, 
plus some management capabilties.
The idea is that you always pass thru the BabaPatcher
and never thru the BabaSynths.
More, GUIs and other UIs are all observers on BabaPatcher

Last updated: 030708

// andrea valle
// http://www.cirma.unito.it/andrea/
// andrea.valle@unito.it

*/


/*
SYNTHDEF STRUCTURE

all ctrl uses In.ar APART out
(
d = SynthDef("tst", 
	{ arg out , freq = 440, mul = 1, add = 0 ;
	Out.ar(out, SinOsc.ar(In.ar(freq), 0, In.ar(mul), In.ar(add)))}
).send(s) ;
)


*/



BabaPatcher {

	// collects all the synths
	var <>synthDict ;
	var <>name ; // symbolic name of the patcher
	var <>index ; // can be useful to keep track of the synth addition order
	var <>connectionDict ; // stores info about patching
	
	// constructor: we need a def and a name 
	// through the name the synth can be referred
	*new { arg name = \theBabaPatcher ; 
		^super.new.initBabaPatcher(name) 
	}

	initBabaPatcher { arg aName ;
		name = aName.asSymbol ;
		index = 0 ;
		synthDict = IdentityDictionary.new ;
		connectionDict = IdentityDictionary.new ;
		this.changed(this, [\init]) ;
	}

	createIndexList {
		var indexList ;
		synthDict.do({ arg item ;
			indexList = indexList.add(item[1]) ;
		}) ; // create the list
		^indexList
	}
	
	order {
		var indexList ; // list of all indices
		var series ;
		indexList = this.createIndexList ;
		indexList = indexList.sort ; // right ordering
		series = Array.series(indexList.size) ; // new indices
		synthDict.do({ arg item ;
			item[1] = series[indexList.indexOf(item[1])] ;
		}) ; // create the list
		index = indexList.size - 1 ;
		this.changed(this, [\order]) ;

	}

	add { arg synthDef, name, args ; // args is an array
		var synth = BabaSynth(synthDef, name).moveToTail ;
		synthDict[name.asSymbol] = [synth, index] ;
		// moveToTail: ratio --> you append to the synth chain
		index = index + 1 ;
		this.changed(this, [\add, synth]) ;
	 }
	 
	remove { arg name ;
		name = name.asSymbol ;
		this.free(name) ;
		synthDict.removeAt(name) ;
		connectionDict.removeAt(name) ;
		this.order ;
	}	 
	 
	
	removeAll {
		synthDict.copy.keys.do({
			|name| 
		name = name.asSymbol ;
		this.free(name) ;
		synthDict.removeAt(name) ;
		}) ;
		connectionDict = IdentityDictionary.new ;
		this.changed(this, [\order]) ;
	}
	
// BabaSynth interface: it substantially forwards the messages to the selected BabaSynth
	
	set { arg synthName, argName, val ;
		var max, min ;
		synthName = synthName.asSymbol ;
		synthDict[synthName][0].set(argName, val) ;
		val =ÊsynthDict[synthName][0].get(argName) ;
		max = synthDict[synthName][0].getMax(argName) ;
		if (val > max) { this.setMax(synthName, argName, val) } ;
		min = synthDict[synthName][0].getMin(argName) ;
		if (val < min) { this.setMin(synthName, argName, val) } ;
		if (connectionDict.includesKey(synthName)) 
			{
			connectionDict[synthName] =
			connectionDict[synthName].clump(2)
				.reject({ |i| i[0] == argName }).flat
			} ;
		this.changed(this, [\set, synthName, argName, val]) ;
	}	
	
	
	setList { arg synthName, args ;
		args.clump(2).do({ |argVal| 
			this.set(synthName, argVal[0], argVal[1])
		}) ;
	}


	setRangeList { arg synthName, args ;
		// args: \freq, [val, min, max], \width, [val, min, max] etc
		args.clump(2).do({ |argVal| 
			this.set(synthName, argVal[0], argVal[1][0]) ;
			this.setMin(synthName, argVal[0], argVal[1][1]) ;
			this.setMax(synthName, argVal[0], argVal[1][2]) ;
		}) ;
	}
	 
	setMax { arg synthName,  argName, val ;
		synthDict[synthName.asSymbol][0].setMax(argName, val) ;
		this.changed(this, [\setMax, synthName, argName, val]) ;
	}
	
	setMin { arg synthName,  argName, val ;
		synthDict[synthName.asSymbol][0].setMin(argName, val) ;
		this.changed(this, [\setMin, synthName, argName, val]) ;
	}

	get { arg synthName, argName ;
		^synthDict[synthName.asSymbol][0].get(argName) ;
	}


	getMax { arg synthName,  argName ;
		^synthDict[synthName.asSymbol][0].getMax(argName) ;
	}
	
	getMin { arg synthName,  argName ;
		^synthDict[synthName.asSymbol][0].getMin(argName) ;
	}

	 
	// ATTENTION: different from BabaSynth
	// in this case we use the name of the controlling synth
	// the ctrl bus is disconnected and the outBus of from
	// is inserted as input
	in { arg synthName, argName, from ;
		var bus ;
		synthName = synthName.asSymbol ;
		from = from.asSymbol ;
		bus = synthDict[from][0].outBus;
		synthDict[synthName][0].in(argName, bus) ;
		connectionDict[synthName] = connectionDict[synthName].addAll([argName, from]) ;
		this.changed(this, [\in, synthName, argName, from]) ;
	}	

	// note that if the signal is routed to out
	// it does no more go to its private bus
	// so it cannot be patched
	out { arg synthName, busIndex ;
		synthDict[synthName.asSymbol][0].out(busIndex) ;
		this.changed(this, [\out, synthName, busIndex]) ;
	}
	
	run { arg synthName, boolean = true ;
		synthDict[synthName.asSymbol][0].run(boolean) ;
		this.changed(this, [\run, synthName, boolean]) ;
	}	

	// PRIVATE: use remove instead 
	free { arg synthName ;
		synthDict[synthName.asSymbol][0].free ;
	}	

	scope { arg synthName ;
		synthDict[synthName.asSymbol][0].scope ;
	}

	moveAfter { arg synthName, aNode ;
		synthDict[synthName.asSymbol][0]
		.moveAfter
		(synthDict[aNode.asSymbol][0].group) ;
		synthDict[synthName.asSymbol][1] = synthDict[aNode.asSymbol][1]+0.5 ;
		this.order ;
	}	
	
	moveBefore { arg synthName, aNode ;
		synthDict[synthName.asSymbol][0]
			.moveBefore
			(synthDict[aNode.asSymbol][0].group) ;
		synthDict[synthName.asSymbol][1] = synthDict[aNode.asSymbol][1]-0.5 ;
		this.order ;
	}	
	
	moveToHead { arg synthName ;
		synthDict[synthName.asSymbol][0]
		.moveToHead ;
		synthDict[synthName.asSymbol][1] = -1 ;
		this.order ;		
	}	
	moveToTail { arg synthName, aNode ;
		synthDict[synthName.asSymbol][0]
		.moveToTail ;
		synthDict[synthName.asSymbol][1] = index + 0.5 ;
		this.order ;
	}	
/////////////////////////////////////////////////////////////////////////////	
// returns a BabaGui
	gui { arg synthNameArr, widthInModules = 3, height = 800, title, color ;
		var gui, parser, document ;
		gui = BabaGui(this, synthNameArr, widthInModules, height, title:title, color: color).makeAllGui ;
	} 

// returns a BabaDocument
	roar	{ arg controller, alpha = 0.9 ;
		var parser = BabaParser(this, controller) ;
		var document = BabaDocument(parser, alpha).bounds_(Rect(1200, 600, 250, 600)) ;
	}

// creates a global gui with a babaDocument
	ui { arg widthInModules = 3, height = 800, controller, alpha = 0.9, title, color ;
		var gui, parser, document ;
		gui = BabaGui(this, nil, widthInModules, height, title:title, color:color).makeAllGui ;
		parser = BabaParser(this, controller) ;
		document = BabaDocument(parser, alpha).bounds_(Rect(1200, 600, 250, 600)) ;
	} 
	 
}


/*


s.reboot ;


(
k = SynthDef("frq", { arg out; Out.ar(out, SinOsc.ar(4, mul: 400, add: 500))}).send(s)
)
s.scope(10)

(
d = SynthDef("tst", 
	{ arg out , freq = 440, width = 0.5, mul = 1, add = 0 ;
	Out.ar(out, Pulse.ar(In.ar(freq), In.ar(width), In.ar(mul), In.ar(add)))}
).send(s) ;
)

(
f = SynthDef("flt", 
	{ arg out , in = 0, freq = 440, mul = 1, add = 0 ;
	Out.ar(out, LPF.ar(In.ar(in), In.ar(freq), In.ar(mul), In.ar(add)))}
).send(s) ;
)

(
e = SynthDef("sin", 
	{ arg out , in = 0, freq = 440, mul = 1, add = 0 ;
	Out.ar(out, SinOsc.ar(In.ar(freq), 0, In.ar(mul), In.ar(add)))}
).send(s) ;
)


	
b = BabaPatcher.new 
b.add(k, \frq)
b.add(d, \tst, [\freq, 440, \mul, 1])
b.add(f, \flt)
b.add(e, \sin)


b.set(\tst, \freq, 410)
b.set(\tst, \mul, 0.7)
b.set(\tst, \add, 0)
b.set(\tst, \width, 0.5)
b.out(\tst, 0)

b.out(\tst)
b.in(\flt, \in, \tst)
b.set(\flt, \freq, 300)
b.set(\flt, \mul, 10.7)
b.set(\flt, \add, 0)

b.out(\flt, 1)

b.remove(\flt)

b.moveBefore(\tst, \frq)

b.moveToTail(\flt)


b.set(\sin, \freq, 1000)
b.set(\sin, \mul, 0.7)
b.set(\sin, \add, 0)
b.out(\sin, 0)
*/

