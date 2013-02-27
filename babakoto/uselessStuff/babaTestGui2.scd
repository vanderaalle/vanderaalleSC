// booting the server
s.reboot ;

(
// here we send some synthDef

// these twos are created by wrapping a UGen in a synthDef 
// thru BabaWrapper
d = BabaWrapper(Pulse).send(s) ;
e = BabaWrapper(SinOsc).send(s) ;

// an 8-track mixer
m = SynthDef(\mix8, { arg out, in1, in2, in3, in4, in5, in6, in7, in8, pos, level ;
	Out.ar(out, Pan2.ar(Mix.new([
		In.ar(in1), In.ar(in2), In.ar(in3), In.ar(in4), 
		In.ar(in5), In.ar(in6), In.ar(in7), In.ar(in8)
	]), In.ar(pos), In.ar(level))
	)
}).send(s) ;

) 


// ok, now we create the BabaPatcher
~baba = BabaPatcher("Blob!") ;

// why a Routine?
// because after adding a synth we set its args, 
// and so we have to wait a tiny bit of time to
// let scsynth allocate the synth
(
Routine.new({
10.do({ |i|
	var name = ("pulse"++i.asString).asSymbol ;
	~baba.add(d, name) ;
	// let's wait a little
	0.05.wait ;
	~baba.setList(name, [\freq, 6+i, \mul, 0.5, \width, 0.5]) ;
}) ;
"done".postln ;
}).play(AppClock) ;
)

// the same
(
Routine.new({
10.do({ |i|
	var name = ("sin"++i.asString).asSymbol ;
	~baba.add(e, name) ;
	0.05.wait ;
	~baba.setList(name, [\freq, 12*i+48, \mul, 0.5]) ;
}) ;
"done".postln ;
}).play(AppClock) ;
)

// we add the mixer
~baba.add(m, \mix8) ;
~baba.setList(\mix8, [\level, 1, \pos, 0.5]);


// creating three windows 
~baba.gui([\pulse0, \pulse1, \pulse3], 1, 400) ;
~baba.gui([\sin0, \sin1, \sin2], 1, 400) ;
~baba.gui([\mix8], 1, 400) ;

// if you prefer all the stuff
~baba.gui(nil, 4, 850) ;

/*
// the gui method of BabaPatcher calls a specialized class, BabaGui
// this is the same of before:
BabaGui.new(~baba, [\pulse0, \pulse1, \pulse3], 1, 400).makeAllGui ;
BabaGui.new(~baba, [\sin0, \sin1, \sin2], 1, 400).makeAllGui ;
BabaGui.new(~baba, nil, 1, 400).makeAllGui ;

*/

