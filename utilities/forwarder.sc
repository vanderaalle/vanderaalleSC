/*

It gets n chans in (busses) and outputs n chans, allowing vol control 
Plus a mixdown of all
*/


Forwarder {
	
	var <>inBusArr, <>outBusArr ;
	// inBus: 	the bus array fed in input
	// outBus:	the bus array spit in output
	var <>synthArr, <>mulArr ; // internal stuff to forward and manipulate
	var <>mixBus, <>mixSynth ; // the mixdown architecture
	var <>path ; // where to write/read log 
	var <vol, <scale ; // scale is for scaling vol without loosing its main val
	var <>mixGui ;
	
	*new { arg inBusArr, path ;  
		^super.new.initMixer(inBusArr, path)
		}

	initMixer {arg aBusArr, aPath ;
		inBusArr = aBusArr ; path = aPath;
		mulArr = [];
		this.createSynths ;
		vol = 1 ; scale  = 1 ;
	}

	createSynths {
		synthArr = [] ;
		{
		outBusArr = Array.fill(inBusArr.size, {Bus.audio(Server.local, 1)}) ;
		mixBus = Bus.audio(Server.local, 1) ;
		Server.local.sync ;	
		SynthDef(\synthBus, {arg outBus = 0, mul = 1, mute = 1, inBus, vol = 1 ;
			Out.ar(outBus, In.ar(inBus, 1)*mul*mute*vol)
			}).add ;
		Server.local.sync ;	
		inBusArr.do{|bus, i|
			synthArr = synthArr.add(Synth.tail(Server.local, \synthBus, 
				[\inBus, bus, \outBus, outBusArr[i].postln, \mute, 1, \mul, 1, \vol, 1])) ;
			mulArr = mulArr.add(1) ;
		Server.local.sync ;	
		mixSynth = {Out.ar(mixBus, Mix(In.ar(outBusArr)/inBusArr.size))}.play(addAction:\addToTail) ;	
		} ;
		Server.local.sync ;	
		{if (mixGui.isNil) {this.gui} }.defer;		
		}.fork
	}
/*	
	recreateSynths {
		{
		synthArr = [] ;
		inBusArr.do{|bus|
		synthArr = synthArr.add(Synth.tail(Server.local, \synthBus, 
				[\bus, inBus, \out, outBus, \mute, 1, \mul, 1, \vol, 1])) ;
			mulArr = mulArr.add(1) ;
			} ;
		Server.local.sync ;
		0.25.wait ;
		{this.mixGui.newStetho}.defer ;
		}.fork
	}
*/	
	
	vol_ { arg value ; synthArr.do{|sy| sy.set(\vol, value*scale)} ; vol = value }
	scale_ { arg value ; synthArr.do{|sy| sy.set(\vol, vol*value)} ; scale = value }
	

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
		if (mixGui.notNil) { {mixGui.refresh}.defer } ;
		} { "Log does not exist".postln } 
	}

	gui { mixGui = ForwarderGui(this) }	
	
}

ForwarderGui {
	
	var <>forwarder, <>window, <>slArr, <>btArr, <>recButt, <>mainSl ;
	var <>stetho, <>bck;
	
	*new { arg forwarder ;
		^super.new.initForwarderGui(forwarder) 	
		}


	initForwarderGui { arg aForwarder ;
		forwarder = aForwarder ;
		window = Window("Forwarder", Rect(100, 600, 20* forwarder.inBusArr.size+20+50+50+500, 320)).front ;
		Button(window, Rect(10,30, 40, 30)).states_([["Record", Color.white, Color.red]])
			.action_{ forwarder.write}.font_(Font("Futura", 10))  ;
		StaticText(window, Rect(10, 80, 100, 30 )).string_("Forwarder").font_(Font("Futura", 10)) ;
		
//		NumberBox(window, Rect(20, 145, 20, 20)).font_(Font("Futura", 10))
//			.action_{|me| forwarder.setOut(me.value)} ;
		
		StaticText(window, Rect(22, 160, 100, 30 )).string_("out").font_(Font("Futura", 10)) ;

		Button(window, Rect(10,30+180, 40, 30)).states_([["Read", Color.red, Color.white]])
			.action_{ forwarder.read; this.refresh}.font_(Font("Futura", 10))  ;
		
		mainSl = Slider(window, Rect(20* forwarder.inBusArr.size+20+50, 10, 30, 250))
			.value_(1.ampdb.linlin(-96, 3, 0, 1)) 
			.action_{|me| forwarder.vol_(me.value.linlin(0,1, -96, 3).dbamp)} ;
		StaticText(window, Rect(20* forwarder.inBusArr.size+20+50, 10+250+10, 30, 15)).string_("Main").font_(Font("Futura", 10)) ;
		forwarder.inBusArr.do{|b, i|
			StaticText(window, Rect(i*20+10+50, 10+250+10+20, 15, 15))
				.font_(Font("Futura", 10)).string_(i.asString)  ;
			slArr = slArr.add( Slider(window, Rect(i*20+10+50, 10, 15, 250))
				.action_{|me| forwarder.synthArr[i].set(\mul, me.value.linlin(0,1, -96, 3).dbamp)}) ;
				forwarder.synthArr[i].get(\mul, {|val| {slArr[i].value_(val.ampdb.linlin(-96, 3, 0,1))}.defer });
				forwarder.synthArr[i].get(\mute, {|val| {btArr[i].value_(val)}.defer });
			btArr = btArr.add(Button(window, Rect(i*20+10+50, 10+250+10, 15, 15))
				.states_([["M", Color.black, Color.grey], ["",Color.white, Color.green]])
				.action_{|me| forwarder.synthArr[i].set(\mute, me.value)}
				)} ;
//		window.onClose_{ 
//			forwarder.inBusArr.do{|b| b.free}; 
//			forwarder.outBusArr.do{|b| b.free}; 
//			forwarder.synthArr.do{|sy| sy.free} } ;
		
		bck = CompositeView.new(window, Rect(20* forwarder.inBusArr.size+20+50+50, 10, 475, 250)) ;
		stetho = 	QStethoscope2.new(Server.local,6, index: forwarder.outBusArr[0].index, view: bck) ;
		stetho.view.bounds_(Rect(0, 0, 450, 250))
		//	window.onClose_{ stetho.free } ;
		//CmdPeriod.doOnce{ window.close } ;
		
		
	} 
	
	newStetho {
		bck.remove ;
		bck = CompositeView.new(window, Rect(20* forwarder.inBusArr.size+20+50+50, 10, 475, 250)) ;
		stetho = 	QStethoscope2.new(Server.local,6, index: 0, view: bck) ;
		stetho.view.bounds_(Rect(0, 0, 450, 250))
	}
	
	refresh {
		mainSl.value_(forwarder.vol.ampdb.linlin(-96, 3, 0,1) );
		forwarder.synthArr.do{|sy, i|
			sy.get(\mul, {|val| {slArr[i].value_(val.ampdb.linlin(-96, 3, 0,1))}.defer })
		};
	}

}

/*

s.reboot ;
(
b = Bus.audio(s, 1); c = Bus.audio(s, 1) ;

x = {Out.ar(b, SinOsc.ar(MouseX.kr(20,2000)))}.play ;
x = {Out.ar(b, Pulse.ar(MouseX.kr(20,2000)))}.play ;

y = {Out.ar(c, WhiteNoise.ar*MouseY.kr)}.play ;
y.free
m = Forwarder([b,c], "/log") ;


~playBuf = Buffer.read(Server.local, "/Sonata1GMinorAdagio.aiff").normalize ; 
~player = {arg out, buf; Out.ar([out, 0], PlayBuf.ar(1, ~playBuf, loop:1))}.play(Server.local, args:[\out, b, \buf, ~playBuf]) ;

~player.free

x.free



k = {Out.ar(0, In.ar(m.mixBus))}.play(addAction:\addToTail)

k.free
m.synthArr[0].set(\mul, -20.dbamp)
m.mixBus
m.outBusArr
m.vol_(0.1)
m.vol
m.mixBus

s.scope(1, m.mixBus.index)
// cmperiod, then
m.recreateSynths

)
*/

/*

USING ANALYZERBANK

a = AnalyzerBankN(m.mixBus) ;
a.runAll ;
g = AnalyzerBankNGui(a)

g.rate_(1/20)

*/
