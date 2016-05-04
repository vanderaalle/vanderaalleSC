// real draft
// just to recover functionaliity

StereoMixer {

	var <>busList, <>synth, <>chanList ;


	*new { arg busList, chanList;
		^super.new.initStereoMixer(busList, chanList)
	}

	initStereoMixer { arg aBusList, aChanList ;
		Server.local.waitForBoot{
			{
		busList = aBusList ;
				chanList = aChanList ;
				case
				{chanList.isNil}{chanList = Array.fill(16, 1)}
				{chanList.size < 16 } {chanList =
					chanList++Array.fill(16-chanList.size, 1)}
				{chanList.includes(nil)}
				{chanList = chanList.collect{|i| if (i.isNil){1}{i}}};
				chanList.postln ;

		SynthDef(\mix, { arg in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11,
			in12, in13, in14, in15, in16,
			pan1 = 0, pan2 = 0, pan3 = 0, pan4 = 0, pan5 = 0, pan6 = 0, pan7 = 0,
			pan8 = 0, pan9 = 0, pan10 = 0, pan11 = 0,
			pan12 = 0, pan13 = 0, pan14 = 0, pan15 = 0, pan16 = 0,
			vol1 = 1, vol2 = 1, vol3 = 1, vol4 = 1, vol5 = 1, vol6 = 1, vol7 = 1,
			vol8 = 1, vol9 = 1, vol10 = 1, vol11 = 1,
			vol12 = 1, vol13 = 1, vol14 = 1, vol15 = 1, vol16 = 1,

			main = 1, out = 0 ;
			var arr = [
						Pan2.ar(In.ar(in1, chanList[0])*vol1, pan1),
			Pan2.ar(In.ar(in2, chanList[1])*vol2, pan2),
			Pan2.ar(In.ar(in3, chanList[2])*vol3, pan3),
			Pan2.ar(In.ar(in4, chanList[3])*vol4, pan4),
			Pan2.ar(In.ar(in5, chanList[4])*vol5, pan5),
			Pan2.ar(In.ar(in6, chanList[5])*vol6, pan6),
			Pan2.ar(In.ar(in7, chanList[6])*vol7, pan7),
			Pan2.ar(In.ar(in8, chanList[7])*vol8, pan8),
			Pan2.ar(In.ar(in9, chanList[8])*vol9, pan9),
			Pan2.ar(In.ar(in10, chanList[9])*vol10, pan10),
			Pan2.ar(In.ar(in11, chanList[10])*vol11, pan11),
			Pan2.ar(In.ar(in12, chanList[11])*vol12, pan12),
			Pan2.ar(In.ar(in13, chanList[12])*vol13, pan13),
			Pan2.ar(In.ar(in14, chanList[13])*vol14, pan14),
			Pan2.ar(In.ar(in15, chanList[14])*vol15, pan15),					Pan2.ar(In.ar(in16, chanList[15])*vol16, pan16)
			] ;
			Out.ar(out,
				Mix.new(arr)*main
			)
			}).add ;
		Server.local.sync ;
		synth = Synth.tail(Server.local,\mix) ;
		Server.local.sync ;
		busList.do{ |bus, i|
			synth.set(("in"++(i+1).asString).asSymbol, bus.index)
			}
			}.fork
	}
	}


	randPan { arg min = -1, max = 1 ;
		16.do{|i| synth.set(("pan"++i.asString).asSymbol, rrand(min, max)) }
	}

	main { arg val ; synth.set(\main, val) }

	mute { arg in ;
		synth.set((\in++(in.asString)).asSymbol, 0) //dirty boy
		}

	unmute { arg in ;
		synth.set((\in++(in.asString)).asSymbol, busList[in-1])
		}

	muteAll { Array.series(16,1).do{|i|this.mute(i)} }

	unmuteAll { Array.series(16,1).do{|i|this.unmute(i)} }

	zero { arg in ;
		synth.set((\vol++(in.asString)).asSymbol, 0)
		}

	one { arg in ;
		synth.set((\vol++(in.asString)).asSymbol, 1)
		}


	vol { arg in, val ;
		synth.set((\vol++(in.asString)).asSymbol, val)
		}

	zeroAll { Array.series(16,1).do{|i|this.zero(i)}}
	oneAll { Array.series(16,1).do{|i|this.one(i)}}


	solo { arg in ;
		var arr = Array.series(16, 1) ;
		arr.remove(in) ;
		arr.do{|i| this.mute(i)}
	}

	unsolo { arg in ;
		var arr = Array.series(16, 1) ;
		arr.remove(in) ;
		arr.do{|i| this.unmute(i)}
	}

	out { arg val = 0 ; synth.set(\out, val)}
}


StereoMixerGui {

	var <>busMixer, <>window, <>slArr, <>btArr, <>recButt, <>mainSl ;
	var <>stetho, <>bck;

	*new { arg busMixer ;
		^super.new.initBusMixerGui(busMixer)
		}


	initBusMixerGui { arg aBusMixer ;
		busMixer = aBusMixer ;
		window = Window("Bus Mixer", Rect(100, 600, 20* busMixer.busList.size+20+50+50+500, 320)).front ;
		// record and read
		Button(window, Rect(10,30, 40, 30)).states_([["Record", Color.white, Color.red]])
			.action_{ busMixer.write}.font_(Font("Futura", 10))  ;
		StaticText(window, Rect(10, 80, 100, 30 )).string_("Bus Mixer").font_(Font("Futura", 10)) ;

		NumberBox(window, Rect(20, 145, 20, 20)).font_(Font("Futura", 10))
			.action_{|me| busMixer.out(me.value)} ;

		StaticText(window, Rect(22, 160, 100, 30 )).string_("out").font_(Font("Futura", 10)) ;

		Button(window, Rect(10,30+180, 40, 30)).states_([["Read", Color.red, Color.white]])
			.action_{ busMixer.read; this.refresh}.font_(Font("Futura", 10))  ;

		mainSl = Slider(window, Rect(20* busMixer.busList.size+20+50, 10, 30, 250))
			.value_(0.ampdb.linlin(-96, 6, 0, 1))
			.action_{|me| busMixer.main(me.value.linlin(0,1, -96, 6).dbamp)} ;
		StaticText(window, Rect(20* busMixer.busList.size+20+50, 10+250+10, 30, 15)).string_("Main").font_(Font("Futura", 10)) ;
		busMixer.busList.do{|b, i|
			StaticText(window, Rect(i*20+10+50, 10+250+10+20, 15, 15))
			.font_(Font("Futura", 10)).string_((i+1).asString)  ;
			slArr = slArr.add( Slider(window, Rect(i*20+10+50, 10, 15, 250))
				.action_{|me| busMixer.vol(i+1, me.value.linlin(0,1, -96, 3).dbamp)}) ;
			busMixer.synth.get(("in"++(i+1)).asSymbol, {|val| {slArr[i].value_(val.ampdb.linlin(-96, 3, 0,1))}.defer });

			btArr = btArr.add(Button(window, Rect(i*20+10+50, 10+250+10, 15, 15))
				.states_([["",Color.white, Color.green], ["M", Color.black, Color.grey]])
				.action_{|me| if (me.value == 1) {busMixer.mute(i+1)}{busMixer.unmute(i+1)}}
				)} ;
//		window.onClose_{
//			busMixer.busList.do{|b| b.free};
//			busMixer.synthArr.do{|sy| sy.free} } ;

		bck = CompositeView.new(window, Rect(20* busMixer.busList.size+20+50+50, 10, 475, 250)) ;
		stetho = 	QStethoscope2.new(Server.local,6, index: 0, view: bck) ;
		stetho.view.bounds_(Rect(0, 0, 450, 250))
		//	window.onClose_{ stetho.free } ;
		//CmdPeriod.doOnce{ window.close } ;


	}

	newStetho {
		bck.remove ;
		bck = CompositeView.new(window, Rect(20* busMixer.busList.size+20+50+50, 10, 475, 250)) ;
		stetho = 	QStethoscope2.new(Server.local,6, index: 0, view: bck) ;
		stetho.view.bounds_(Rect(0, 0, 450, 250))
	}

	refresh {
		mainSl.value_(busMixer.vol.ampdb.linlin(-96, 3, 0,1) );
		busMixer.synthArr.do{|sy, i|
			sy.get(\mul, {|val| {slArr[i].value_(val.ampdb.linlin(-96, 3, 0,1))}.defer })
		};
	}

}