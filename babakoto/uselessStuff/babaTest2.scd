s.reboot ;

d = BabaWrapper(Pulse).send(s) ;


(
b = BabaPatcher("Blob!") ;
Routine.new({
10.do({ |i|
	var name = ("pulse"++i.asString).asSymbol ;
	b.add(d, name) ;
	0.05.wait ;
	b.setList(name, [\freq, 48, \mul, 0.5, \width, 0.5]) ;
}) ;
"done".postln ;
}).play(AppClock) ;
)



x = BabaXSession(b).gui ;
b.ui(4, 850, x) ;

(
r = {
	inf.do{
	b.set(~synth, \freq, rrand(~min, ~max)) ;
	b.set(~synth, \width, rrand(0.01, 0.5)) ;
	~how.wait ;
	}
	
} ;

t = Tasker(b).addTask("rnd", r, [\synth, \pulse0, \min, 0.1, \max, 1000, \how, 0.2]) ;
h = TaskerGui(t) ;
h.makeAllGui ;

)