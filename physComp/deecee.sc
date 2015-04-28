// one synth based
DeeCee1 {

	var <>synth ;
	var <>offset, <>arr ;

	var <>serializer ; // a serial scheduler
	var <>fifo ; // its fifo, a mere list
	var <>evtDur ; // activation dur
	var <>scanPeriod ; // its fifo scan rate
	var <>dump ;
	var <>id ; // an optional unique ID in case of various DCs

	*new { arg offset = 2, id, evtDur = 0.0075, scanPeriod = 0.012 ;
		^super.new.initDeeCee(offset, id, evtDur, scanPeriod)
	}

	initDeeCee { arg theOff, anId, anEvtDur, aScanPeriod ;
		offset = theOff ;
		id = anId ;
		arr = [] ;
		evtDur = anEvtDur; scanPeriod = aScanPeriod ;
		Array.series(16).do{|i|
			var a = [] ;
			i.asBinaryString(4).do{|j| a = a.add(j.asString.asInteger) } ;
			arr = arr.add(a.reverse) // depends on blackbox
		} ;
		this.createSynth ;
		// evtDur = 0.007 ; // was 0.01
		// scanPeriod = 0.012 ; // check this against event dur! was 0.015
		fifo = [] ; // empty
		serializer = Task({
			var which ;
			inf.do{
				which = fifo[0] ;
				if (which.notNil) {this.event(which); fifo = fifo[1..]} ;
				scanPeriod.wait
			}
		}) ;
		dump = 15 ; // we dump to 15
	}

	createSynth {
		Server.local.waitForBoot{
			{
		SynthDef(\deecee, { arg v1 = 0, v2 = 0, v3 = 0, v4 = 0, offset = 2 ;
			Out.ar(offset, K2A.ar([v1,v2,v3,v4]));
			}).add ;
			Server.local.sync ;
			synth = Synth(\deecee, [\offset, offset]) ;
			Server.local.sync ;
			this.start ; this.addEvent(dump) ; // we protect against not addressed current
				}.fork
		}
	}

	select { arg which = 1; synth.setn(\v1, arr[which]) }

	toDump { this.select(dump) }
	// should be a shortcut to select the reset
	//zero { this.select(0) }
	// doesn't make sense! 0 is not a port

	// this might be private
	event { arg which = 1 ;
		// dur: empirical minimum time
		{
			this.select(which);
			evtDur.wait;
			this.select(dump); // we dump
		}.fork
	}


	addEvent { arg which; fifo = fifo.add(which).postln;
		this.changed(this, [id, which]);
	}

	// interface to serializer
	start { serializer.start }
	pause { serializer.pause }
	resume { serializer.resume }

}

// event based
// THIS IS THE OLDER IMPLEMENTATION
// UNSAFE if you use coils
DeeCee2 {

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

	event2 { arg which = 1, dur = 0.1, dump = 0, release = 0.001 ;
		{
			synth = Synth(\deecee, [\offset, offset, \v1, arr[which]]);
			Server.local.sync ;
			dur.wait;
			synth.free ;
			Server.local.sync ;
			synth = Synth(\deecee, [\offset, offset, \v1, arr[dump]]);
			release.wait;
			synth.free
 		}.fork;
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
