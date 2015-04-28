/*
THE BABAKOTO PROJECT: BabaSynth
-a- started: 11/06/08

BabaSynth is a wrapper for a synth
The idea is to put into an unique object
all the stuff related to a synth
- a name
- a synth
- a set of control synths
synth and controls must be grouped
so you can move the synth and all the controls are moved too

you can create BabaSynth instance directly
bu the idea is that you pass thru a BabaPatcher

the BabaSynth writes to its (fixed, consistent) audiobus. 
To output it to soundcard you have to tell it explicitly

Last updated: 030708

// andrea valle
// http://www.cirma.unito.it/andrea/
// andrea.valle@unito.it

*/

BabaSynth {

	// this means we can change the name but not the def (no sense)
	var <synthDef, <>name;
	// should they be public or not?
	// group wraps around sth audio and its controls
	var <>synth, <>group  ;
	// collects control names and values
	var <>controlDict, <>controlList ; // list keeps track of order
	// the bus where the synth writes
	var <>outBus ;
	
	// constructor: we need a def and a name 
	// through the name the synth can be referred
	*new { arg synthDef, name  ; 
		^super.new.initBabaSynth(synthDef, name) 
	}

	initBabaSynth { arg aSynthDef, aName ;
		synthDef = aSynthDef ;
		name = aName.asSymbol ;
		group = Group.new ; // the proper group
		// so while moving babasynths, effectively we move the groups (a synth and its controls)
		controlDict = IdentityDictionary.new ;
		controlList = [] ;
		// they should be bus, so not very clean to use them as usual args
		synthDef.allControlNames.do({ arg item ;
				var argName = item.asString.split($ )[4] ;
				var max = item.asString.split($ )[6].asFloat ;
				var bus = Bus.audio ; 
				// the previous is a *private* bus. 
				// The control keeps on writing here 
				// so that when you want to set a value
				// you connect the control and set its value
				// this means: we have always to use Bus allocator
				var val = max ;
				if (argName.asSymbol != \out)
				{
				controlList = controlList.add(argName.asSymbol) ;
				controlDict[argName.asSymbol] = [
					val, 
					0.0, 
					max,
					SynthDef("ctrl", { arg inval = val ; 
						Out.ar(bus, K2A.ar(inval)) }).play(group),
					bus
				]
				}
				}) ;
		// the audio synth writes here
		outBus = 	Bus.audio ;
		// the audio synth is after all the stuff
		// so controls precedes it
		// note that it start with 0 on all params
		// because args are filled with an empty audio bus
		synth = Synth.tail(group, synthDef.name) ;
		synth.set(\out, outBus) ;
	}


	// polymorphism
	// mimics synth.set on BabaSynth
	// NOT for out
	set { arg argName, val ;
		// max min tracking
//		if (val < controlDict[argName][1] ) { val = controlDict[argName][1] } ;
//		if (val > controlDict[argName][2] ) { val = controlDict[argName][2] } ;
		controlDict[argName][0] = val ;
		controlDict[argName][3].set(\inval, val) ;
		synth.set(argName, controlDict[argName][4]) ; 
	}

	// this is intended to let the user specify
	// explicitly the out audio bus
	// --> ONLY FOR public soundcard buses
	out { arg busIndex ;
		if ( busIndex.isNil ) 
			{ synth.set(\out, outBus) }
			{ synth.set(\out, busIndex) } ;
	}

	// this if for patching, i.e. from explicit buses
	// DANGEROUS: use it from BabaPatcher.in
	in { arg argName, busIndex ;
		synth.set(argName, busIndex) ;
	}	

// synth interface	
	
	run { arg boolean = true ;
		group.run(boolean) ;
	}
	
	free { 
	// freeing synths
		group.free ;
	// freeing buses
		controlDict.do({ |item| item[4].free })
		}
	
// order of execution

	moveAfter { arg aNode ;
		group.moveAfter(aNode) ;
	}
	
	moveBefore { arg aNode ;
		group.moveBefore(aNode) ;
	}
	
	
	moveToHead { 
		group.moveToHead ;
	}
	
	moveToTail {
		group.moveToTail ;
	}
	
// max & min for each arg: 
// the idea is too keep track of a range of values for each arg

	setMax { arg argName, val ; 
		controlDict[argName][2] = val ;
	}


	setMin { arg argName, val ; 
		controlDict[argName][1] = val ;
	}

	get { arg argName ;
		^controlDict[argName][0] ;
	}

	getMax { arg argName ; 
		^controlDict[argName][2] ;
	}


	getMin { arg argName ; 
		^controlDict[argName][1] ;
	}


	// let's see what's going on in output
	scope {
		outBus.scope ;
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
	{ arg out , freq = 440, mul = 1, add = 0 ;
	Out.ar(out, SinOsc.ar(In.ar(freq), 0, In.ar(mul), In.ar(add)))}
).send(s) ;
)
	
b = BabaPatcher.new 
b.add(k, \frq)
b.add(d, \tst)



b.set(\tst, \freq, 410)
b.set(\tst, \mul, 0.7)
b.out(\tst, 0)
b.set(\tst, \add, 0)


b.in(\tst, \freq, \frq)
b.remove(\tst)

b.moveBefore(\tst, \frq)

b.moveToTail(\tst)

*/