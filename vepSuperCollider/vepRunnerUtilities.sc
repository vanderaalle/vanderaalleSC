// Andrea Valle, started on Nov 2009

// VepRunner utilities

VepBusMeterView {

// a 8 bus viewer
// bus are analyzed and  sampled, amplitude is used to color a box
// from green (0) to hsv(0.1, 1,0.8) (1, and clip)
// can probably be generalized to n, with some boring effort 

// this is the view to be included in other GUIs	
	var <>numBus ;	
	var <>refresh ;
	var <>parent, <>bounds, <>w, <>enSlider, <>enNumBox ;
	var <>speakerArr ;
	var <>synthArr ;
	var <>globalLoad ;
	var <>responder ;
	var <>enhance ; // a multiplier for emphasizing low amps 
	var <>onCmdPeriod ; // if true, close window after cmd+period
	var <>oldVal ; // store db val ;
	var <>clip, <>clipLabel, <>noClip ; // clipping warning 
	var <>color ;
	
	*new { arg parent, bounds, numBus = 8, refresh = 5, color ; 	 
		^super.new.initVepBusViewer(parent, bounds, numBus, refresh, color) 
	}

	initVepBusViewer { arg aParent, aBounds, aNumBus, aRefresh, aColor ; 
		parent = aParent ;
		bounds = aBounds ;
		color = aColor ;
		if (color.isNil) {color = Color(0.1, 0.1, 0.1)} ;
		w = CompositeView.new(parent, bounds) ;
		numBus = aNumBus ;
		refresh = aRefresh ;
		enhance = 1 ;
		oldVal = 0 ;
		noClip = true ;
		{
		SynthDef(\amp, { arg bus, id, rate = 5  ;
			var amp = Amplitude.kr(In.ar(bus));
			SendTrig.kr( Impulse.kr(rate),  id, amp) ;
		}).send(Server.local) ;
		Server.local.sync ;
		synthArr = 
			Array.fill(numBus, {|i| Synth(\amp, [\bus, i, \id, i, \rate, refresh ], addAction:\addToTail) }) ;
		}.fork ;
		responder = OSCresponder(Server.local.addr,'/tr',
			{ arg time, responder, msg ;
			var id = msg[2] ;
			var val = msg[3] ; 
			this.paint(id, val) ;
		}).add ;
		this.createGui
	}
	
	createGui {
		var dimW = 440 ;
		var dim = 400;
		var spDim = 40 ;
		var step = spDim*0.5 ;
		var shift = 20 ;
		//w = Window.new("VepRunner: channel loading", Rect(500, 500, dimW, dimW)) ;
		w.background_(color) ;

			StaticText.new(w, Rect(dim*0.5-step+shift-3, dim*0-step+shift-3, spDim+6, spDim+6)).background_(Color.new(0.65, 0.65, 0.65)) ;
			StaticText.new(w, Rect(dim*0.25-step+shift-3, dim*0.25-step+shift-3, spDim+6, spDim+6)).background_(Color.new(0.65, 0.65, 0.65)) ; 
			StaticText.new(w, Rect(dim*0-step+shift-3, dim*0.5-step+shift-3, spDim+6, spDim+6)).background_(Color.new(0.65, 0.65, 0.65)) ;
			StaticText.new(w, Rect(dim*0.25-step+shift-3, dim*0.75-step+shift-3, spDim+6, spDim+6)).background_(Color.new(0.65, 0.65, 0.65)) ; 
			StaticText.new(w, Rect(dim*0.5-step+shift-3, dim-step+shift-3, spDim+6, spDim+6)).background_(Color.new(0.65, 0.65, 0.65)) ;
			StaticText.new(w, Rect(dim*0.75-step+shift-3, dim*0.75-step+shift-3, spDim+6, spDim+6)).background_(Color.new(0.65, 0.65, 0.65)) ; 
			StaticText.new(w, Rect(dim-step+shift-3, dim*0.5-step+shift-3, spDim+6, spDim+6)).background_(Color.new(0.65, 0.65, 0.65)) ;
			StaticText.new(w, Rect(dim*0.75-step+shift-3, dim*0.25-step+shift-3, spDim+6, spDim+6)).background_(Color.new(0.65, 0.65, 0.65)) ;


		speakerArr = [
			StaticText.new(w, Rect(dim*0.5-step+shift, dim*0-step+shift, spDim, spDim)).string_("0").background_(Color.grey) ,
			StaticText.new(w, Rect(dim*0.25-step+shift, dim*0.25-step+shift, spDim, spDim)).string_("0").background_(Color.grey) , 
			StaticText.new(w, Rect(dim*0-step+shift, dim*0.5-step+shift, spDim, spDim)).string_("0").background_(Color.grey) ,
			StaticText.new(w, Rect(dim*0.25-step+shift, dim*0.75-step+shift, spDim, spDim)).string_("0").background_(Color.grey) , 
			StaticText.new(w, Rect(dim*0.5-step+shift, dim-step+shift, spDim, spDim)).string_("0").background_(Color.grey) ,
			StaticText.new(w, Rect(dim*0.75-step+shift, dim*0.75-step+shift, spDim, spDim)).string_("0").background_(Color.grey) , 
			StaticText.new(w, Rect(dim-step+shift, dim*0.5-step+shift, spDim, spDim)).string_("0").background_(Color.grey) ,
			StaticText.new(w, Rect(dim*0.75-step+shift, dim*0.25-step+shift, spDim, spDim)).string_("0").background_(Color.grey) 
			] ;
		speakerArr.do{|t| t.align_( \center ) } ;

		clip = StaticText.new(w, Rect(dim*0.5-step+shift+(spDim*0.25), dim*0.5-step+shift+(spDim*0.25), spDim*0.5, spDim*0.5)).background_(color) ;
		
		// labels 
			StaticText.new(w, Rect(dim*0.5-step+shift, dim*0-step+shift+spDim, spDim, spDim)).string_("out: 0").stringColor_(Color.new(0.65, 0.65, 0.65)).align_( \center )  ; 
			
			StaticText.new(w, Rect(dim*0.25-step+shift+spDim, dim*0.25-step+shift, spDim, spDim)).string_("out: 1").stringColor_(Color.new(0.65, 0.65, 0.65)).align_( \center ) ; 
			StaticText.new(w, Rect(dim*0-step+shift+spDim, dim*0.5-step+shift, spDim, spDim)).string_("out: 2").stringColor_(Color.new(0.65, 0.65, 0.65)).align_( \center ) ;
			StaticText.new(w, Rect(dim*0.25-step+shift+spDim, dim*0.75-step+shift, spDim, spDim)).string_("out: 3").stringColor_(Color.new(0.65, 0.65, 0.65)).align_( \center ) ; 
			
			StaticText.new(w, Rect(dim*0.5-step+shift, dim-step+shift-spDim, spDim, spDim)).string_("out: 4").stringColor_(Color.new(0.65, 0.65, 0.65)).align_( \center ) ;
			
			StaticText.new(w, Rect(dim*0.75-step+shift-spDim, dim*0.75-step+shift, spDim, spDim)).string_("out: 5").stringColor_(Color.new(0.65, 0.65, 0.65)).align_( \center ) ; 
			StaticText.new(w, Rect(dim-step+shift-spDim, dim*0.5-step+shift, spDim, spDim)).string_("out: 6").stringColor_(Color.new(0.65, 0.65, 0.65)).align_( \center ) ;
			StaticText.new(w, Rect(dim*0.75-step+shift-spDim, dim*0.25-step+shift, spDim, spDim)).string_("out: 7").stringColor_(Color.new(0.65, 0.65, 0.65)).align_( \center ) ;
		// clip
			clipLabel = StaticText.new(w, Rect(dim*0.5-step+shift+(spDim*0.25), dim*0.5-step+shift+(spDim*0.25)+25, spDim, spDim*0.5))
			.background_(color).string_("Clip!").align_(\left).stringColor_(color) ;
			
	}

	paint { arg id, val ;
			var hue ; var speaker = speakerArr[id] ;
			var newVal ;
			newVal = val.ampdb.round(3) ;
			if (newVal != oldVal) {
				speaker.string_(val.ampdb.round(0.1).asString) } ;
			hue =  (val.ampdb).clip(-96, 0).linlin(-96, 0, 0.25, 0) ;
			if (newVal >= 1) {
				if (noClip) {
					{
					noClip = false ;
					clip.background_(Color.hsv(0,1,1)) ;
					clipLabel.stringColor_(Color.hsv(0,1,1)) ;
					0.5.wait ;
					clip.background_(color) ;
					clipLabel.stringColor_(color) ;
					noClip = true ;
					}.fork
				}
				} ;
			speaker.background_(Color.hsv(hue, 1, 1)) ;
			oldVal = newVal ;
	}
	

}
 
// Autonomous window
VepBusMeter {
	
	var <>w, <>onCmdPeriod, <>meterView ;
	
	*new { arg numBus = 8, refresh = 5, onCmdPeriod = true ; 
		^super.new.initVepBusMeter(numBus, refresh, onCmdPeriod) 
	}

	initVepBusMeter { arg aNumBus, aRefresh, anOnCmdPeriod ; 
		w = Window.new("VepRunner: channel loading", Rect(500, 500, 440, 440)) ;
		meterView = VepBusMeterView(w, w.view.bounds, aNumBus, aRefresh) ;
		onCmdPeriod = anOnCmdPeriod ;
		if (onCmdPeriod) { CmdPeriod.doOnce{w.close}} ;
		w.onClose_({meterView.responder.remove ; meterView.synthArr.do{|sy| sy.free} }) ;
		w.front ;
	}

} 
 
// message posting: messages are simply posted to a view
// visualizing what's going on
// by using the messenger, we avoid changing vepRunner code

VepRunnerMessengerView {
	
	var <>vepRunner ;
	var <>parent, <>bounds, <>w, <>arr, <>msgList ; 
	var <>old ;
	
	*new { arg parent, bounds, vepRunner, queueSize = 5 ; 
		^super.new.initVepRunnerMessenger(parent, bounds, vepRunner, queueSize) 
	}

	initVepRunnerMessenger { arg aParent, aBounds, aVepRunner, aQueueSize ;  
		vepRunner = aVepRunner ;
		vepRunner.addDependant(this) ;
		parent = aParent ;
		bounds = aBounds ;
		w = CompositeView.new(parent, bounds) ;
		arr = Array.fill(aQueueSize, {|i|
			StaticText.new(w, Rect(0, 40*i, 200, 40))
		}) ;
		msgList = [] ;
	}
	
	
	update { arg theChanged, theChanger, more ;
		var txt = "", l ;
		l = more[0].asString++"\n" ;
		if (l.asSymbol != old.asSymbol) {
			old = l ;
			msgList = msgList.add(l) ;
			{
			msgList.reverse[..4].do{|msg, i| 
				arr[i].string_(msg)
				.font_(Font("Futura", 36-(i*2+12))) 
				.stringColor_(Color.hsv((i)/15, 1,1))
				.align_(\center) ;
			} ;
		}.defer
		}
	}

}
	
// Autonomous window
VepRunnerMessenger {
	
	var <>w, <>messengerView ;

	*new { arg vepRunner, queueSize = 5 ; 
		^super.new.initVepRunnerMessenger(vepRunner, queueSize) 
	}

	initVepRunnerMessenger { arg aVepRunner, aQueueSize ; 
		w = Window.new("Messenger",  Rect(0, 800, 200, 40*aQueueSize)) ; 
		messengerView = VepRunnerMessengerView(w, w.bounds, aVepRunner, aQueueSize) ;
		CmdPeriod.doOnce{w.close; messengerView.vepRunner.removeDependant(messengerView)} ;
		w.front
	}

} 


// Custom GUI for VEP, including previous stuff
VepGui {
	
	var <>w, <>busMeter, <>messenger, <>stetho ;
	var <>sl1, <>sl2, <>sl3, <>sl4, <>nb1, <>nb2, <>nb3, <>nb4 ;
	var <>runner, <>dsp ;
	
	*new { arg vepRunner, vepDSP ; 
		^super.new.initVepGui(vepRunner, vepDSP) 
	}

	initVepGui { arg aVepRunner, aVepDSP ; 
		var bck ;
		var font = Font.new("Futura", 13.5) ;
		runner = aVepRunner ;
		dsp = aVepDSP ;
		dsp.addDependant(this) ;
		runner.addDependant(this) ;
		w = Window.new("VEP: Tora Tora Tora!",  Rect(200, 200, 900, 460)) ;
		w.view.background_(Color.gray(0.125)) ; 
		busMeter = VepBusMeterView(w, Rect(10, 10, 440, 440), color: Color.black) ;
		bck = CompositeView.new(w, Rect(530, 200, 350, 270)) ;
		stetho = Stethoscope.new(Server.local,10, view: bck) ;
		10.do{|i| StaticText.new(w, Rect(500, 200+(24*i), 20, 20)).string_(i.asString).stringColor_(Color.hsv(0.1, 1,0.8)).font_(font) } ;
		w.onClose_({
			busMeter.responder.remove ; busMeter.synthArr.do{|sy| sy.free} ;
			stetho.free }) ;
		this.createControls ;
		CmdPeriod.doOnce{
			w.close ; 
		} ;

		w.front ;
	}
	
	createControls {	
		var font = Font.new("Futura", 13.5) ;
		var offset = 480 ;
		StaticText.new(w, Rect(10+offset, 10, 70, 25)).string_(" vol ").font_(font).stringColor_(Color.hsv(0.1, 1,0.8)) ;
		StaticText.new(w, Rect(10+offset, 60, 70, 25)).string_(" spread ").font_(font).stringColor_(Color.hsv(0.1, 1,0.8)) ;
		StaticText.new(w, Rect(10+offset, 110, 70, 25)).string_(" subVol ").font_(font).stringColor_(Color.hsv(0.1, 1,0.8)) ;
		StaticText.new(w, Rect(10+offset, 160, 70, 25)).string_(" noiseVol ").font_(font).stringColor_(Color.hsv(0.1, 1,0.8)) ;		
		sl1 = Slider.new(w, Rect(75+offset, 10, 200, 25)).value_(1) ;
		sl2 = Slider.new(w, Rect(75+offset, 60, 200, 25)).value_(0) ;
		sl3 = Slider.new(w, Rect(75+offset, 110, 200, 25)).value_(0.25) ;
		sl4 = Slider.new(w, Rect(75+offset, 160, 200, 25)).value_(0.25) ;
		nb1 = NumberBox.new(w, Rect(285+offset, 10, 50, 25)).value_(0) ;
		nb2 = NumberBox.new(w, Rect(285+offset, 60, 50, 25)).value_(0) ;
		nb3 = NumberBox.new(w, Rect(285+offset, 110, 50, 25)).value_(-12) ;
		nb4 = NumberBox.new(w, Rect(285+offset, 160, 50, 25)).value_(-20) ;

		StaticText.new(w, Rect(285+offset+60, 10, 150, 25)).string_("dB:-96,0").font_(font).stringColor_(Color.hsv(0.1, 1,0.8))  ;
		StaticText.new(w, Rect(285+offset+60, 60, 150, 25)).string_("val:0-1").font_(font).stringColor_(Color.hsv(0.1, 1,0.8))  ;
		StaticText.new(w, Rect(285+offset+60, 110, 150, 25)).string_("dB:-96,0").font_(font).stringColor_(Color.hsv(0.1, 1,0.8))  ; 
		StaticText.new(w, Rect(285+offset+60, 160, 150, 25)).string_("dB:-96,0").font_(font).stringColor_(Color.hsv(0.1, 1,0.8))  ;

		
		sl1.action_({|sl| dsp.vol_(sl.value.linlin(0,1,-96,0).dbamp) ; 
			nb1.value_(sl.value.linlin(0,1,-96,0))}) ;
		sl2.action_({|sl| runner.spread_(sl.value*100) ; nb2.value_(sl.value*100)}) ;
		sl3.action_({|sl| dsp.subVol_(sl.value.linlin(0,1,-96,0).postln.dbamp.postln) ;
			nb3.value_(sl.value.linlin(0,1,-96,0))}) ;
		sl4.action_({|sl| dsp.noiseVol_(sl.value.linlin(0,1,-96,0).dbamp) ;
			nb4.value_(sl.value.linlin(0,1,-96,0))}) ;
		nb1.action_({|nb| dsp.vol_(nb.value.dbamp) ; 
			sl1.value_(nb.value.linlin(-96,0, 0,1))}) ;
		nb2.action_({|nb| runner.spread_(nb.value);
			sl2.value_(nb.value*0.01) }) ; 
		nb3.action_({|nb| dsp.subVol_(nb.value.dbamp);
			sl3.value_(nb.value.linlin(-96,0, 0,1)) }) ;
		nb4.action_({|nb| dsp.noiseVol_(nb.value.dbamp);
			sl4.value_(nb.value.linlin(-96,0, 0,1)) }) ;
	
//	nb1.valueAction_(0) ; nb2.valueAction_(0); nb3.valueAction_(-12) ; nb4.valueAction_(-20);
	}

	update { arg theChanged, theChanger, more ;
//		[theChanged, theChanger, more].postln ;
		case { more[0] == \vol}
				{	sl1.value_(dsp.vol.ampdb.linlin(-96,0, 0,1)) ;
				 	nb1.value_(dsp.vol.ampdb)
				 }
			{ more[0] == \spread}
				{	sl2.value_(runner.spread*0.01);
				 	nb2.value_(runner.spread)
				 }
			{ more[0] == \subVol}
				{	sl3.value_(dsp.subVol.ampdb.linlin(-96,0, 0,1));
				 	nb3.value_(dsp.subVol.ampdb)
				 }
			{ more[0] == \noiseVol}
				{	sl4.value_(dsp.noiseVol.ampdb.linlin(-96,0, 0,1));
				 	nb4.value_(dsp.noiseVol.ampdb)
				 }


	}

}
	