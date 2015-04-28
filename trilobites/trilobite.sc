Trilobite {

	var <>deecee, <>id ; // deecee ref, id
	var <>in, <db ; // in chan and how much you boost on in
	var <>emitPattern, <>emission;
	var <>counter, <>mod, <>mul ; // a counter for emissions
	var <>hysteresis ; // time before geting active again
	var <>listenSynth, <>listenBus, <>listenTask, <>listenPeriod ;
	var <>amp ;
	var <>flag ; // \listen vs. \emit
	var <>watchDogTask, watchDogPeriod ; // reset flag to \listen every period
	var <>loThresh, <>hiThresh ;

	*new { arg deecee, id ;
		^super.new.initTrilobite(deecee, id)
	}

	initTrilobite { arg aDeecee, anId ;
		deecee = aDeecee ;
		id = anId ;
		in = id - 1 ;
		db = 60 ;
		amp = -10 ;
		emitPattern = [0.25, 1.25] ;
		hysteresis = 1 ;
		counter = 0 ; mod = 1; mul = 1;
		emission = {
			(emitPattern*(counter%mod+1*mul)).postln.do{|it| deecee.addEvent(id);
				it.wait ;
			} ;
			hysteresis.wait ;
			flag = \listen ;
		};
		Server.local.waitForBoot{
			{
				listenBus = Bus.control(Server.local) ;
				Server.local.sync ;
				listenSynth = { arg in, out, db = 60 ;
					Out.kr(out, Lag.kr(Amplitude.kr(SoundIn.ar(in)*db.dbamp))
						.ampdb)
				}.play(args: [\in, in, \out, listenBus, \db, db]) ;
			}.fork
		} ;
		loThresh = -15 ; hiThresh = 0 ; // in dB
		flag = \listen ;
		listenPeriod = 0.5 ;
		listenTask = Task({
			inf.do{
				listenBus.get{
					|v| amp = v ;
					case
					{ (amp > hiThresh) && ( flag == \listen) }
					{ deecee.addEvent(id) ; flag = \listen }
					// or
					{  (amp < hiThresh) && (amp > loThresh ) && ( flag == \listen ) }
					{ "I HEAR YOU!!!".postln ; flag = \emit;
						counter = counter+1; emission.fork }
					};
				listenPeriod.wait ;
			}
		}).play ;
		watchDogPeriod  = 30 ; // every 10 sec reset to \listen
		watchDogTask = Task({inf.do{ watchDogPeriod.wait; "reset".postln ;
			flag = \listen }}).play;

	}

	db_ { arg val ;
		listenSynth.set(\db, val) ;
		db = val ;
	}
}


/*
Server.killAll
s.boot

// the DC
~dc = DeeCee1.new(2);


~trilo = Trilobite(~dc, 1) ;
~gui = TrilobiteGUI.new([~trilo]) ;
~trilo.listenPeriod = 0.5 ;
~trilo.emitPattern = Morse.timesFor($r);

//g = TrilobiteGUI.new(Array.fill(8, {|i| Trilobite(d, i+1)}));


~trilos =

*/


TrilobiteGUI {

	var <>trilobiteArray;
	var <>slArr, <>lbArr ;
	var size ;
	var <>window ;
	var <>pollTask ;

	*new { arg trilobiteArray ;
		^super.new.initTrilobiteGui(trilobiteArray)
	}

	initTrilobiteGui { arg aTrilobiteArray ;
		var x = 10 ;
		var width = 12 ;
		var labels = ["dB", "lo", "in","hi"] ;
		var colors = [Color.hsv(0.1, 0.9, 1), Color.hsv(0.4, 0.9, 0.9), Color(0.3),  Color.hsv(0.0, 0.9, 0.9)] ;
		var actions, values ;
		trilobiteArray = aTrilobiteArray ;
		size = trilobiteArray.size ;
		values =  Array.fill(size, {|n|
			[
				trilobiteArray[n].db.linlin(0, 60, 0,1),
				trilobiteArray[n].loThresh.linlin(-96,6,0,1),
				trilobiteArray[n].amp.linlin(-96,6,0,1),
				trilobiteArray[n].hiThresh.linlin(-96,6,0,1)]
		}) ;
		actions = Array.fill(size, {|n| [
			{|me| trilobiteArray[n].db = me.value.linlin(0,1, 0, 60).postln },
			{|me| trilobiteArray[n].loThresh = me.value.linlin(0,1, -96, 6)},
			nil,
			{|me| trilobiteArray[n].hiThresh = me.value.linlin(0,1, -96, 6)}
		]}) ;
		window = Window("trilobite", Rect(10, 10, size * (10+ (width+5*4) +10), 270))
		.front ;
		slArr = [] ; lbArr = [] ;

		size.do{|i|
			StaticText(window, Rect(x, 10, width, 20) ).font_(Font("Futura", 20)).string_((trilobiteArray[i].id).asString) ;
			4.do{|j|
				slArr = slArr.add(Slider(window, Rect(x, 40, width, 200))
					.background_(colors[j]).action_(actions[i][j])
					.value_(values[i][j])
				) ;
				lbArr = lbArr.add(StaticText(window, Rect(x, 30, width, 10 ) ).font_(Font("Futura", 8)).string_("-0")) ;
				StaticText(window, Rect(x, 10+10+220, width, 10) ).font_(Font("Futura", 8)).string_(labels[j]) ;

				x = x+width+5 ;
			} ;
			x = x+20 ;
		} ;

		pollTask = Task{inf.do{
			var lab ;
			{
			trilobiteArray.do{|trilo, i|
				lab = lbArr.clump(4)[i] ;
				lab[0].string_(trilo.db.asString) ;
				lab[1].string_(trilo.loThresh.asString) ;
				lab[2].string_(trilo.amp.asString) ;
				lab[3].string_(trilo.hiThresh.asString) ;
				slArr.clump(4)[i][0].value = trilo.db.clip(0,60).linlin(0, 60, 0, 1) ;
				slArr.clump(4)[i][1].value = trilo.loThresh.clip(-96,6).linlin(-96, 6, 0, 1) ;
				slArr.clump(4)[i][2].value = trilo.amp.clip(-96,6).linlin(-96, 6, 0, 1) ;			slArr.clump(4)[i][3].value = trilo.hiThresh.clip(-96,6).linlin(-96, 6, 0, 1) ;
			};
			}.defer ;
			0.1.wait }
		}.play ;
		window.onClose_({pollTask.stop})
	}

}

