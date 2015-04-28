Spectrum {

	var <>oscFunc ; // the osc receiver
	var <>synth ; // the analyser
	var <>bus ; // the bus
	var <>out ; // default you hear the sound
	var <>spectrum ; // an array where we put the spec data
	var <>bandWidth ; // freq window in Hz
	var <>linearBands ; // private
	var <>maxHerz ; // max herz
	var <>sonogram ;// an array where we collect spec arrays
	var <>update ; // update freq for spec
	var <>gram ; // boolean for spectrum collection

	*new { arg bandWidth = 50, maxHerz = 20000, update = 10 ;
		^super.new.initSpectrum(bandWidth, maxHerz, update)
	}

	initSpectrum { arg aBandWidth, aMaxHerz, aUpdate ;
		{
			out = 0 ;
			bandWidth = aBandWidth ; // default = 50 Hz
			maxHerz = aMaxHerz ;
			linearBands = maxHerz/bandWidth ;
			update = aUpdate ;
			gram = false ;
			Server.local.sync ;
			bus = Bus.audio(Server.local, 1) ;
			SynthDef(\bank, {arg in = 0, out = 0 ;
				var amp;
				var source = In.ar(in,1) ;
				//source = SinOsc.ar(60.poll.midicps)*10.dbamp ;
				amp = Array.fill(linearBands, {|i|
					Lag.kr(Amplitude.kr(
						BPF.ar(source, (i+1)*maxHerz/linearBands, 0.01))
				).ampdb}) ;
				Out.ar(out, source) ;
				SendReply.ar(Impulse.ar(update), '/amp', values:  amp)
			}).add ;
			Server.local.sync ;
			synth = Synth(\bank, [\in, bus, \out, out], addAction:\addToTail) ;
			Server.local.sync ;
			oscFunc = OSCFunc({ |msg| spectrum = msg[3..] ;
				if (gram) { sonogram = sonogram.add(spectrum) }
			}, '/amp');
		}.fork
	}

	plot { arg name, bounds, discrete=false, numChannels, minval, maxval;
		this.spectrum.plot(name, bounds, discrete=false, numChannels, minval, maxval)
	}

}


SpectrumGUI {

	var <>spectrum, <windowDur, <>update ;
	var <>w,<>u,<>gr, <>column, <>width ;
	var <>task ;

	*new { arg spectrum, windowDur = 30 ;
		^super.new.initSpectrumGUI(spectrum, windowDur)
	}

	initSpectrumGUI { arg aSpectrum, aWindowDur ;
		spectrum = aSpectrum ;
		windowDur = aWindowDur ;
		width = 600 ;
		column = 0 ;
		update = width/windowDur ;
		w = Window.new("spectrum", Rect(10, 10, width, spectrum.linearBands)).front ;
		u = UserView(w,Rect(0, 0, width, spectrum.linearBands)) ;
		gr = UserView(w,Rect(0, 0, width, spectrum.linearBands)) ;
		gr.drawFunc_{
			10.do{|i|
				Pen.fillColor_(Color.green) ;
				Pen.strokeColor_(Color.red) ;
				Pen.width_(1) ;
				Pen.stringAtPoint((i*windowDur*0.1).asString, (i*60) @ 20, Font("Helvetica", 8), Color.red) ;
				Pen.line((i*60) @ 0, (i*60) @ spectrum.linearBands);
				Pen.stroke;
				Pen.stringAtPoint((i*spectrum.maxHerz*0.1).asString,
					5 @ (spectrum.linearBands-(i*spectrum.linearBands*0.1)),
					Font("Helvetica", 8), Color.red) ;
				Pen.line(0 @ (i*spectrum.linearBands*0.1), 600 @ (i*spectrum.linearBands*0.1));
				Pen.stroke;
				Pen.fill ;
			}
		} ;
		u.background_(Color.white) ;
		u.drawFunc = {
			spectrum.spectrum.reverse.do{|it, ind|
				Pen.fillColor = Color.grey(it.linlin(-60, 0, 1,0));
				Pen.addRect(Rect(column%width, ind, 1, 1)) ;
				Pen.fill ;
			} ;
		} ;
		u.clearOnRefresh_(false) ;
		task = Task{
			inf.do{|i|
				{column = i; u.refresh}.defer ;
				update.reciprocal.wait ;
			}
		}
	}

	run { arg bool = true ;
		if(bool) {task.play}{task.pause}
	}

	windowDur_{ arg val = 30;
		windowDur = val ;
		update = width/windowDur ;
		gr.refresh ;
	}

}

/*
p = Spectrum.new

// various sources
y = {Out.ar(p.bus, Mix.fill(10, {|i|SinOsc.ar(400*i)}))}.play ;
y = {Out.ar(p.bus, Mix.fill(10, {|i|SinOsc.ar(i*LFNoise1.kr(0.5, 400, 800))}))}.play ;
y = {Out.ar(p.bus, Mix.fill(10, {|i|SinOsc.ar(i*LFNoise1.kr(0.1, 1000, 1500))}))}.play ;
y = {Out.ar(p.bus, SinOsc.ar(10000))}.play ;
y = {Out.ar(p.bus, SoundIn.ar) }.play

// you plot the actual spectrum here
p.plot


p.gram_(true)
p.sonogram.size
p.gram_(false)

// launch sonagraph
g = SpectrumGUI(p)
g.run ; // run is off by
g.windowDur_(20) // change window dur
g.run(false)
 y.free
*/