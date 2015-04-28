/*
// IM4Voice parse a score via IM4Parser then chain event envelopes via its method building 3 whole voice envelopes. The original scre is written in 144 beats (36 pages of 4 beats each); to rescale envelopes times sampleEnvelopes poll values from each envelopes including the tempo one 0,01 sec rate, 1/100 of a beat taken @240 bpm. In the and it's possible to build rescaled envelopes multipling each segment at sample rate following the formula (sampledTempo / 240) * 0.01.

IM4Voice {
	var <>voiceID ; //to title the draw screen
	var <>eventList ; // collects all the events
	// an array of multiplier to rescale times for final envelopes
	var <>tempoArr, <>finAmp, <>finFreq, <>finFilt;
	var <>scanRate, <>measures, <>quarters ;

	// constructor
	*new { arg voiceFile ;
		^super.new.initIM4Voice(voiceFile)
	}

	initIM4Voice { arg aVoiceFile ;
		var dur, amp, freq, filt, envArray ;
		var ampArr, freqArr, filtArr ;
		// define tempo envelopes divinding beats, taking a beat at 240 bpm
		var tempoEnvelope = Env([128, 128, 88, 144, 124, 100, 100, 172, 136, 136, 96, 148, 148, 80, 168, 168], [12, 12, 8, 4, 12, 12, 12, 8, 16, 8, 4, 12, 8, 4, 12], \linear) ;
		// open the aVoiceFile
		voiceID = aVoiceFile.at(aVoiceFile.size - 6)++aVoiceFile.at(aVoiceFile.size - 5) ;
		if (voiceID.first == 0) {voiceID = voiceID.last} {voiceID = voiceID} ;
		measures = 144 ; // from score
		scanRate = 100 ; // how many ticks per unit
		eventList = IM4Parser(aVoiceFile) ;
		envArray = this.buildVoiceEnvelopes(eventList) ;
		amp = envArray[1] ;
		freq = envArray[2] ;
		filt = envArray[3] ;
		ampArr = this.sampleEnvelope(amp) ;
		freqArr = this.sampleEnvelope(freq) ;
		filtArr = this.sampleEnvelope(filt) ;
		tempoArr = this.sampleEnvelope(tempoEnvelope) ;
		finAmp = this.finalEnvelope(ampArr) ;
		finFreq = this.finalEnvelope(freqArr) ;
		finFilt = this.finalEnvelope(filtArr) ;
	}

	// chains events envelopes for each field (dur, amp, freq, filt) and build 4 whole voice envelopes
	buildVoiceEnvelopes { arg eventArray ;
		var dur = [], ampL = [], ampT = [], freqL = [], freqT = [], filtL = [], filtT = [], envA, envFr, envFi ;
		eventArray.do{|ev|
			ev.postln ;
			dur = dur.add(ev[1]) ;
			ampL = ampL++(ev[2].levels) ;
			ampT = ampT++(ev[2].times).add(0);
			freqL = freqL++(ev[3].levels) ;
			freqT = freqT++(ev[3].times).add(0) ;
			filtL = filtL++(ev[4].levels) ;
			filtT = filtT++(ev[4].times).add(0) ;
		} ;
		ampT.removeAt(ampT.size - 1) ;
		freqT.removeAt(freqT.size - 1) ;
		filtT.removeAt(filtT.size - 1) ;
		// creates envelopes and set dur to 144
		envA = Env(ampL, ampT, \linear) ;
		envA.duration = measures ;
		envFr = Env(freqL, freqT, \linear) ;
		envFr.duration = measures ;
		envFi = Env(filtL, filtT, \linear) ;
		envFi.duration = measures ;
		dur = dur.sum.postln ;
		^[dur, envA, envFr, envFi] ;
	}

	// draw all the 3 envelopes in an unique window. it seems kinda useless but allow to see if event envelopes are in sync, and some eyeCandy too ;-)
	draw {
		var freq, amp, filt ;
		var hor = 200, ver = 1000 ;
		var win = Window("voice "++voiceID, Rect(20, 20, ver, hor)).front ;
		win.background_(Color.white) ;

		freq = this.finFreq.levels.normalize(2,hor-2).resamp1(ver);
		amp = this.finAmp.levels.normalize(2,hor-2).resamp1(ver);
		filt = this.finFilt.levels.normalize(2,hor*0.5).resamp1(ver);

		win.drawFunc_{
			Pen.width = 1.25 ;

			[amp, freq, filt].do{|which, i|
				Pen.moveTo(0 @ 500) ;
				which[1..].do{|it, id|
					Pen.lineTo((id+1) @ (hor-it)) ;
				} ;
				Pen.strokeColor_(Color.hsv(i * 0.3, 0.95, 0.9, 0.7));
				Pen.stroke ;
			}
		}
	}

	sampleEnvelope {arg envelope ;
		var arrayedEnv = [] ;
		(measures*scanRate).do{|i|
			arrayedEnv = arrayedEnv.add(envelope.at(i/scanRate)) ;
		} ;
		^arrayedEnv ;
	}

	finalEnvelope { arg array ;
		var finalEnv, tempoMul ;
		// set the dur of 1/100 of a beat @ tempoArr speed
		tempoMul = 60 / tempoArr * 4 / scanRate ;
		tempoMul.removeAt(tempoMul.size - 1) ;
		tempoMul.sum.asTimeString.postln ;
		finalEnv = Env(array, tempoMul, \linear) ;
		^finalEnv ;
	}
}

// text files parsing class
IM4Parser {

	var <>eventList ; // collects all the events
	var <>text ;  // raw text from score file

	// constructor
	*new { arg voiceFile ;
		^super.new.initIM4Parser(voiceFile)
	}

	initIM4Parser { arg aVoiceFile ;
		// open the aVoiceFile
		var file ;
		var events ;
		var evt ;
		file = File(aVoiceFile, "r") ;
		text  = file.readAllString ;
		file.close ;
		// parse and retrieve
		events = text.replace("#", "").split($*).select{|i| i.size > 0} ;
		// events.postln ;
		events.do{|ev|
			evt = this.parseEvent(ev) ;
			evt = this.processEvent(evt) ;
			eventList = eventList.add(evt) ;
		} ;
		^eventList ;
	}


	parseEvent { arg eventString ;
		var event ;
		// "new Event: ".postcln ;
		// creates an array of events
		event = eventString.split($@).select{|i| i.size > 0} ;
		event = event.collect{|i| i.split($\n).select{|i| i.size > 0}} ;
		// event.postln ;
		^event ;
	}


	processEvent { arg singleEvent ;
		var processedEvent ;
		processedEvent = if (singleEvent.size == 2) {this.processPause(singleEvent)} {this.processRadio(singleEvent)} ;

		^processedEvent ;
	}

	processPause{ arg pause ;
		var processedPause ;
		var id = pause[0][0].asInteger ;
		var dur = pause[1][0].split($+).collect{|i| i.interpret}.sum ;
		var amp = Env([1, 1], [dur], \linear);
		var freq = Env([0, 0], [dur], \linear) ;
		var filt = Env([0, 0], [dur], \linear) ;
		processedPause = [id, dur, amp, freq, filt] ;
		// processedPause.postln ;
		^processedPause ;

	}

	processRadio{ arg radio ;
		var processedRadio ;
		var id = radio[0] ;
		var dur = radio[1] ;
		var amp = radio[2], amp2 = [] ;
		var freq = radio[3], freq2 = [] ;
		var filter = radio[4] ;

		id = id[0].asInteger ;
		dur = dur[0].split($+).collect{|i| i.interpret}.sum ;

		amp = amp.select{|i| i != "g"} ;
		amp = amp.collect{|i|
			if (i.size > 3) {i.split($+).collect{|i| i.interpret}.sum} {i.interpret}
		} ;
		amp.do{|i, x|
			if (i == nil) {amp2 = amp2.add(amp[x - 2]).add(0)} {amp2 = amp2.add(i)} ;
		} ;
		if (amp2.last == 0) {amp2.removeAt(amp2.size - 1)} {} ;
		amp2 = [amp2.select{|i, x| x.even}, amp2.select{|i, x| x.odd}] ;

		freq = freq.select{|i| i != "g"} ;
		freq = freq.collect{|i|
			if (i.size > 3) {i.split($+).collect{|i| i.interpret}.sum} {i.interpret}
		} ;
		freq.do{|i, x|
			if (i == nil) {freq2 = freq2.add(freq[x - 2]).add(0)} {freq2 = freq2.add(i)} ;
		} ;
		if (freq2.last == 0) {freq2.removeAt(freq2.size - 1)} {} ;
		freq2 = [freq2.select{|i, x| x.even}, freq2.select{|i, x| x.odd}] ;

		filter = filter.collect{|i|
			if (i.size > 3) {i.split($+).collect{|i| i.interpret}.sum} {i.interpret}
		} ;
		filter = [filter.select{|i, x| x.even}.stutter(2), [filter.select{|i, x| x.odd}, 0].lace(filter.size - 1)] ;

		processedRadio = [id, dur, amp2, freq2, filter] ;
		// debug stuff
		processedRadio[2][0].do{|i, x| if (i.isInteger == false) {["amp value", processedRadio[0],i, processedRadio[2]].postln}{}} ;
		if (processedRadio[2][1].sum != processedRadio[1]) {["amp time", id, processedRadio[2][1].sum, processedRadio[1]].postln} {} ;
		if (processedRadio[3][1].sum != processedRadio[1]) {["freq time", id, processedRadio[3][1].sum, processedRadio[1]].postln} {} ;
		if (processedRadio[4][1].sum != processedRadio[1]) {["filt time", id, processedRadio[4][1].sum, processedRadio[1]].postln} {} ;
		processedRadio[3][0].do{|i, x| if (i.isInteger == false) {["freq value", processedRadio[0],i].postln}{}} ;
		processedRadio[4][0].do{|i, x| if (i.isInteger == false) {["filt value", processedRadio[0],i].postln}{}} ;

		processedRadio = this.buildEnvelopes(processedRadio) ;
		// processedRadio.postln ;
		^processedRadio ;

	}

	buildEnvelopes{ arg radio ;
		var envRadio ;
		var ampE ;
		var freqE ;
		var filtE ;
		ampE = Env(radio[2][0], radio[2][1], \linear) ;
		freqE = Env(radio[3][0], radio[3][1], \linear) ;
		filtE = Env(radio[4][0], radio[4][1], \linear) ;
		envRadio = radio.putEach([2, 3, 4], [ampE, freqE, filtE]) ;
		^envRadio ;
	}
}

// performance class
IM4Performer {
	var <>voice1, <>voice2, <>voice3, <>voice4, <>voice5, <>voice6, <>voice7, <>voice8, <>voice9, <>voice10, <>voice11, <>voice12 ; // each voice
	var <>voiceArr ; // structured voice data
	var <>door ; // serial port
	var <>scanTime = 0.2 ; // scan time
	var <>task ; // polling task

	// constructor
	*new { arg aPath ; // where the folder with score files is placed
		^super.new.initIM4Performer(aPath) ;
	}

	initIM4Performer { arg path ;
		door = SerialPort.new("/dev/tty.usbmodem24121") ;

		voice1 = IM4Voice(path++"01.txt") ;
		voice2 = IM4Voice(path++"02.txt") ;
		voice3 = IM4Voice(path++"03.txt") ;
		voice4 = IM4Voice(path++"04.txt") ;
		voice5 = IM4Voice(path++"05.txt") ;
		voice6 = IM4Voice(path++"06.txt") ;
		voice7 = IM4Voice(path++"07.txt") ;
		voice8 = IM4Voice(path++"08.txt") ;
		voice9 = IM4Voice(path++"09.txt") ;
		voice10 = IM4Voice(path++"10.txt") ;
		voice11 = IM4Voice(path++"11.txt") ;
		voice12 = IM4Voice(path++"12.txt") ;

		voiceArr = [voice1, voice2, voice3, voice4, voice5, voice6, voice7, voice8, voice9, voice10, voice11, voice12] ;
	}

	// takes as arg an array of voices and play them sending to serial port
	play { arg array ;

		task = Task{
			var id, idx, voice, counter ;
			inf.do{|i|
				counter = i * scanTime ;
				idx = i % array.size ;
				id = array[idx] ;
				voice = voiceArr[id] ;
				door.put(id).postln ;
				1/4 * scanTime.wait ;
				door.put(voice.finAmp.at(counter)).postln ;
				1/4 * scanTime.wait ;
				door.put(voice.finFreq.at(counter)).postln ;
				1/4 * scanTime.wait ;
				door.put(voice.finFilt.at(counter)).postln ;
				1/4 * scanTime.wait ;
			} ;
		} ;
		task.play ;
	}

	playAll {
		this.play(voiceArr) ;
	}
	stop {
	this.task.stop ;
	}
}
*/