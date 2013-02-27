(
// PAY ATTENTION
s.reboot ;
)

// WAIT for NOTIFICATION
(
~dur = 1 ;
10.do({ Buffer.alloc(s, 44100 * ~dur, 1).bufnum.postln }); // a ~dur second 1 channel Buffer

i = 10 ;
~breathed = SampleListGenerator.fromPath ("/audioRumentario/clean/breathed") ;
~breathed.do({ arg name ;
	Buffer.read(s, "/audioRumentario/clean/breathed/"++name++".wav", bufnum: i) ;
	i = i+1
	}) ;

~rey = SampleListGenerator.fromPath ("/audioRumentario/clean/perc/reyong") ;
~rey.do({ arg name ;
	Buffer.read(s, "/audioRumentario/clean/perc/reyong/"++name++".wav", bufnum: i) ;
	i = i+1
	}) ;


~dom = SampleListGenerator.fromPath ("/audioRumentario/clean/perc/domestica") ;
~dom.do({ arg name ;
	Buffer.read(s, "/audioRumentario/clean/perc/domestica/"++name++".wav", bufnum: i) ;
	i = i+1
	}) ;

~dob = SampleListGenerator.fromPath ("/audioRumentario/clean/perc/dobaichi") ;
~dob.do({ arg name ;
	Buffer.read(s, "/audioRumentario/clean/perc/dobaichi/"++name++".wav", bufnum: i) ;
	i = i+1
	}) ;

("Max indices of buffers (i): "++i.asString).postln ;

)



(

d = SynthDef.new("PlayBuf_0", 
	{ arg out, bufnum, rate, trig, mul ;
	// PlayBuf numChannels must be fixed
	var dur = 1 ;
	Out.ar(out, 
		PlayBuf.ar(1, In.ar(bufnum), In.ar(rate), In.ar(trig), 0, 0)
		* In.ar(mul)) 
	}
).send(s) ;

// 8-track mixer
m = SynthDef(\mix8, { arg out, in1, in2, in3, in4, in5, in6, in7, in8, pos, level ;
	Out.ar(out, Pan2.ar(Mix.new([
		In.ar(in1), In.ar(in2), In.ar(in3), In.ar(in4), 
		In.ar(in5), In.ar(in6), In.ar(in7), In.ar(in8)
	]), In.ar(pos), In.ar(level))
	)
}).send(s) ;


t = BabaWrapper(Impulse).send(s) ;
)

b = BabaPatcher("Blob!") ;

(
Routine.new({
10.do({ |i|
	var name = ("imp"++i.asString).asSymbol ;
	b.add(t, name) ;
	0.05.wait ;
	b.setList(name, [\freq, 6+i, \mul, 0.5]) ;
}) ;
"done".postln ;
}).play(AppClock) ;
)

(
Routine.new({
10.do({ |i|
	var name = ("play"++i.asString).asSymbol ;
	b.add(d, name) ;
	0.05.wait ;
	b.setList(name, [\bufnum, 10+i, \rate, 1, \mul, 0.5]) ;
}) ;
"done".postln ;
}).play(AppClock) ;
)


b.add(m, \mix8) ;
b.setList(\mix8, [\level, 1, \pos, 0.5]);

b.gui(3, 800) ;
b.roar ;

// controller
x = BabaXSession(b).gui ;
