/*
This file must be included in SCClassLibrary folder of SuperCollider installation.
(Or in usual extension folders).

Extension needed: SwingOSC (GUI package for signal scoping):
- Place SwingOSC class folder  into SCClassLibrary folder of SuperCollider installation.
(Or in usual extension folders).
- Place SwingOSC help folder  into Help folder of SuperCollider installation.
(Or in usual extension folders).


See below for usage example

*/

Guardian {
	
	var <>numChans, <>synthArr, <>soundinArr, <>outArr,
		<>resp, // the resp
	  <>inVolArr, 	// mic vol 
	  <>outVolArr, 	// pulse train vol
	  <>threshArr, 	// onset threshold 	
	  <>freqArr ; 	// freq to solenoids	
	// constructor
	*new { arg numChans ; 
		^super.new.initMe(numChans) 	
	}

	initMe { arg aNum;
		numChans = aNum ;
		soundinArr = [] ;
		synthArr = [] ;
		outArr = [] ;
		threshArr = Array.fill(numChans, {1}) ; // init at max 
		outVolArr = Array.fill(numChans, {1}) ; // init at normalize
		inVolArr = Array.fill(numChans, {1}) ; // init at normalize
		freqArr = Array.fill(numChans, {1}) ; // init at min (single beat)
		Server.local.waitForBoot{		
		{
		numChans.do{ outArr = outArr.add(Bus.audio(Server.local, 1)) } ;
		Server.local.sync ;
		SynthDef(\onsets, { arg inBus = 0, thresh = 0.25, id = 0 ;
			var loc = LocalBuf(512, 1) ;
			var onsets, chain, input = In.ar(inBus) ;
			chain = FFT(loc, input) ;	
			onsets = Onsets.kr(chain, thresh) ; 
			SendTrig.kr(onsets, id, Amplitude.kr(input)) ;
		}).add ;
		Server.local.sync ;
		numChans.do{|i| soundinArr = soundinArr.add(
			 {|vol = 1 | Out.ar(outArr[i], SoundIn.ar(i)*vol)}.play )} ;
		Server.local.sync ;
		this.createSynth ;
				Server.local.sync ;
		resp = OSCresponderNode(Server.local.addr,'/tr',
		{ arg time,responder,msg;
			var id = msg[2], amp = msg[3].ampdb ;
			var freq = freqArr[id] ; 
			var dur = 0.25 ; 
			amp = amp.linlin(-30, 0, 0.1, 1) ;
			amp =1 ;
			this.changed(this, [id]) ;
			{Out.ar(id, Pulse.ar(freq).unipolar*Line.kr(amp, amp, dur, doneAction:2)
				*outVolArr[id])}.play ;
		}).add;}.fork
		}
	}	
	
	createSynth {
		numChans.do{arg i ; synthArr = synthArr.add( Synth(\onsets, 
			[\id, i, \thresh, threshArr[i], \inBus, outArr[i]],
			addAction:\addToTail
			) ) }
	}
		
	removeResp { resp.remove; "Responder removed".postln }
	
	setThresh { arg id, thresh ;
		 threshArr[id] = thresh ;
		 synthArr[id].set(\thresh, thresh)
		}

	setFreq { arg id, freq ;
		 freqArr[id] = freq ;
		}

	setInVol { arg id, vol ;
		inVolArr[id] = vol ;
		 soundinArr[id].set(\vol, vol)	
	} 
	
	setOutVol { arg id, vol ;
		outVolArr[id] = vol 	
	} 
	
	gui {
		GuardianGUI(this)
		}
	
	gui2 {
		GuardianGUI2(this)
		}

}

// MONODIRECTIONAL: GUI --> MODEL 
GuardianGUI {
	
	var <>guardian, <>numChans,	 
		<>window, <>w, <>h, 
		<>inVolK, <>outVolK, <>threshK, <>freqK,  	//Knobs
		<>inVolV, <>outVolV, <>threshV, <>freqV ; // boxes

		
	// constructor
	*new { arg guardian ; 
		^super.new.initMe(guardian) 	
	}

	initMe { arg aGuardian;
		var v ;
		guardian = aGuardian ;
		numChans = guardian.numChans ;
		inVolK = [] ; outVolK = [] ; threshK = []; freqK = [] ;
		inVolV = [] ; outVolV = [] ; threshV = []; freqV = [] ;
		w = 150*numChans ;
		h = 500 ;
		window = Window("Guardian", Rect(30, 30, w, h)).front ;
		// freeing all the stuff
		window.onClose_({ guardian.removeResp; Server.local.freeAll ; guardian.outArr.do{|b| b.free} }) ;
		window.view.background_(Color.hsv(0.1, 0.8,1)) ;
		{
		Server.local.waitForBoot{
			{
		numChans.do{ arg i ;
			Stethoscope.new(Server.local,1, guardian.outArr[i].index, view: 
			CompositeView.new(window, Rect(i*150+10, 10, 140, 120))) ;
			inVolK = inVolK.add(Knob(window, Rect(i*150+10, 10+100, 50, 50))
				.color_([Color.red, Color(0.26, 0.759, 0.188), Color.grey, Color.black])
				.value_(1)) ;
			threshK = threshK.add(Knob(window, Rect(i*150+10, 75+10+100, 50, 50))
				.color_([Color.red, Color(0.26, 0.759, 0.188), Color.grey, Color.black])
				.value_(1))  ;
			freqK = freqK.add(Knob(window, Rect(i*150+10, 150+10+100, 50, 50))
				.color_([Color.red, Color(0.26, 0.759, 0.188), Color.grey, Color.black])
				.value_(1))  ;
			outVolK = outVolK.add(Knob(window, Rect(i*150+10, 225+10+100, 50, 50)) 
				.color_([Color.red, Color(0.26, 0.759, 0.188), Color.grey, Color.black])
					.value_(1)) ;
			inVolV = inVolV.add(NumberBox(window, Rect(i*150+10+60, 10+20+5+100, 70, 20))
				.value_(1)) ;
			threshV = threshV.add(NumberBox(window, Rect(i*150+10+60, 75+10+20+5+100, 70, 20))
				.value_(1)) ;
			freqV = freqV.add(NumberBox(window, Rect(i*150+10+60, 150+10+20+5+100, 70, 20))
				.value_(1)) ;
			outVolV = outVolV.add(NumberBox(window, Rect(i*150+10+60, 225+10+20+5+100, 70, 20))
				.value_(1)) ;
			//labels
			StaticText(window, Rect(i*150+10+60, 10+100, 100, 20)).font_(Font("Gill Sans",11)).string_("input (0-1): "+i) ;
			StaticText(window, Rect(i*150+10+60, 75+10+100, 100, 20)).font_(Font("Gill Sans",11)).string_("thresh (0-1): "+i) ;
			StaticText(window, Rect(i*150+10+60, 150+10+100, 100, 20)).font_(Font("Gill Sans",11)).string_("freq (0-10): "+i) ;
			StaticText(window, Rect(i*150+10+60, 225+10+100, 100, 20)).font_(Font("Gill Sans",11)).string_("output (0-1): "+i) ;
			Stethoscope.new(Server.local,1,i, view: 
			CompositeView.new(window, Rect(i*150+10, 275+10+20+5+100, 140, 120))) ;
		} ;
		Server.local.sync ;
		numChans.do{ arg i ;	
			inVolK[i].action_{|it| v = it.value.trunc(0.000001); guardian.setInVol(i, v); inVolV[i].value_(v) } ;
			outVolK[i].action_{|it|  v = it.value.trunc(0.000001); guardian.setOutVol(i, v); outVolV[i].value_(v) } ;
			threshK[i].action_{|it|  v = it.value.trunc(0.000001); guardian.setThresh(i, v); threshV[i].value_(v) } ;
			freqK[i].action_{|it|  v = it.value.trunc(0.000001)*10; guardian.setFreq(i, v); freqV[i].value_(v) } ;
			inVolV[i].action_{|it|  v = it.value.trunc(0.000001); guardian.setInVol(i, v); inVolK[i].value_(v) } ;
			outVolV[i].action_{|it|   v = it.value.trunc(0.000001); v = it.value.trunc(0.000001); guardian.setOutVol(i, v); outVolK[i].value_(v) } ;
			threshV[i].action_{|it|  v = it.value.trunc(0.000001); guardian.setThresh(i, v); threshK[i].value_(v) } ;
			freqV[i].action_{|it|  v = it.value.trunc(0.000001); guardian.setFreq(i, v); freqK[i].value_(v.linlin(0,10, 0,1)) } ;
		}
		}.fork(clock:AppClock)}}.defer
		
	}
}	


// BIDIRECTIONAL: no stetho
GuardianGUI2 {
	
	var <>guardian, <>numChans,	 
		<>window, <>w, <>h, 
		<>inVolK, <>outVolK, <>threshK, <>freqK,  	//Knobs
		<>inVolV, <>outVolV, <>threshV, <>freqV, // boxes
		<>butt ;
		
	// constructor
	*new { arg guardian ; 
		^super.new.initMe(guardian) 	
	}

	initMe { arg aGuardian;
		var v ;
		guardian = aGuardian ;
		guardian.addDependant(this) ;
		numChans = guardian.numChans ;
		butt = [] ;
		inVolK = [] ; outVolK = [] ; threshK = []; freqK = [] ;
		inVolV = [] ; outVolV = [] ; threshV = []; freqV = [] ;
		w = 150*numChans ;
		h = 340 ;
		window = Window("Guardian", Rect(30, 30, w, h)).front ;
		// freeing all the stuff
		window.onClose_({ guardian.removeResp; Server.local.freeAll ; guardian.outArr.do{|b| b.free} }) ;
		window.view.background_(Color.hsv(0.1, 0.8,0.25)) ;
		{
		Server.local.waitForBoot{
			{
		numChans.do{ arg i ;
			StaticText.new(window, Rect(i*150+10+15-2, 300-2, 24, 24)).background_(Color(0.26, 0.759, 0.188));
			butt = butt.add(StaticText.new(window, Rect(i*150+10+15, 300, 20, 20))
				.font_(Font("Gill Sans",11))
				.background_(Color(0.26, 0.759, 0.188))
				);
			inVolK = inVolK.add(Knob(window, Rect(i*150+10, 10, 50, 50))
				.color_([Color.red, Color(0.26, 0.759, 0.188), Color.grey, Color.black])
				.value_(1)) ;
			threshK = threshK.add(Knob(window, Rect(i*150+10, 75+10, 50, 50))
				.color_([Color.red, Color(0.26, 0.759, 0.188), Color.grey, Color.black])
				.value_(1))  ;
			freqK = freqK.add(Knob(window, Rect(i*150+10, 150+10, 50, 50))
				.color_([Color.red, Color(0.26, 0.759, 0.188), Color.grey, Color.black])
				.value_(1))  ;
			outVolK = outVolK.add(Knob(window, Rect(i*150+10, 225+10, 50, 50)) 
				.color_([Color.red, Color(0.26, 0.759, 0.188), Color.grey, Color.black])
					.value_(1)) ;
			inVolV = inVolV.add(NumberBox(window, Rect(i*150+10+60, 10+20+5, 70, 20))
				.value_(1)) ;
			threshV = threshV.add(NumberBox(window, Rect(i*150+10+60, 75+10+20+5, 70, 20))
				.value_(1)) ;
			freqV = freqV.add(NumberBox(window, Rect(i*150+10+60, 150+10+20+5, 70, 20))
				.value_(1)) ;
			outVolV = outVolV.add(NumberBox(window, Rect(i*150+10+60, 225+10+20+5, 70, 20))
				.value_(1)) ;
			//labels
			StaticText(window, Rect(i*150+10+60, 10, 100, 20)).font_(Font("Gill Sans",11)).string_("input (0-1): "+i).stringColor_(Color.white) ;
			StaticText(window, Rect(i*150+10+60, 75+10, 100, 20)).font_(Font("Gill Sans",11)).string_("thresh (0-1): "+i).stringColor_(Color.white) ;
			StaticText(window, Rect(i*150+10+60, 150+10, 100, 20)).font_(Font("Gill Sans",11)).string_("freq (0-10): "+i).stringColor_(Color.white) ;
			StaticText(window, Rect(i*150+10+60, 225+10, 100, 20)).font_(Font("Gill Sans",11)).string_("output (0-1): "+i).stringColor_(Color.white) ;
		} ;
		Server.local.sync ;
		numChans.do{ arg i ;	
			inVolK[i].action_{|it| v = it.value.trunc(0.000001); guardian.setInVol(i, v); inVolV[i].value_(v) } ;
			outVolK[i].action_{|it|  v = it.value.trunc(0.000001); guardian.setOutVol(i, v); outVolV[i].value_(v) } ;
			threshK[i].action_{|it|  v = it.value.trunc(0.000001); guardian.setThresh(i, v); threshV[i].value_(v) } ;
			freqK[i].action_{|it|  v = it.value.trunc(0.000001)*10; guardian.setFreq(i, v); freqV[i].value_(v) } ;
			inVolV[i].action_{|it|  v = it.value.trunc(0.000001); guardian.setInVol(i, v); inVolK[i].value_(v) } ;
			outVolV[i].action_{|it|   v = it.value.trunc(0.000001); v = it.value.trunc(0.000001); guardian.setOutVol(i, v); outVolK[i].value_(v) } ;
			threshV[i].action_{|it|  v = it.value.trunc(0.000001); guardian.setThresh(i, v); threshK[i].value_(v) } ;
			freqV[i].action_{|it|  v = it.value.trunc(0.000001); guardian.setFreq(i, v); freqK[i].value_(v.linlin(0,10, 0,1)) } ;
		}
		}.fork(clock:AppClock)}}.defer
		
	}
	
	update { arg theChanged, theChanger, more ;
		{
		butt[more[0]].background_(Color.red);
		0.2.wait ;
		butt[more[0]].background_(Color.hsv(0.1, 0.8,0.25))
		}.fork(AppClock)
		}
}

/*
// in order to evaluate code, select lines and use Lang->Evaluate Selection

// this should be all you need
Guardian(4).gui ; // 4 here indicates the number of I/O, just specify the value you need 
Guardian(4).gui2 ;


//-----------------------------------------------------------------------------------------

// GUI

/*
From top to bottom
- a scoping window showing the input signal for the channel
- knob/number box for input signal volume (knob is in range 0-1, numberbox is not limited)
- knob/number box for onset detection threshold
- knob/number box for frequency of output square signal (0-10)
- knob/number box for output signal volume (knob is in range 0-1, numberbox is not limited)
- a scoping window showing the output signal for the channel

*/ 

/* Troubleshooting */

// if you want to start from the beginning
// the right way would be simply to close the Guardian window
// but SwingOSC seems to hang frequently due to scoping stuff
// Thus: the easy and hacky way to clean up all is very simply to do:
// Lang->Compile Library
// by means of it, you close server and GUI instances, and recompile the library
// (recompiling is useless but as a side effect it cleans all) 
// if you receive a:
/*
SwingOSC server failed to start
*/
// then, hard kill SwingOSC app from system


// if for some reasons server does not boot:
Server.local.boot ;

// if server seems to hang:
// Server.killAll // or just press the K button on Server window

*/

