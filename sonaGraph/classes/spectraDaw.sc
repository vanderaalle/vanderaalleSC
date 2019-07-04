// a spectrum (spec) is simply a sonagram (2D, but not array2D)
// array without other data (no rate, pitch ect)
// n bins of 88 values, where value is db and index pitch

// here we use only class methods
// data are constantly returned

SD : SpectraDaw {} // just for shortcut

SpectraDaw {

	var <>spec ;

	// start with an array of amps
	*from { arg sonaAmp, thresh = -96 ;
		^this.filterAbove(sonaAmp, thresh)
	}

	// mix a an array of spectra
	// returns a spectrum
	// we use lin or it's a mess
	*mix {|specArray|
		var size = specArray.collect{|i| i.size}.sort.last ;
		var mix = Array.fill(size, { Array.fill(88, {0}) }) ;
		var zeroPadded ;
		specArray = specArray.collect{|spec| this.toLinear(spec)} ;
		zeroPadded =
		specArray.collect{|spec|
			if (spec.size < size) { this.zeroPad(
				spec, size-spec.size, \toTail, \lin)}
			{spec}
		} ;
		zeroPadded.do{|spec|
			mix = mix+spec
		} ;
		// removeInf to respect usual format
		^(this.removeInf(mix.ampdb))
	}


	*mount {|specArray, posArray|
		var newSpec ;
		if(specArray.size != posArray.size)
		{"mismatch between spec and pos arrays".postln } ;
		newSpec = specArray.collect
		{|i,j| this.zeroPad(i, posArray[j], \toHead)};
		^this.mix(newSpec)
	}

	*toLinear {|spec|
		^spec.collect{|i|
			i.collect{|j|
				if (j== 96.neg){0}{j.dbamp}
			}
		}
	}

	*removeInf {|spec|
		^spec.collect{|i|
			i.collect{|j|
				if (j== inf.neg){-96}{j}
			}
		}
	}

	// offset a spec filling with zero values at beginning
	// toHead or toTail, dB o lin
	*zeroPad { |spec, howMany, where = \toHead, type = \dB|
		var val = if (type == \dB){-96}{0} ;
		var pad = Array.fill(howMany, { Array.fill(88, {val})} ) ;
		if(where == \toHead){
			^(pad++(spec))
		}{
			^(spec++(pad))
		}
	}
	//
	// zeroPad { |howMany, where = \toHead, type = \dB|
	// 	var val = if (type == \dB){-96}{0} ;
	// 	var pad = Array.fill(howMany, { Array.fill(88, {val})} ) ;
	// 	if(where == \toHead){
	// 		^(pad++(spec))
	// 	}{
	// 		^(spec++(pad))
	// 	}
	// }


	// concatenate a sequence of spectra
	*concatenate {|specArray|
		var conc = [] ;
		specArray.do{|spec|
			conc = conc++spec
		} ;
		^conc
	}

	// an error checker
	*check {|spec|
		spec.do{|i, j|
			if (i.size != 88){
				"error in bin".post; j.post; " : values are ".post; i.size.postln
			} ;
		}
	}

	*filterAbove {|spec, thresh = -30|
		^spec.collect{|p|
			p.collect{|db| if(db > thresh){db}{-96}}
		}
	}

	*cut {|spec, from, to|
		^spec[from..to]
	}

	// shifts spec of interval, what happens when lower or higher?
	*transpose { |spec, interval|
		var block = Array.fill(interval.abs, {-96}) ;
		case
		{ interval == 0 }
		{ "unmodified".postln; ^spec }
		{ interval.sign == 1}
		{ ^spec.collect{|p| (block++p)[..87]}}
		{ interval.sign == -1}
		{ ^spec.collect{|p| (p++block)[(interval.abs)..]}}
	}

	// delete last of every n, a time compression
	*deleteEvery {|spec, every = 2|
		^spec.clump(every).collect{|i| i[..every-2]}.flatten
	}

	// insert a new block every n,  empty or duplicating last
	// empty or fill
	*insertEvery {|spec, every = 2, how = \empty|
		^spec.clump(every).collect{|i|
			if(how == \empty){
				i++[Array.fill(88, {-96})]
			}{i++[i.last]}
		}.flatten
	}

	*reverse {|spec|
		^spec.reverse
	}

	*stretch {|spec, factor = 1|
		^spec.collect{|i|
			Array.fill(factor, {i})
		}.flatten
	}

	*play {|spec, rate, boost = 15, from = 0|
		^{
			var playing = [] ;
			spec[from..].do{|chord|
				chord.do{|dB, i|
					if ((playing.includes(i).not).and(dB > -96)){
						Synth(\mdaPiano,
							[\freq, (i+21).midicps,
								\mul, (dB+boost).dbamp]
					) } ;
				} ;
				playing = [] ;
				chord.do{|i, j| if(i > -96){playing = playing.add(j)}} ;
				rate.reciprocal.wait
			}
		}.fork ;

	}

}

/*
// here we start up server and defs
SonaGraph.prepare ;

// something to analyze, i.e a buffer
~path = Platform.resourceDir +/+ "sounds/a11wlk01.wav";
~sample = Buffer.read(s, ~path).normalize ;

// an istance
a = SonaGraph.new ;
// now analyzing in real-time
a.analyze(~sample,15) ; // rate depends on dur etc

// easy way to select
g = SonaGraphGui.new(a, ~sample, hStep:5, vStep:6).makeGui(-40) ;
g.select(40, 55)


n = SpectraDaw.from(g.selected, -30)
SD.check(n)
//n.filterAbove
SD.play(n, 4, 20)
n.spec.size

g.select(0, 40)
m = SpectraDaw.from(g.selected, -30)

SD.play(m, 4, 20)
m.do{|i| i.postln}
m.size

k = SD.mix([n, m])

k.size
k = SD.from(k)
k.do{|i| i.postln}
SD.play(k, 4, 20)

k.size
k.do{|i| i.trunc.postln}


k = SD.from(SD.concatenate([m, n]))
SD.play(k, 4, 15)
j = SD.transpose(m, 12)
SD.play(j, 4, 20)

SD.check(j)
SD.play(n, 4, 20)
l = SD.transpose(n, -12)
SD.check(l)

SD.play(l, 4, 15)

k = SD.mount([n, n, n], [0, 10, 20])
SD.play(k, 4, 15)
SpectraDawGui(k, 4).makeGui

g.select(0, 40)
m = SpectraDaw.from(g.selected, -30)

z = SD.deleteEvery(m,4)
SpectraDawGui(z, 4).makeGui
z = SD.deleteEvery(z)

z = SD.insertEvery(m,2, \fill)
SpectraDawGui(z, 4).makeGui
z = SD.insertEvery(z,2, \empty)
SD.check(z)
*/
