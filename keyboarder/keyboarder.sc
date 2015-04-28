Keyboarder {

	var <>factor, <>transp, <>space, <>crlf, <>map ;
	var <doc ;
	var <>title, <>bounds, <>background, <>stringColor, <>font ;
	var server ;
	var <>log, <>rec, <>startTime ;
	var <>notes ;

	var <>vol ; // use me as a general volume to be passed

	*new { arg factor = 1.0, transp = 0,
			space = true, crlf = true,
			map,
			title = "Scriptorium",
			bounds = Rect(1280-640, 800-480, 640, 480),
			background = Color(0,0,0.4),
			stringColor = Color.white,
			font = Font.new("Futura", 30),
			rec = true ;
		^super.new.initKeyboarder([factor, transp, space, crlf, map, title, bounds, background, stringColor, font, rec])
	}

	initKeyboarder { arg args ;
		// protecting against auto syntax colorize
		Document.globalKeyDownAction_({}) ;
		vol = 1 ;
		#factor, transp, space, crlf, map,
			title, bounds, background, stringColor, font, rec = args ;
		map = map ? Array.series(58) ;
		server = Server.local ;
		server.waitForBoot({
			SynthDef(\keySquare, { arg freq, amp = 0.1, transp = 0, out = 0, width=0.5 ;
				var dur ;
				freq = freq.clip(0, 100.midicps) ;
				dur = 3-(freq.cpsmidi*0.01) ;
				Out.ar(out,
				Pan2.ar(Limiter.ar(
				FreeVerb.ar(
RLPF.ar(
Pulse.ar(freq, mul:amp*0.15, width:width)*LFNoise1.ar(20)*EnvGen.kr(Env.perc(releaseTime:dur), doneAction:2), freq*3) +
				RLPF.ar(
				FreeVerb.ar(
					EnvGen.kr(Env.perc(releaseTime:dur), doneAction:2)
					*CombL.ar(
						Pulse.ar(freq, mul:amp, width:width),
						0.2, 0.2, 4
				),
				0.9, 0.9
				)*SinOsc.ar(freq), freq, XLine.ar(4, 0.01, dur))*XLine.kr(2, 0.25, dur)
				+SoundIn.ar*SinOsc.ar(freq) // adding the pression sound
				,
				0.4, 0.7, 0.9)
				,0.2)
				,
				LFNoise1.ar(0.1))
				)
			}).send(server) ;

/*
			SynthDef(\keySquare, { arg freq, amp = 0.1, transp = 0, out = 0, width=0.5 ;
				Out.ar(out,
				Pan2.ar(
				EnvGen.kr(Env.perc, doneAction:2)
				*Pulse.ar(freq, mul:amp, width:width)),
				LFNoise1.ar(3))
			}).send(server) ;
*/
		}) ;
		doc = Document.new.title_(title)
			.bounds_(bounds)
			.background_(background)
			.stringColor_(stringColor)
			.font_(font)
			.keyDownAction_({arg doc, key, modifiers, keycode;
				var width, pitch, amp = 0.9 ;
				keycode.postln ;
				case
					{ (keycode == 32 and: {space.not}) } { amp = 0 }
					{ (keycode == 13 and: {crlf.not}) } { amp = 0 }
					{ keycode == 43 }
						{ server.volume.volume_((server.volume.volume.postln+1).max(-90).min(0)) ;
							vol = (vol+0.1).max(0).min(1)}
					{ keycode == 45 }
						{ server.volume.volume_((server.volume.volume-1).max(-90).min(0)) ;
							vol = (vol-0.1).max(0).min(1) }

				//"key, keycode: ".post ; [key, keycode].postln ;
					{ (keycode >= 65 and: { keycode < 123 }) }
					{ keycode = map[keycode-65] + 65 } ;
				//"remapped to: ".post ; keycode.postln ;
				width = 0.5/127*keycode.clip2(127) ;
				pitch = (keycode*factor+transp).clip2(136) ;
				/*
				"midi, note: ".post ; pitch.post ; ", ".post; pitch.midinote.postln ;
				"\n\n".postln ;
				pitch.midinote.postln ;
				*/
				if (rec) { this.record([pitch, key, modifiers, keycode]) } ; // check me
				Synth(\keySquare, [\freq, pitch.midicps, \amp, amp, \width, width]) ;
				this.changed(this, [key, vol]) ; // dependancy support
			});
			log = [] ;
			startTime = thisThread.seconds ;

	}

	reset {
		doc.string_("");
		startTime = thisThread.seconds ;
		log = [] ;
	}


	// a routine for some start up infos in the doc
	presentation { arg string, presFont = Font.new("Gill Sans", 90),
			color =  Color.new(1, 116/255,0);
		Routine({
			doc.stringColor_(color)
				.font_(presFont)
				.string_(string);
			100.do({ 	arg j ;
				doc.stringColor_(color.alpha_((100-j)*0.01)) ;
				0.05.wait ;
			}) ;
			doc.string_("")
				.stringColor_(Color.white)
				.font_(font) ;
		}).play(AppClock) ;
		doc.front

	}

	record { arg arr ;
		log = log.add([thisThread.seconds-startTime].addAll(arr))
	}

	writeLog { arg path = "/log.arc";
		log = [notes].addAll(log) ;
		log.writeArchive(path)
	}

	openLog { arg path = "/log.arc";
		log = Object.readArchive(path) ;
		notes = log[0] ;
		log = log[1..] ;
	}

	playFromLog {
		var key, time ;
		var width, pitch, amp = 0.2 ;
		var waitTime, actualTime, nextTime ;
		Routine({
			log[..log.size-2].do({ arg item, index ;
//			item.postln ;
				actualTime = item[0] ;
				nextTime = log[index+1][0] ;
				waitTime = nextTime - actualTime ;
				// should protect against not-Ascii
				key = if (item[4] <= 126) { item[2] } { " " } ;
				doc.string_(doc.string++key) ;

				pitch = item[1] ;
				width = 0.5/127*pitch.clip2(127) ;
				Synth(\keySquare, [\freq, pitch.midicps, \amp, amp, \width, width]) ;

				waitTime.wait ;
			})
		}).play(AppClock) ;
	}

}