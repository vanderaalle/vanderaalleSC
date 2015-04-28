Burst {

	var <>inBus, <>outBus ;
	var <>store, <>alias ;
	var <>flag ; 	// flag allows to play only processed buffers
				// otherwise we can have unprocessed sound
	var <>vol ; 	// a general vol
		
	*new { arg inBus, outBus = 0 ; 
		^super.new.initBurst(inBus, outBus) 	
	}

	initBurst { arg aInBus, aOutBus ;
		inBus = aInBus ;
		outBus = aOutBus ;
		SynthDef(\record, { arg buf, in ;
		Ê Ê in = In.ar(in) ;
		Ê Ê RecordBuf.ar(in, buf, doneAction: 2, loop: 0);
		}).add;
		{
		store  = Buffer.alloc(Server.local, (Server.local.sampleRate*1).asInteger) ;
		Server.local.sync ;
		alias  = Buffer.alloc(Server.local, store.numFrames) ;
		}.fork ;
		flag = false ;
		vol = 1 ;
	}
	

	// direct DSP methods
	
	// PRIVATE 
	perm { arg arr, howMany;
		var a, b, index ;
		var blocks = (arr.size/howMany).trunc.asInteger ;
		var tail = FloatArray.new ;
		var permArr ;
		tail = arr[(blocks*howMany)..]  ;
		permArr = FloatArray.new ;
		a = Array.series((blocks/2).trunc.asInteger, 1,2) ; 
		b = Array.series((blocks/2).trunc.asInteger, 0,2) ; 
		index = [a,b].flop.flat ;
		howMany = howMany-1 ;
		index = if (blocks.even) {index} {index = index++(blocks-1)} ;
		index.do{|i| permArr = permArr++arr[i*howMany..(i+1)*howMany] } ;
		^permArr++tail
		} 


	// PUBLIC INTERFACE 	
	// arr: array to permute
	// freq: permutation period expr not in samples but in freq
	// tailFreq: incr/decr to freq for iteration
	// iter: how many times the process is iterated
	permIter { arg arr, freq = 20, tailFreq = 5, iter = 1 ;
		var howMany ;
		iter.do{|id|
			howMany =  (Server.local.sampleRate/(freq+(tailFreq*id))).asInteger;
			arr = this.perm(arr, howMany)
			} ;
		^arr	
		} 

	// just to verify what's in the buffer
	storePlay {store.play}

	// apply process 
	// sec is the duration of the fragment to be permuted
	// note that sec = delay
	process { arg sec = 3, freq = 20, tailFreq = 5, iter = 4 ;
		var place ;
		{
		alias  = Buffer.alloc(Server.local, store.numFrames) ;
		store.copyData(alias) ;
		Server.local.sync ;
		store.free ;
		Server.local.sync ;
		flag = false ;
		Server.local.sync ;
		// store is the place where we re/load samples
		store  = Buffer.alloc(Server.local, (Server.local.sampleRate*sec).asInteger) ;
		Server.local.sync ;
		Synth(\record, [\buf, store, \in, inBus], addAction:\addToTail) ; 
		Server.local.sync ;
		(sec+0.001).wait ;		
		store.loadToFloatArray(action: 
			{ arg array; place = this.permIter(array, freq, tailFreq, iter) ; 
				store.loadCollection(place);
				flag = true ; 
				alias.free ;
			});
		}.fork
	}

	play { 
		var which = if(flag) {store} {alias} ;
		{ Out.ar(outBus, vol * PlayBuf.ar(1, which,  doneAction:2))}.play 
	 }

}

/*


Server.local.reboot;
(
// here we read from file
~playBuf = Buffer.read(Server.local, "/Sonata1GMinorAdagio.aiff").normalize ; 
// we write on a bus

~in = Bus.audio(Server.local, 1) ;

~player = {arg out, buf; Out.ar(out, PlayBuf.ar(1, ~playBuf, loop:1))}.play(Server.local, args:[\out, ~in, \buf, ~playBuf]) ;

// or sinusoid
~freq = 300 ;
~sin = {arg out, freq = 400; Out.ar(out, SinOsc.ar(freq))}.play(Server.local, args:[\out, ~in, \freq, ~freq]) ;
)

// single burst
~burst = Burst(~in) ;
// fire!
~burst.process(2, 500, 123, 1) ;
~burst.play

// change freq
~sin.set(\freq, 1000)

(
// a set of firing burst, out on > 2
~burstArr = Array.fill(6, {|i| Burst(~in, 2+i)}) ;

// first we process 
6.do{|i|
	{
	var sec = rrand(0.3, 1) ;
	var freq = rrand(20, 100);
	var tail = rrand(5, 15) ;
	var iter = rrand(1,6) ;	
	inf.do{
		~burstArr[i].process(sec, freq, tail, iter) ;
		(sec+1).wait ; // time for processing is crucial
		}	
	}.fork
} ;

// then we play

{
	inf.do{|i| ~burstArr[i%6].play ; rrand(0.1, 0.5).wait }
}.fork ;

s.scope
)
	

*/