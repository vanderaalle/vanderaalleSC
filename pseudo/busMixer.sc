/*

Minimal bus mixer

The idea is that you create an array of busses
Then you pass it to BusMixer, at the end of the chain.
You get GUI for free

Andrea Valle, 12/06/2012, for the Pseudo project
*/


BusMixer {

	var <>busArr, <>numChan ;
	var <>synthArr, <>mulArr ;
	var <out, <>monoBus, <>monoSynth ;
	var <>path ;
	var <vol, <scale ; // scale is for scaling vol without loosing its main val
	var <>mixGui ;

	*new { arg busArr, numChan = 4, out = 0, path ;  // sending stuff
		^super.new.initBusMixer(busArr, numChan, out, path)
		}

	initBusMixer {arg aBusArr, aNumChan, aOut, aPath ;
		busArr = aBusArr ; numChan = aNumChan	 ; path = aPath; out = aOut ;
		mulArr = [];
		this.createSynths ;
		vol = 1 ; scale  = 1 ;
	}

	createSynths {
		synthArr = [] ;
		{
		SynthDef(\synthBus, {arg out = 0, mul = 1, mute = 1, bus, vol = 1 ;
			Out.ar(out, In.ar(bus, numChan)*mul*mute*vol)
			}).add ;
		monoBus = Bus.audio(Server.local, 4) ; // to mono check
//		if (mixGui.notNil) {mixGui.window.close} ;
		Server.local.sync ;
		busArr.do{|bus|
			synthArr = synthArr.add(Synth.tail(Server.local, \synthBus,
				[\bus, bus, \out, out, \mute, 1, \mul, 1, \vol, 1])) ;
			mulArr = mulArr.add(1) ;
		Server.local.sync ;
		} ;
		Server.local.sync ;
		{if (mixGui.isNil) {this.gui} }.defer;
		}.fork
	}

	recreateSynths {
		{
		synthArr = [] ;
		busArr.do{|bus|
		synthArr = synthArr.add(Synth.tail(Server.local, \synthBus,
				[\bus, bus, \out, out, \mute, 1, \mul, 1, \vol, 1])) ;
			mulArr = mulArr.add(1) ;
			} ;
		Server.local.sync ;
		0.25.wait ;
		{this.mixGui.newStetho}.defer ;
		}.fork
	}


	// just to test, must be improved
	mono { arg out = 0 ;
		{
			this.out_(monoBus.index) ;
			Server.local.sync ;
			monoSynth = {Out.ar(out, Mix(In.ar(monoBus, 4)))}.play(addAction:\addToTail)
		}.fork
		}

	vol_ { arg value ; synthArr.do{|sy| sy.set(\vol, value*scale)} ; vol = value }
	scale_ { arg value ; synthArr.do{|sy| sy.set(\vol, vol*value)} ; scale = value }

	out_ { arg value ; synthArr.do{|sy| sy.set(\out, out)} ; out = value }

	write {
		mulArr = [vol] ;
		synthArr.do{|sy, i|
			sy.get(\mul, {|val| mulArr = mulArr.add(val) ;
				mulArr.writeArchive(path) })
		};
	}

	read {
		if (File.exists(path)) {
		mulArr = Object.readArchive(path) ;
		this.vol_(mulArr[0]) ;
		mulArr = mulArr[1..] ;
		mulArr.do{|mul, i| synthArr[i].set(\mul, mul) } ;
		if (mixGui.notNil) { {mixGui.refresh}.defer; "suca".postln } ;
		} { "Log does not exist".postln }
	}

	gui { mixGui = BusMixerGui(this) }

}

BusMixerGui {

	var <>busMixer, <>window, <>slArr, <>btArr, <>recButt, <>mainSl ;
	var <>stetho, <>bck;

	*new { arg busMixer ;
		^super.new.initBusMixerGui(busMixer)
		}


	initBusMixerGui { arg aBusMixer ;
		busMixer = aBusMixer ;
		window = Window("Bus Mixer", Rect(100, 600, 20* busMixer.busArr.size+20+50+50+500, 320)).front ;
		Button(window, Rect(10,30, 40, 30)).states_([["Record", Color.white, Color.red]])
			.action_{ busMixer.write}.font_(Font("Futura", 10))  ;
		StaticText(window, Rect(10, 80, 100, 30 )).string_("Bus Mixer").font_(Font("Futura", 10)) ;

		NumberBox(window, Rect(20, 145, 20, 20)).font_(Font("Futura", 10))
			.action_{|me| busMixer.setOut(me.value)} ;

		StaticText(window, Rect(22, 160, 100, 30 )).string_("out").font_(Font("Futura", 10)) ;

		Button(window, Rect(10,30+180, 40, 30)).states_([["Read", Color.red, Color.white]])
			.action_{ busMixer.read; this.refresh}.font_(Font("Futura", 10))  ;

		mainSl = Slider(window, Rect(20* busMixer.busArr.size+20+50, 10, 30, 250))
			.value_(1.ampdb.linlin(-96, 3, 0, 1))
			.action_{|me| busMixer.vol_(me.value.linlin(0,1, -96, 3).dbamp)} ;
		StaticText(window, Rect(20* busMixer.busArr.size+20+50, 10+250+10, 30, 15)).string_("Main").font_(Font("Futura", 10)) ;
		busMixer.busArr.do{|b, i|
			StaticText(window, Rect(i*20+10+50, 10+250+10+20, 15, 15))
				.font_(Font("Futura", 10)).string_(i.asString)  ;
			slArr = slArr.add( Slider(window, Rect(i*20+10+50, 10, 15, 250))
				.action_{|me| busMixer.synthArr[i].set(\mul, me.value.linlin(0,1, -96, 3).dbamp)}) ;
				busMixer.synthArr[i].get(\mul, {|val| {slArr[i].value_(val.ampdb.linlin(-96, 3, 0,1))}.defer });
				busMixer.synthArr[i].get(\mute, {|val| {btArr[i].value_(val)}.defer });
			btArr = btArr.add(Button(window, Rect(i*20+10+50, 10+250+10, 15, 15))
				.states_([["M", Color.black, Color.grey], ["",Color.white, Color.green]])
				.action_{|me| busMixer.synthArr[i].set(\mute, me.value)}
				)} ;
//		window.onClose_{
//			busMixer.busArr.do{|b| b.free};
//			busMixer.synthArr.do{|sy| sy.free} } ;

		bck = CompositeView.new(window, Rect(20* busMixer.busArr.size+20+50+50, 10, 475, 250)) ;
		stetho = 	QStethoscope2.new(Server.local,6, index: 0, view: bck) ;
		stetho.view.bounds_(Rect(0, 0, 450, 250))
		//	window.onClose_{ stetho.free } ;
		//CmdPeriod.doOnce{ window.close } ;


	}

	newStetho {
		bck.remove ;
		bck = CompositeView.new(window, Rect(20* busMixer.busArr.size+20+50+50, 10, 475, 250)) ;
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

/*

s.reboot ;
(
b = Bus.audio(s, 4); c = Bus.audio(s, 4) ;  d = Bus.audio(s, 4) ;

x = {Out.ar(b, SinOsc.ar([1000, 1500, 2000, 3000]))}.play ;
y = {Out.ar(c, [WhiteNoise.ar, WhiteNoise.ar,WhiteNoise.ar,WhiteNoise.ar])}.play ;
z = {Out.ar(d, [Pulse.ar, Pulse.ar,Pulse.ar,Pulse.ar])}.play ;

m = BusMixer([b,c,d], 2, "/log") ;

m.vol_(0.1)
m.vol

// cmperiod, then
m.recreateSynths

)
*/