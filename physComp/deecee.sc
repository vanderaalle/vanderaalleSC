DeeCeeOLD {
	
	var <>synth ;
	var <>offset, <>arr ;
	
	*new { arg offset = 2 ; 
		^super.new.initDeeCee(offset) 	
	}

	initDeeCee { arg theOff ;
		offset = theOff ;
		arr = [] ;
		Array.series(16).do{|i|
			var a = [] ;
			i.asBinaryString(4).do{|j| a = a.add(j.asString.asInteger) } ;
			arr = arr.add(a.reverse) // depends on blackbox
		} ;
		this.createSynth 
	}
	
	createSynth {
		Server.local.waitForBoot{
			{
		SynthDef(\deecee, { arg v1 = 0, v2 = 0, v3 = 0, v4 = 0, offset = 2 ;
			Out.ar(offset, K2A.ar([v1,v2,v3,v4]));
			}).add ;
			Server.local.sync ;
			synth = Synth(\deecee, [\offset, offset])	 
				}.fork
		}
	}
	
	select { arg which = 1;
		synth.setn(\v1, arr[which])
		}
	
	// should be a shortcut to select the reset
	zero { this.select(0) }  	
}


DeeCee {
	
	var <>synth ; 
	var <>offset, <>arr ;
	
	*new { arg offset = 2 ; 
		^super.new.initDeeCee(offset) 	
	}

	initDeeCee { arg theOff ;
		offset = theOff ;
		arr = [] ;
		Array.series(16).do{|i|
			var a = [] ;
			i.asBinaryString(4).do{|j| a = a.add(j.asString.asInteger) } ;
			arr = arr.add(a.reverse) // depends on blackbox
		} ;
		this.createSynth 
	}
	
	createSynth {
		Server.local.waitForBoot{
			{
		SynthDef(\deecee, { arg v1 = 0, v2 = 0, v3 = 0, v4 = 0, offset = 2 ;
			Out.ar(offset, K2A.ar([v1,v2,v3,v4]));
			}).add ;
				}.fork
		}
	}
	
	select { arg which = 1;
		{
		if (synth.notNil) {synth.free} ; // prevents parallelism 
		Server.local.sync ;
		synth = Synth(\deecee, [\offset, offset, \v1, arr[which]])
 		}.fork;
	}
	
	
	event { arg which = 1, dur = 1 ;
		{
		synth = Synth(\deecee, [\offset, offset, \v1, arr[which]]);
		dur.wait ; synth.free
		}.fork
	}
	
	reset { if (synth.notNil) {synth.free}  }

	zero { this.select(0) }  	
}


/*


x = DeeCee.new; 
x = DeeCee.new(2+4); 

x.arr[0] = [1,1,1,1]*0.1
x.select(14)

x.zero

x.select(0)

x.select(1)
x.select(2)
x.select(3)
x.select(4)
x.select(5)
x.select(6)
x.select(7)
x.select(8)
x.select(9)
x.select(10)
x.select(11)
x.select(12)
x.select(13)
x.select(14)
x.select(15)
x.select(0)


{inf.do{x.select(rrand(1,5)); (0.01*[0.25, 0.5, 1]).postln.choose.wait}}.fork

// motors
{inf.do{x.select(Array.series(10, 6).choose.postln); (0.01*[0.25, 0.5, 1]).postln.choose.wait}}.fork

{inf.do{x.event(Array.series(10, 6).choose.postln,  0.02); (Array.series(8,1)*0.0625).choose.wait}}.fork

//////


{inf.do{|i|n = rrand(1,5); x.select(n); 0.05.wait ;x.select(15);
	(0.2*[0.25, 0.5, 1]).postln.choose.wait; }}.fork

{inf.do{|i|n = 2; x.select(n); 0.05.wait ;x.select(15);
	[0.25].postln.choose.wait; }}.fork


*/
