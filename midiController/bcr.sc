/*
works as follows:

a = BCR.new; // a new instance
// then:
// you set a var with the name of the controller
// providing a func, where val is the value of the controller


a.k1 = { |val| val.postln } ;
a.gk4 = { |val| val.postln } ;
a.bb5 = { |val| val.postln } ;
*/

BCR {
	var gk1, gk2, gk3, gk4, gk5, gk6, gk7, gk8 ;
	var b1, b2, b3, b4, b5, b6, b7, b8 ;
	var bb1, bb2, bb3, bb4, bb5, bb6, bb7, bb8 ;
	var <k1, <k2, <k3, <k4, <k5, <k6, <k7, <k8 ;
	var kk1, kk2, kk3, kk4, kk5, kk6, kk7, kk8 ;
	var kkk1, kkk2, kkk3, kkk4, kkk5, kkk6, kkk7, kkk8 ;
	var <sw1, <sw2, <sw3, <sw4 ; // four button on the right
	// on/off presing the gks
	var <gkb1, <gkb2, <gkb3, <gkb4, <gkb5, <gkb6, <gkb7, <gbk8 ;

	var <numArray, <nameArray ;
	var <controlDict, <noteOnDict ;

	// you do what you need with this
	var <>internal;


	*new {
		//arg inport = 2 , outport = 2 ;
		// assuming 2 ports: US-122 + Xsession
		//	^super.new.initBRC(inport, outport);
		^super.new.initBRC;
	}

	initBRC {
		//arg inPort, outPort ;
		// internal representation of values, if needed
		internal = [] ;

		controlDict = IdentityDictionary.new ;
		noteOnDict =  IdentityDictionary.new ;
/*
//		MIDIClient.init(inPorts,outPorts);	// explicitly intialize the client
		MIDIClient.init ;	// explicitly intialize the client
		MIDIIn.connect(inPort, outPort);
//			inPorts.do({ arg i;
//			MIDIIn.connect(i, MIDIClient.sources.at(i));
//		});
*/
		// gk depends on "Encoder groups". Here we assume the first (topleft)
		numArray =
				Array.series(8, 1)
				++
				Array.series(40, 65)
				++
				Array.series(4,105)
				++
				Array.series(8, 33) ;
		nameArray =
			// general knobs
			Array.fill(8, {|ind| ("gk"++(ind+1)).asSymbol })
			++
			// buttons
			Array.fill(8, {|ind| ("b"++(ind+1)).asSymbol })
			++
			Array.fill(8, {|ind| ("bb"++(ind+1)).asSymbol })
			++
			//knobs
			Array.fill(8, {|ind| ("k"++(ind+1)).asSymbol })
			++
			Array.fill(8, {|ind| ("kk"++(ind+1)).asSymbol })
			++
			Array.fill(8, {|ind| ("kkk"++(ind+1)).asSymbol })
			++
			Array.fill(4, {|ind| ("sw"++(ind+1)).asSymbol })
			++
			Array.fill(8, {|ind| ("gkb"++(ind+1)).asSymbol })
			;
		numArray.do({ arg item ;
			controlDict[item] = {} ;
			 }) ;
		this.map ;
	}


	map {
		MIDIIn.control = { arg src, chan, num, val ;
			controlDict[num].value(val)
		} ;
	}


// general knobs

	gk1_ { arg func ;
		var num = numArray[nameArray.indexOf(\gk1)] ;
		controlDict[num] = func  ;
		gk1 = func ;
		}

	gk2_ { arg func ;
		var num = numArray[nameArray.indexOf(\gk2)] ;
		controlDict[num] = func  ;
		gk2 = func ;
		}

	gk3_ { arg func ;
		var num = numArray[nameArray.indexOf(\gk3)] ;
		controlDict[num] = func  ;
		gk3 = func ;
		}

	gk4_ { arg func ;
		var num = numArray[nameArray.indexOf(\gk4)] ;
		controlDict[num] = func  ;
		gk4 = func ;
		}

	gk5_ { arg func ;
		var num = numArray[nameArray.indexOf(\gk5)] ;
		controlDict[num] = func  ;
		gk5 = func ;
		}

	gk6_ { arg func ;
		var num = numArray[nameArray.indexOf(\gk6)] ;
		controlDict[num] = func  ;
		gk6 = func ;
		}

	gk7_ { arg func ;
		var num = numArray[nameArray.indexOf(\gk7)] ;
		controlDict[num] = func  ;
		gk7 = func ;
		}

	gk8_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\gk8)] ;
		controlDict[num] = func  ;
		gk1 = func ;
		}


// buttons

	b1_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\b1)] ;
		controlDict[num] = func  ;
		b1 = func ;
		}

	b2_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\b2)] ;
		controlDict[num] = func  ;
		b2 = func ;
		}

	b3_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\b3)] ;
		controlDict[num] = func  ;
		b3 = func ;
		}

	b4_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\b4)] ;
		controlDict[num] = func  ;
		b4 = func ;
		}

	b5_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\b5)] ;
		controlDict[num] = func  ;
		b5 = func ;
		}

	b6_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\b6)] ;
		controlDict[num] = func  ;
		b6 = func ;
		}

	b7_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\b7)] ;
		controlDict[num] = func  ;
		b7 = func ;
		}

	b8_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\b8)] ;
		controlDict[num] = func  ;
		b8 = func ;
		}


// 2nd row

	bb1_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\bb1)] ;
		controlDict[num] = func  ;
		bb1 = func ;
		}

	bb2_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\bb2)] ;
		controlDict[num] = func  ;
		bb2 = func ;
		}

	bb3_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\bb3)] ;
		controlDict[num] = func  ;
		bb3 = func ;
		}

	bb4_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\bb4)] ;
		controlDict[num] = func  ;
		bb4 = func ;
		}

	bb5_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\bb5)] ;
		controlDict[num] = func  ;
		bb5 = func ;
		}

	bb6_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\bb6)] ;
		controlDict[num] = func  ;
		bb6 = func ;
		}

	bb7_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\bb7)] ;
		controlDict[num] = func  ;
		bb7 = func ;
		}

	bb8_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\bb8)] ;
		controlDict[num] = func  ;
		bb8 = func ;
		}

// knobs


	k1_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\k1)] ;
		controlDict[num] = func ;
		k1 = func ;
		}

	k2_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\k2)] ;
		controlDict[num] = func   ;
		k2 = func ;
		}

	k3_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\k3)] ;
		controlDict[num] = func  ;
		k3 = func ;
		}

	k4_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\k4)] ;
		controlDict[num] = func  ;
		k4 = func ;
		}

	k5_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\k5)] ;
		controlDict[num] = func  ;
		k5 = func ;
		}

	k6_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\k6)] ;
		controlDict[num] = func  ;
		k6 = func ;
		}

	k7_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\k7)] ;
		controlDict[num] = func  ;
		k7 = func ;
		}

	k8_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\k8)] ;
		controlDict[num] = func  ;
		k8 = func ;
		}


// 2nd row

	kk1_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\kk1)] ;
		controlDict[num] = func  ;
		kk1 = func ;
		}

	kk2_ { arg func ;
		var num = numArray[nameArray.indexOf(\kk2)] ;
		controlDict[num] = func  ;
		kk2 = func ;
		}

	kk3_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\kk3)] ;
		controlDict[num] = func  ;
		kk3 = func ;
		}

	kk4_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\kk4)] ;
		controlDict[num] = func  ;
		kk4 = func ;
		}

	kk5_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\kk5)] ;
		controlDict[num] = func  ;
		kk5 = func ;
		}

	kk6_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\kk6)] ;
		controlDict[num] = func  ;
		kk6 = func ;
		}

	kk7_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\kk7)] ;
		controlDict[num] = func  ;
		kk7 = func ;
		}

	kk8_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\kk8)] ;
		controlDict[num] = func  ;
		kk8 = func ;
		}


// 3nd row

	kkk1_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\kkk1)] ;
		controlDict[num] = func  ;
		kkk1 = func ;
		}

	kkk2_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\kkk2)] ;
		controlDict[num] = func  ;
		kkk2 = func ;
		}

	kkk3_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\kkk3)] ;
		controlDict[num] = func  ;
		kk3 = func ;
		}

	kkk4_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\kkk4)] ;
		controlDict[num] = func  ;
		kkk4 = func ;
		}

	kkk5_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\kkk5)] ;
		controlDict[num] = func  ;
		kkk5 = func ;
		}

	kkk6_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\kkk6)] ;
		controlDict[num] = func  ;
		kkk6 = func ;
		}

	kkk7_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\kkk7)] ;
		controlDict[num] = func  ;
		kkk7 = func ;
		}

	kkk8_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\kkk8)] ;
		controlDict[num] = func  ;
		kkk8 = func ;
		}

// switch buttons,  topleft, topright, bottomleft, bottomright

	sw1_ { arg func ;
		var num = numArray[nameArray.indexOf(\sw1)] ;
		controlDict[num] = func  ;
		sw1 = func ;
		}

	sw2_ { arg func ;
		var num = numArray[nameArray.indexOf(\sw2)] ;
		controlDict[num] = func  ;
		sw2 = func ;
		}

	sw3_ { arg func ;
		var num = numArray[nameArray.indexOf(\sw3)] ;
		controlDict[num] = func  ;
		sw3 = func ;
		}

	sw4_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\sw4)] ;
		controlDict[num] = func  ;
		sw4 = func ;
		}

// on/onff over gk
	gkb1_ { arg func ;
		var num = numArray[nameArray.indexOf(\gkb1)] ;
		controlDict[num] = func  ;
		gkb1 = func ;
		}

	gkb2_ { arg func ;
		var num = numArray[nameArray.indexOf(\gkb2)] ;
		controlDict[num] = func  ;
		gkb2 = func ;
		}

	gkb3_ { arg func ;
		var num = numArray[nameArray.indexOf(\gkb3)] ;
		controlDict[num] = func  ;
		gkb3 = func ;
		}

	gkb4_ { arg func ;
		var num = numArray[nameArray.indexOf(\gkb4)] ;
		controlDict[num] = func  ;
		gkb4 = func ;
		}

	gkb5_ { arg func ;
		var num = numArray[nameArray.indexOf(\gkb5)] ;
		controlDict[num] = func  ;
		gkb5 = func ;
		}

	gkb6_ { arg func ;
		var num = numArray[nameArray.indexOf(\gkb6)] ;
		controlDict[num] = func  ;
		gkb6 = func ;
		}

	gkb7_ { arg func ;
		var num = numArray[nameArray.indexOf(\gkb7)] ;
		controlDict[num] = func  ;
		gkb7 = func ;
		}

	gkb8_ 	{ arg func ;
		var num = numArray[nameArray.indexOf(\gkb8)] ;
		controlDict[num] = func  ;
		gkb1 = func ;
		}

}