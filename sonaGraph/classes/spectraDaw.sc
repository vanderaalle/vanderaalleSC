// a spectrum (spec) is simply a sonagram (2D, but not array2D)
// array without other data (no rate, pitch ect)
// n bins of 88 values, where value is db and index pitch

// here we use only class methods
// data are constantly returned = ytou always get a spec

SD : SpectraDaw {} // just for shortcut

SpectraDaw {

	var <>spec ;

	// start with an array of amps
	*from { arg sonaAmp, thresh = -96 ;
		^this.ampAbove(sonaAmp, thresh)
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

	*flatAmp {|spec, db = -40|
		^spec.collect{|p|
			p.collect{|deb| if(deb != -96){db}{-96}}
		}
	}

	// amp filtering out
	*ampAbove {|spec, thresh = -30|
		^spec.collect{|p|
			p.collect{|db| if(db > thresh){db}{-96}}
		}
	}
	*ampBelow {|spec, thresh = -30|
		^spec.collect{|p|
			p.collect{|db| if(db < thresh){db}{-96}}
		}
	}

	// pitch filtering out
	*pitchAbove {|spec, pitch|
		var t = pitch-21 ;
		^spec.collect{|i, j|
			i.collect{|p, n|
				if(n > t){p}{-96}
			}
		}
	}
	*pitchBelow {|spec, pitch|
		var t = pitch-21 ;
		^spec.collect{|i, j|
			i.collect{|p, n|
				if(n < t){p}{-96}
			}
		}
	}
	*pitchRange {|spec, lo, hi|
		^this.pitchBelow(this.pitchAbove(spec, lo), hi)
	}


	// you pass an array of pitches. Only those are kept
	*pitchMask {|spec, mask|
		var m = mask-21 ;
		^spec.collect{|i, j|
			i.collect{|p, n|
				if(m.includes(n)){p}{-96}
			}
		}
	}

	// symmetrical excision operations
	*cut {|spec, from, to|
		^spec[from..to]
	}
	*remove {|spec, from, to|
		^(spec[0..from]++spec[to..(spec.size-1)])
	}

	*silence {|bins|
		^Array.fill(bins, { Array.fill(88, {-96})})
	}

	*insertSilence {|spec, which, bins|
		^(spec[0..which]++this.silence(bins)++spec[which..(spec.size-1)])
	}

	*cancel {|spec, from, to|
		^(spec[0..from]++this.silence(to-from)++spec[to..(spec.size-1)])
	}

	*removeAt {|spec, bin, pitch|
		^spec.collect{|i, j|
			i.collect{|p, n|
				if((j == bin).and((n+21) == pitch)){-96}{p}
			}
		}
	}

	*insertAt {|spec, bin, pitch, amp = 0|
		^spec.collect{|i, j|
			i.collect{|p, n|
				if((j == bin).and((n+21) == pitch)){amp}{p}
			}
		}
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

	// conversion, assuming 4/4
	// we assume to start from 1 in music
	*barbin {|measure, quarter, sixthteen|
		^((measure-1)*16)+((quarter-1)*4)+(sixthteen-1)
	}
	*binbar {|bins|
		var meas = (bins/16).trunc.asInteger ;
		var quarter = ((bins-(meas*16))/4).asInteger ;
		var sixthteen = bins - (meas*16)-(quarter*4) ;
		^[meas+1, quarter+1, sixthteen+1]
	}

	// returns duration
	dur {|spec, rate, asTimeString = false|
		if (asTimeString){
			^((spec)*rate.reciprocal).asTimeString
		}{
			^(spec)*rate.reciprocal
		}
	}

	// archiving spec
	*writeArchive {|spec, path|
		spec.writeArchive(path) ;
	}
	*readArchive {|path|
		^Object.readArchive(path)
	}

	// as event list support
	// convert into a csound score like format

	*eventsFromVoice {|voice|
		var evList = [] ;
		var ev ;
		var v = Pseq([-96]++voice).asStream ; // init
		var act = v.next;
		var next;
		var dur, att ;
		var i = 0 ;
		while{ act.notNil }{
			next = v.next.post ; ": ".post ;
			case
			{ (act == -96) && (next == -96) }
			{ //"no event detected".postln ;
				dur = nil ;
			}
			{ (act == -96) && (next != -96) }
			{ "NEW event started".postln ;
				att = i ;
				dur = 1 ;
			}
			{ (act != -96) && (next != -96) && (next.notNil) }
			{ "KEEPing event".postln ;
				dur = dur+1
			}
			{ (act != -96) && ((next == -96)||(next.isNil)) }
			{ "END event".postln ;
				evList = evList.add([att, dur])
			} ;
			i = i+1 ;
			act = next ;
		} ;
		^evList
	}

	*writeEventList {|evList, path|
		evList.writeArchive(path)
	}
	*readEventList {|path|
		^Object.readArchive(path)
	}

	*eventsFromVoices {|spec|
		^spec.flop.collect{|p|
			this.eventsFromVoice(p)
		}
	}

	// here we have an idea of bins into evList
	*getMaxExt {|evList|
		var max = 0 ;
		evList.select{|v|v != [] }
		.do{|v| v.do{|e| if((e[0]+e[1]) > max){ max = e[0]+e[1] }} };
		^max
	}

	*playEvents {|evList, rate, boost, from = 0|
		var cnt = from ;
		^{
			(this.getMaxExt(evList)-from).do{
				evList.do{|i, j|
					if(i != []){
						i.do{|ev|
						if(ev[0] == cnt){
							Synth(\mdaPiano,
							[\freq, (j+21).midicps,
								\mul, (boost).dbamp]
								)
							}
						}
					}
				} ;
				cnt = cnt +1 ;
				rate.reciprocal.wait ;
			}
		}.fork
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

SD.binbar(4)
SD.barbin(1, 1, 1)
*/
