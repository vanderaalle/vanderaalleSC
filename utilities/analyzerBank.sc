// Outputs normalized values in range [0,1]

AnalyzerBankN {

	var <>names, <>defs, <>bank ;
	var <>synthDict, <>outDict, <>inBus ;
	var <>anaGui ;

	*new { arg inBus ;  // sending stuff
		^super.new.initAnalyzerBank(inBus)
		}

	initAnalyzerBank {arg aBus ;
		inBus = aBus ;
		synthDict = IdentityDictionary.new ;
		outDict = IdentityDictionary.new ;
		this.createSynths ;
	}

	createSynths {
		var name, stat ;
		{
		bank = Group.tail ;
		defs = [
		// pitch in midi, clipped [20, 120]
			SynthDef(\pitch, { arg in, out, rate = 5 ;
				var pt, hpt;
				#pt, hpt = Lag3.kr(Tartini.kr(In.ar(in))) ;
				Out.kr(out, pt
					.cpsmidi
					.clip(20, 120)
					.linlin(20,120, 0,1) ;
					) ;
			}).add,

			// amplitude in dB, clipped [-60, 0]
			SynthDef(\amplitude, { arg in, out ;
				var amp = Lag3.kr(Amplitude.kr(In.ar(in)))
					.ampdb
					.clip(-60, 0)
					.linlin(-60, 0, 0, 1);
				Out.kr(out, amp) ;
			}).add,

//			// Loudness
//			SynthDef(\loudness, {  arg in, out, rate = 5 ;
//				var loc = LocalBuf(1024, 1) ;
//				var sones, chain, input = In.ar(in) ;
//				chain = FFT(loc, input) ;
//				sones = Loudness.kr(chain) ;
//				Out.kr(out, sones) ;
//			}).add,
//
			// SpecCentroid --> brightness
			// centroid in midi, clipped [32, 126]
			SynthDef(\centroid, { arg in, out, rate = 5 ;
				var loc = LocalBuf(2048, 1) ;
				var centre, chain, input = In.ar(in) ;
				chain = FFT(loc, input) ;
				centre = Lag3.kr(SpecCentroid.kr(chain))
					.cpsmidi
					.clip(32, 126)
					.linlin(32,126, 0,1) ;
				Out.kr(out, centre) ;
			}).add,

			// SpecFlatness
			SynthDef(\flatness, { arg in, out ;
				var loc = LocalBuf(2048, 1) ;
				var flat, chain, input = In.ar(in) ;
				chain = FFT(loc, input) ;
				flat = Lag3.kr(SpecFlatness.kr(chain).log*10)
					.linlin(-45, -1.6, 0, 1) ;
				Out.kr(out, flat) ;
			}).add,
		 ] ;
		 	// statistic
			// correlation expresses if params changes in the same direction
			// maxVariation expresses max variation of one of the param in time
		defs = defs.add(
		SynthDef(\stat, { arg in1, in2, in3,in4, outCorr, outVar, rate = 60 ;
				var correlation, maxVariation, time ;
				var i1 = In.kr(in1) ;
				var i2 = In.kr(in2) ;
				var i3 = In.kr(in3) ;
				var i4 = In.kr(in4) ;
				time = 1/rate ;
				correlation = ((i1+i2+i3+i4)*0.25-0.5).abs*2 ;
				maxVariation = [
				i1-DelayN.kr(i1, time, time),
				i2-DelayN.kr(i2, time, time),
				i3-DelayN.kr(i3, time, time),
				i4-DelayN.kr(i4, time, time)
				] .abs; //.sum ;
				maxVariation = maxVariation[0].max(maxVariation[1]).max(maxVariation[2]).max(maxVariation[3]) ;
				Out.kr(outCorr, correlation) ;
				Out.kr(outVar, maxVariation) ;

			}).add ;
		) ;
	 	Server.local.sync ;
	 	defs.do{|def|
		 	name = def.name.asSymbol.postln ;
		 	outDict[name] = Bus.control(Server.local, 1) ;
		 	Server.local.sync ;
		 	synthDict[name] = Synth.newPaused(name, [\out, outDict[name], \in, inBus], bank, \addToHead) ;
		 	} ;
		 outDict[\correlation] = Bus.control(Server.local, 1) ;
		 outDict[\maxVariation] = Bus.control(Server.local, 1) ;
		 Server.local.sync ;
		 outDict.removeAt(\stat) ;
		 synthDict[\stat] = Synth.newPaused(\stat,
		 	[
		 	\outCorr, outDict[\correlation],
		 	\outVar, outDict[\maxVariation],
		 	\in1, outDict[\amplitude],
		 	\in2, outDict[\pitch],
		 	\in3, outDict[\centroid],
		 	\in4, outDict[\flatness]
		 	], bank, \addToTail) ;
		 Server.local.sync ;
		}.fork
	}

	run { arg name, flag = true ;
		synthDict[name.asSymbol].run(flag)
	}

	runArray { arg names, flag = true ;
		names.do{|name| this.run(name.asSymbol, flag)}
	}

	runAll { arg flag = true ;
		// bank.run(flag) ;
		defs.do{|def| this.run(def.name.asSymbol, flag)}
	}

	gui { arg rate; anaGui = AnalyzerBankNGui(this, rate:rate) }
}


AnalyzerBankNGui {

	var <>analyzer, <>keys ;
	var <>window, <>width, <>step, <>small ;
	var <>rate, <>pollTask, <>drawTask, <>polled, <>old ;

	*new { arg analyzer, step = 20, rate = 1/20 ;  // sending stuff
		^super.new.initAnalyzerBankNGui(analyzer, step, rate)
		}

	initAnalyzerBankNGui {arg anAnalyzer, aStep, aRate ;
		analyzer = anAnalyzer ;
		width = 300 ;
		step = aStep ;
		small = 10 ;
		rate = aRate ;
		keys = analyzer.outDict.keys.asArray ;
		polled = Array.fill(analyzer.outDict.keys.size, {0}) ; // init
//		old = Array.fill(analyzer.outDict.keys.size, {0}) ;
		this.makeWindow ;
		this.setTasks ;
	}


	makeWindow {
		var ind, val ;
		window = Window("Analyzer Bank", Rect(100, 100, 300+width, (analyzer.defs.size+1)*(step+small)+small )).front ;
		window.drawFunc = {
			keys.do{|key, ind|
				Pen.stringAtPoint(key.asString, 10@(step+small*ind+small), Font("Futura", 12), Color.black) ;
				Pen.stringAtPoint(polled[ind].round(0.01).asString, 450@(step+small*ind+small), Font("Futura", 12), Color.black) ;

				Pen.fillColor = Color.hsv(ind.linlin(0,keys.size, 0, 0.2), 0.95, 0.95);
				Pen.fillRect(Rect(100, step+small*ind+small, width* polled[ind], step ))
				} ;
		// variation
//		ind = keys.size ;
//		val = (old-polled).abs.maxItem ; // .sum/polled.size ;
//			Pen.stringAtPoint("maxVariation", 10@(step+small*ind+small), Font("Futura", 12), Color.black) ;
//			Pen.stringAtPoint(val.round(0.01).asString, 450@(step+small*ind+small), Font("Futura", 12), Color.black) ;
//			Pen.fillColor = Color.hsv(ind.linlin(0,keys.size, 0, 0.2), 0.95, 0.95);
//			Pen.fillRect(Rect(100, step+small*ind+small, width* val, step ))
//
		};
		window.onClose_{
			drawTask.pause ; pollTask.pause ;
			}

	}

	setTasks {
		drawTask = Task(
			{inf.do{
				{window.refresh}.defer ;
				rate.wait ;
			}
		}
		).start ;
		pollTask = Task(
			{
			inf.do{
				// old = polled.copy ;
			keys.do{|key, ind|
				analyzer.outDict[key].get{|v| polled[ind] = v} ;
				rate.wait ;
			}
			}
		}
		).start;

	}

}

/*


s.reboot ;
(
b = Bus.audio(s, 1); c = Bus.audio(s, 1) ;

x = {Out.ar(b, SinOsc.ar(1000))}.play ;
//y = {Out.ar(c, WhiteNoise.ar)}.play ;
y = {Out.ar(b, WhiteNoise.ar)}.play ;
y.free;
x.free
x = {Out.ar(b, Pulse.ar(1000))}.play ;
x = {Out.ar(b, LPF.ar(WhiteNoise.ar, MouseX.kr(20, 20000, 1)))}.play ;
x = {Out.ar(b, Pulse.ar(MouseX.kr(20, 20000, 1)))}.play ;



a = AnalyzerBankN(b);
a.runAll ;

a.run(\stat)
a.outDict[\pitch].get{|v|v.postln}
s.queryAllNodes

g.step
g.height
g = AnalyzerBankNGui(a)
g.analyzer.outDict[\pitch]

g.rate_(1/10)
*/