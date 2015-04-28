s.reboot ;



(
d = BabaWrapper(Pulse).send(s) ;
e = BabaWrapper(SinOsc).send(s) ;

// 8-track mixer
m = SynthDef(\mix8, { arg out, in1, in2, in3, in4, pos, level ;
	Out.ar(out, Pan2.ar(Mix.new([
		In.ar(in1), In.ar(in2), In.ar(in3), In.ar(in4), 
	]), In.ar(pos), In.ar(level))
	)
}).send(s) ;
)

b = BabaPatcher("Blob!") ;

b.add(d, \pulse0) ;
b.add(e, \sine0) ;
b.add(d, \pulse1) ;
b.add(e, \sine1) ;

b.add(m, \mix4) ;


b.setList(\pulse0, [\freq, 12, \mul, 0.5, \width, 0.5]) ;
b.setList(\pulse1, [\freq, 12, \mul, 0.5, \width, 0.5]) ;
b.setList(\sine0, [\freq, 100, \mul, 0.5]) ;
b.setList(\sine1, [\freq, 120, \mul, 0.5]) ;

	
b.gui(2, 500) ;
b.roar ;
