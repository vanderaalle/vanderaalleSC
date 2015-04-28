s.reboot ;

(
d = BabaWrapper(Pulse).send(s) ;
e = BabaWrapper(SinOsc).send(s) ;

// 8-track mixer
m = SynthDef(\mix8, { arg out, in1, in2, in3, in4, in5, in6, in7, in8, pos, level ;
	Out.ar(out, Pan2.ar(Mix.new([
		In.ar(in1), In.ar(in2), In.ar(in3), In.ar(in4), 
		In.ar(in5), In.ar(in6), In.ar(in7), In.ar(in8)
	]), In.ar(pos), In.ar(level))
	)
}).send(s) ;

// LPF
l = SynthDef(\LPF, { arg out, in, freq ;
	Out.ar(out, LPF.ar(In.ar(in), In.ar(freq)))
	}).send(s) ;

) 

b = BabaPatcher("Blob!") ;

(
Routine.new({
10.do({ |i|
	var name = ("pulse"++i.asString).asSymbol ;
	b.add(d, name) ;
	0.05.wait ;
	b.setList(name, [\freq, 6+i, \mul, 0.5, \width, 0.5]) ;
}) ;
"done".postln ;
}).play(AppClock) ;
)

(
Routine.new({
10.do({ |i|
	var name = ("sin"++i.asString).asSymbol ;
	b.add(e, name) ;
	0.05.wait ;
	b.setList(name, [\freq, 12*i+48, \mul, 0.5]) ;
}) ;
"done".postln ;
}).play(AppClock) ;
)

//b.add(l, \LPF) ;
//b.setList(\LPF, [\freq, 1000]);

b.add(m, \mix8) ;
b.setList(\mix8, [\level, 1, \pos, 0.5]);

b.gui(3, 800) ;
b.roar ;

// controller
x = BabaXSession(b).gui ;
