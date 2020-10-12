
+ SpectraDaw {


	*audioToSpec {|audioArr, minPitch = 21, maxPitch = 109|
		var spec = audioArr.collect{|i|
			Array.fill(88, {|j|
				if( j== i.linlin(-1.0, 1.0, minPitch-21, maxPitch-21).asInteger){0}{-inf}
			})
		} ;
		^spec
	}


	*fromAudio {|func, dur = 0.01, size = 100, minPitch = 21, maxPitch = 109|
		if (Server.local.serverRunning.not){"First boot the server!".postln}
			{
				func.loadToFloatArray(dur,
				action: { arg array;
						sigArr = SD.audioToSpec(array.resamp0(size), minPitch, maxPitch)
			});
		} ;
	}

}

/*
SonaGraph.prepare ; // we need mdaPiano

SD.fromAudio({LFPulse.ar(Line.kr(50, 5, 1), mul:0.5)+Line.ar(0, 0.5, 1)}, 1, 100, 36, 96) ;
d = SD.sigArr

SD.fromAudio({SinOsc.ar(1)}, 1, 100, 28, 40)
e = SD.sigArr

SpectraDawGui(SD.mix([d, e]), 4).boost_(-12).rate_(10).makeGui
SD.play(d.sigArr, 10, -12)


~arr = [] ;
{
	6.do{|i|
		SD.fromAudio({SinOsc.ar(0.1+Line.kr(0, i*0.25, 1))}, 1, 500, 28+(i*5), 28+36+(i*5));
		1.2.wait ;
		~arr = ~arr.add(SD.insertEvery(SD.sigArr, 18-(i*2))) ;
		0.1.wait
		;
	};
	"done".postln
}.fork

SpectraDawGui(SD.mix(~arr), 4).boost_(-12).rate_(8).makeGui
SD.play(d.sigArr, 10, -12)


~arr = [] ;
{
	6.do{|i|
		SD.fromAudio({LFPulse.ar(1+Line.kr(0, i*0.25, 1), mul:LFPar.ar(0.3+(i*0.2)))}, 1, 500, 28+(i*5), 28+36+(i*5));
		1.2.wait ;
		~arr = ~arr.add(SD.insertEvery(SD.sigArr, 15-(i*2))) ;
		0.1.wait
		;
	};
	"done".postln
}.fork

SpectraDawGui(SD.mix(~arr), 4).boost_(-12).rate_(8).makeGui



~arr = [] ;
{
	6.do{|i|
		SD.fromAudio({LFPulse.ar(1+Line.kr(0, i*0.25, 1),0, 0.8)+LFNoise0.ar(100,0.2)}, 1, 500, 28+(i*5), 28+36+(i*5));
		1.2.wait ;
		~arr = ~arr.add(SD.insertEvery(SD.sigArr, 15-(i*2))) ;
		0.1.wait
		;
	};
	"done".postln
}.fork

SpectraDawGui(SD.mix(~arr), 4).boost_(-12).rate_(8).makeGui


*/

