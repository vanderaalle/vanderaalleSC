/*
SO : Model {

	classvar <classDict ;		// a dict containing className -> [range]

	var <>objectDict ; 	// the global dict to store all data
	var rank ;			// can be useful to have a progressive ordering
	var <soundPath ;		// the path of the sound to be annotated
	var <server, <buffer, <duration ; 	// the server, the buffer, its duration for playback
	var defPath ; 		// a default path where to save txt (internal usage)
	var <score ;

	*new { arg soundPath ;
		^super.new.initSO(soundPath)
		}

	initSO { arg aSoundPath ;
		objectDict = IdentityDictionary.new ; // just creating a Dict
		rank = 0 ;

		classDict = IdentityDictionary[
			\Hn		-> [[-2.5, -1.5],	[0.0, 1.0],	[0.0, 1.0]],
			\N 		-> [[-1.5, -0.5],	[0.0, 1.0],	[0.0, 1.0]],
			\Ni 		-> [[-0.5, 0.5],	[0.0, 1.0],	[0.0, 1.0]],
			\Nii		-> [[0.5, 1.5],	[0.0, 1.0],	[0.0, 1.0]],
			\Zn		-> [[1.5, 2.5],	[0.0, 1.0],	[0.0, 1.0]],
			\Hx 		-> [[-2.5, -1.5],	[1.0, 2.0],	[0.0, 1.0]],
			\X 		-> [[-1.5, -0.5],	[1.0, 2.0],	[0.0, 1.0]],
			\Xi 		-> [[-0.5, 0.5],	[1.0, 2.0],	[0.0, 1.0]],
			\Xii 	-> [[0.5, 1.5],	[1.0, 2.0],	[0.0, 1.0]],			\Zx 		-> [[1.5, 2.5],	[1.0, 2.0],	[0.0, 1.0]],
			\P 		-> [[1.5, 2.5],	[0.0, 2.0],	[1.0, 2.0]],
			\Yii 	-> [[0.5, 1.5],	[0.0, 2.0],	[1.0, 2.0]],
			\Yi 		-> [[-0.5, 0.5],	[0.0, 2.0],	[1.0, 2.0]],
			\Y 		-> [[-1.5, -0.5],	[0.0, 2.0],	[1.0, 2.0]],
			\T 		-> [[-2.5, -1.5],	[0.0, 2.0],	[1.0, 2.0]],
			\E 		-> [[-2.5, -1.5],	[0.0, 2.0],	[2.0, 3.0]],
			\W		-> [[-1.5, -0.5],	[0.0, 2.0],	[2.0, 3.0]],
			\Phi		-> [[-0.5, 0.5],	[0.0, 2.0],	[2.0, 3.0]],
			\K		-> [[0.5, 1.5],	[0.0, 2.0],	[2.0, 3.0]],
			\A 		-> [[1.5, 2.5],	[0.0, 2.0],	[2.0, 3.0]]
				] ;
		if ( aSoundPath.notNil, { this.loadSound(aSoundPath) }) ;


	 }


	loadSound { arg path ;
		server = server ? Server.local.boot ;
		soundPath = path ;
		server.doWhenBooted({
			buffer = Buffer.read(server, path, action:
				{ 	"file loaded".postln ; duration = buffer.numFrames/server.sampleRate ;
					 }) ;
			SynthDef(\SOPlayerMono,
	 		{ arg bufnum, amp = 1, out = 0, dur = 1, startPos = 0, loop = 0, rate = 1, chan = 1 ;
			Out.ar(out,
				Line.kr(amp, amp, dur, doneAction:2)
				*
				[
				PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum),
							rate: rate, startPos: startPos, loop: loop),
				PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum),
							rate: rate, startPos: startPos, loop: loop)
				]
				)
			}).send(server) ;
			SynthDef(\SOPlayerStereo,
	 		{ arg bufnum, amp = 1, out = 0, dur = 1, startPos = 0, loop = 0, rate = 1, chan = 2 ;
			Out.ar(out,
				Line.kr(amp, amp, dur, doneAction:2)
				*
				PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum),
							rate: rate, startPos: startPos, loop: loop)
				)
			}).send(server) ;
			this.loadProcessing ;
			SynthDef(\sineDef, { arg pitch, amp ;
				 Out.ar(0, SinOsc.ar(pitch.postln, mul: amp/9)) }).send(server) ;
		}) ;

	}


	loadProcessing {
	// should be done better using server structure
//		server.doWhenBooted({
			SynthDef(\SOPitchFollow,
			{ arg bufnum, amp = 1, out = 0, dur = 1, startPos = 0, loop = 0, rate = 1 ;
				var in, ampli, freq, hasFreq ;
				in = Line.kr(amp, amp, dur, doneAction:2)
				*
				Mix.new(PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum),
							rate: rate, startPos: startPos, loop: loop)) ;
				ampli = Amplitude.kr(in, 0.05, 0.05);
				# freq, hasFreq = Pitch.kr(in);
				out = SinOsc.ar(freq, mul: amp);
				Out.ar(0,[out, out])
		}).send(server);
//	})

	}

////////////////

// play the file

	play { arg selector = nil, to ;//, loop = 0 ; // cannot work because of Line
		var from ;
		if (soundPath.isNil, { "no file found. Please load one".postln ; ^this } );
		// selector
		if (to.isNil, { to = buffer.numFrames/server.sampleRate }) ;
		case {(selector.class == Symbol).or(selector.class == String)}
				{ from = objectDict[selector.asSymbol][0] ;
				  to = from + objectDict[selector.asSymbol][1] ;
				}
			{ selector.notNil }
				{ from = selector }
			{ selector.isNil }
				{ from = 0 } ;
		// select in function of channels: maybe problem with stereo?
		if ( buffer.numChannels == 1, {
			^Synth(\SOPlayerMono, [\bufnum, buffer.bufnum, \dur, to-from,
				\startPos, (from*server.sampleRate),
				\loop, 0])
				}, {
			^Synth(\SOPlayerStereo, [\bufnum, buffer.bufnum, \dur, duration,
				\startPos, from*server.sampleRate,
				\loop, 0])

			})
	}


	pitch { arg selector = 0, to ;//, loop = 0 ; // cannot work because of Line
		var from ;
		if (soundPath.isNil, { "no file found. Please load one".postln ; ^this } );
		// selector
		if (to.isNil, { to = buffer.numFrames/server.sampleRate }) ;
		if ((selector.class == Symbol).or(selector.class == String),
			{
			from = objectDict[selector.asSymbol][0] ;
			to = from + objectDict[selector.asSymbol][1] ;
			}, {
			from = selector
			}
			) ;
		Synth(\SOPitchFollow, [\bufnum, buffer.bufnum, \dur, to-from,
				\startPos, (from*server.sampleRate),
				\loop, 0])

	}



// registers

	*site { arg oct ;
		var win, sl ;
		// allowed range: 1, 9
		{
		EnvGen.ar(Env.perc)*
		//LFPulse.ar(3)*
		SinOsc.ar(oct.clip(1, 9).octcps.postln) }.play
	}



	*dyn { arg val ; // val is the value you will insert: 0-9
		// pppp, ppp, pp,  p, mp, mf, f, ff, fff
		var dynArr = [45, 50, 55, 60, 65, 70, 75, 80, 90] ;
		{SinOsc.ar(mul: dynArr[val.clip2(8)].linlin(0, 96, -96, 0).dbamp.postln) }.play

	}



////////// editing

	add { arg name, attack, duration, profile, caliber, variation,
				site, dynamics, group, text ;
		// name: string (-> symbol)
		// you can pass a string or a symbol
		// text: string
		group = group ? 0 ;
		objectDict.add(name.asSymbol -> [attack, duration, profile, caliber, variation,
				site, dynamics, group, text, rank]) ;
		rank = rank + 1 ;
		this.changed(\addRemove, [name].addAll(objectDict[name]))
	}

	remove { arg name ;
		// name: string -> asSymbol
		// you can pass a string or a symbol
		if ( name.isNil, { ^this.clean } ) ;
		objectDict.removeAt(name.asSymbol) ;
		this.changed(\addRemove, [name].addAll(objectDict[name]))

	}

	clean {
		objectDict = IdentityDictionary.new ;
		rank = 0 ;
		//collection = "a Collection" ;
	}

	retrieve { arg name ;
		if ( objectDict[name.asSymbol].notNil,
			{ ^objectDict[name.asSymbol] },
			{^Array.fill(10, { nil }) } )
	}


// changing each params' value

	changeProfile { arg name, profile ;
		name = name.asSymbol ;
		objectDict[name][2] = profile ;
		this.changed(\position, [name].addAll(objectDict[name]))
	}

	changeCaliber { arg name, caliber ;
		name = name.asSymbol ;
		objectDict[name][3] = caliber ;
		this.changed(\position, [name].addAll(objectDict[name]))
	}


	changeVariation { arg name, variation ;
		name = name.asSymbol ;
		objectDict[name][4] = variation ;
		this.changed(\position, [name].addAll(objectDict[name]))
	}

// global
	changePosition { arg name, profile, caliber, variation ;
		var position, remarks, rank ;
		name = name.asSymbol ;
		objectDict[name][2] = profile ;
		objectDict[name][3] = caliber ;
		objectDict[name][4] = variation ;
		this.changed(\position, [name].addAll(objectDict[name]))
	}



	changeAttack { arg name, attack ;
		name = name.asSymbol ;
		objectDict[name][0] = attack ;
		this.changed(\position, [name].addAll(objectDict[name]))
	}

	changeDuration { arg name, duration ;
		name = name.asSymbol ;
		objectDict[name][1] = duration ;
		this.changed(\position, [name].addAll(objectDict[name]))
	}

	changeSite { arg name, site ;
		name = name.asSymbol ;
		objectDict[name][5] = site ;
		this.changed(\position, [name].addAll(objectDict[name]))
	}

	changeDynamics { arg name, dynamics ;
		name = name.asSymbol ;
		objectDict[name][6] = dynamics ;
		this.changed(\position, [name].addAll(objectDict[name]))
	}

	changeGroup { arg name, group ;
		name = name.asSymbol ;
		objectDict[name][7] = group.asInteger ;
		this.changed(\position, [name].addAll(objectDict[name]))
	}

	changeText { arg name, text ;
		name = name.asSymbol ;
		objectDict[name][8] = text ;
		this.changed(\position, [name].addAll(objectDict[name]))
	}


	changeFeatures { arg name, profile, caliber, variation,
				site, dynamics, group, text ;
		var attack, duration, rank ;
		name = name.asSymbol ;
		attack = objectDict[name][0];
		duration = objectDict[name][1];
 		rank = objectDict[name].reverse[0] ;
		objectDict[name] = [attack, duration, profile, caliber, variation,
				site, dynamics, group, text].add(rank) ;
		this.changed(\position, [name].addAll(objectDict[name]))
	}


	changeAll { arg name,  attack, duration, profile, caliber, variation,
				site, dynamics, group, text ;
		var rank ;
		name = name.asSymbol ;
 		rank = objectDict[name].reverse[0] ;
		objectDict[name] = [attack, duration, profile, caliber, variation,
				site, dynamics, group, text].add(rank) ;
		this.changed(\position, [name].addAll(objectDict[name]))
	}

	rename { arg name, newName ;
		name = name.asSymbol ;
		newName = newName.asSymbol ;
		objectDict[newName.asSymbol] = objectDict[name.asSymbol] ;
		this.remove(name) ;
		this.changed(\addRemove, [newName].addAll(objectDict[newName]))
	}

// Storing/retrieving in internal format
// actually useless. So undocumented

	writeToArchive { arg path ;
		this.writeArchive(path)
	}

	readFromArchive { arg path ;
		^Object.readArchive(path)
	}



// open/close txt file

	save { arg path ;
		var file, objectDef ;
		if ( path.isNil ) {  path = defPath } { defPath = path } ;
		file = File(path, "w") ;
		objectDict.keys.do({ arg name ;
			objectDef = objectDict[name] ;
			file.write(name) ;
			objectDef.do({|field| file.write("\t"+field)}) ;
			file.write("\n")
		}) ;
		file.close ;
	}

	open { arg path, clean = true ;
		var name, attack, duration, profile, caliber, variation,
			site, dynamics, group, text, rank ;
		var arr ;
		defPath = path ;
		if (clean) { this.clean } ;
		arr = TabFileReader.read(defPath) ;
		arr.do({ arg item ;
			name = item[0].asSymbol ;
			#attack, duration, profile, caliber, variation,
			site, dynamics = item[1..7].collect({|i| i.interpret});
			group = item[8].asInteger ;
			text = item[9] ;
//			rank = item[10].asInteger ;
			this.add(name, attack, duration, profile, caliber, variation,
			site, dynamics, group, text) ;//, rank)
		}) ;
	}


	import { arg auPath, clean = true ;
		var name, attack, duration ;
		var arr ;
		if (clean) { this.clean } ;
		arr = TabFileReader.read(auPath) ;
		arr.do({ arg item ;
			name = item[0].asSymbol ;
			attack = item[1].asFloat ;
			duration = item[2].asFloat ;
			this.add(name, attack, duration)
		}) ;

	}


	fromAudacity { arg auPath, clean = true ;
		var name, attack, duration ;
		var arr ;
		if (clean) { this.clean } ;
		arr = TabFileReader.read(auPath) ;
		arr.do({ arg item ;
			name = item[2].asSymbol ;
			attack = item[0].asFloat ;
			duration = item[1].asFloat - attack ;
			this.add(name, attack, duration)
		}) ;

	}

// inspecting

	getClass { arg name ;
		var itsClass, objectList ;
		name = name.asSymbol ;
		classDict.keys.do({ arg class ;
			objectList = this.findInClass(class) ;
			if ( objectList.includes(name), { itsClass = class }) ;
		}) ;
		^itsClass
	}

	findInClass { arg className ;
		var range = classDict[className.asSymbol] ;
		var objectList = [] ;
		var x, y, z ;
		objectDict.do({ arg item ;
			#x,y,z =  item[2..4] ;
			if ( ((x >= range[0][0])
				// includes both border --> can be ambiguous
				.and(x <= range[0][1])
				.and(y >= range[1][0])
				.and(y <= range[1][1])
				.and(z >= range[2][0])
				.and(z <= range[2][1]))
				,
				{ objectList = objectList.add(objectDict.findKeyForValue(item))
					} )
		})
		^objectList
		}

	// returns all the sustained objects
	findSustained {
		var objectList = [] ;
		[\Hn, \N, \Hx, \X, \T, \Y, \E, \W].do({
			arg className ;
			objectList = objectList.addAll(this.findInClass(className))

		}) ;
		^objectList
	}

	// returns all the impulsive objects
	findImpulsive {
		var objectList = [] ;
		[\Ni, \Yi, \Phi, \Xi].do({
			arg className ;
			objectList = objectList.addAll(this.findInClass(className))

		}) ;
		^objectList
	}


	// returns all the iterative objects
	findIterative {
		var objectList = [] ;
		[\Zn, \Nii, \Zx, \Xii, \Yii, \P, \A, \K].do({
			arg className ;
			objectList = objectList.addAll(this.findInClass(className))

		}) ;
		^objectList
	}

	// returns all the central objects
	findCentral {
		var objectList = [] ;
		[\N, \Ni, \Nii, \X, \Xi, \Xii, \Y, \Yi, \Yii].do({
			arg className ;
			objectList = objectList.addAll(this.findInClass(className))
		}) ;
		^objectList
	}


	// returns all the excentric objects
	findExcentric {
		var objectList = [] ;
		[\E, \W, \Phi, \K, \A].do({
			arg className ;
			objectList = objectList.addAll(this.findInClass(className))
		}) ;
		^objectList
	}

	// returns all the homogeneous objects
	findHomogeneous {
		var objectList = [] ;
		[\T, \E, \P, \A].do({
			arg className ;
			objectList = objectList.addAll(this.findInClass(className))
		}) ;
		^objectList
	}

	// return a list of all the neighbors of start
	// ordered from nearest to farthest
	findNeighbours { arg name, number = objectDict.size ;
		var distDict = Dictionary.new ;
		var distance, keys, neighbours ;
		objectDict.keys.do({ arg otherName ;
			distance = this.calculateDistanceByPosition(name, otherName) ;
			distDict.add(distance -> otherName)
		}) ;
		keys = distDict.keys.asArray.sort ;
		keys.do({ arg key ;
			neighbours = neighbours.add(distDict[key])
		})
		^neighbours[1..(number-1)]
	}

	// private
	calculateDistanceByPosition	{ arg name, anotherName ;
		var distance, x1, y1, z1, x2, y2, z2 ;
		#x1, y1, z1 = objectDict[name][2..4] ;
		#x2, y2, z2 = objectDict[anotherName][2..4] ;
		distance = sqrt((x2-x1).squared + (y2-y1).squared + (z2-z1).squared) ;
		^distance
	}


//// other inspections

	findInGroup { arg group ;
		var objectList = [], objectDef ;
		objectDict.keys.do({ arg name ;
			objectDef = objectDict[name] ;
			if ( objectDef[7] == group, { objectList = objectList.add(name) })
		}) ;
		^objectList

	}

// objects having a duration included in range
	findInDurationRange { arg min, max ;
		var objectList = [], objectDef ;
		if ( max.isNil, { max = duration }) ;
		if ( min.isNil, { min = 0 }) ;
		objectDict.keys.do({ arg name ;
			objectDef = objectDict[name] ;
			if ( objectDef[2].inclusivelyBetween(min, max),
				{ objectList = objectList.add(name) })
		}) ;
		^objectList
	}


// objects COMPLETELY included between a temporal boundary
	findInDurationInterval { arg from, to ;
		var objectList = [], objectDef ;
		if ( to.isNil, { to = duration }) ;
		if ( from.isNil, { from = 0 }) ;
		objectDict.keys.do({ arg name ;
			objectDef = objectDict[name] ;
			if ( objectDef[1].inclusivelyBetween(from, to).and(
				(objectDef[1]+objectDef[2]).inclusivelyBetween(from, to)
				),
				{ objectList = objectList.add(name) })
		}) ;
		^objectList

	}

// objects which attack is included a temporal boundary
	findInAttackInterval { arg from, to ;
		var objectList = [], objectDef ;
		if ( to.isNil, { to = duration }) ;
		if ( from.isNil, { from = 0 }) ;
		objectDict.keys.do({ arg name ;
			objectDef = objectDict[name] ;
			if ( objectDef[1].inclusivelyBetween(from, to),
				{ objectList = objectList.add(name) })
		}) ;
		^objectList

	}



// objects having a site included in range
	findInSiteRange { arg min, max ;
		var objectList = [], objectDef ;
		if ( max.isNil, { max = 7 }) ;
		if ( min.isNil, { min = 0 }) ;
		objectDict.keys.do({ arg name ;
			objectDef = objectDict[name] ;
			if ( objectDef[5].inclusivelyBetween(min, max),
				{ objectList = objectList.add(name) })
		}) ;
		^objectList
	}


// objects having a dynamics included in range
	findInDynamicsRange { arg min, max ;
		var objectList = [], objectDef ;
		if ( max.isNil, { max = 7 }) ;
		if ( min.isNil, { min = 0 }) ;
		objectDict.keys.do({ arg name ;
			objectDef = objectDict[name] ;
			if ( objectDef[6].inclusivelyBetween(min, max),
				{ objectList = objectList.add(name) })
		}) ;
		^objectList
	}


	// max group index
	maxGroup {
		var max = 0 ;
		objectDict.do({ arg objectDef ;
			if ( objectDef[7] > max, { max =  objectDef[7] }) ;
		})
		^max
	}

	// an array containing all the indices
	groupArray {
		var num = [];
		objectDict.do({ arg objectDef ;
			num = num.add(objectDef[7]);
		}) ;
		^num.asSet.asArray.sort
	}

	// the size of the groupArray (how many groups)
	groupNumber {
		^this.groupArray.size
	}

	maxRank {
		var rank, rankList = [] ;
		objectDict.keys.do({ arg key ;
					rank = objectDict[key].reverse[0] ;
					rankList = rankList.add(rank) ;
		}) ;
		^rankList.sort.reverse[0]
		}

/////// print info support

	print { arg name ;
		var profile, caliber, variation, attack, duration, site, dynamics, group, text, rank ;
		var class ;
		if ( name.isNil ) {^this.printAll} ;
		if ( objectDict.includesKey(name.asSymbol).not, { ^"no object with such a name".postln }) ;
		#attack, duration, profile, caliber, variation, site, dynamics, group, text, rank
				= objectDict[name.asSymbol] ;
		if ([profile, caliber, variation].includes(nil), { class = "-" },
			{ class = this.getClass(name) }) ;
		"O+++++++++++++++++++++++++++++O".postln ;
		"name: ".post; name.postln ;
		"attack: ".post ; attack.postln ;
		"duration: ".post ; duration.postln ;
		"profile: ".post ; profile.postln ;
		"caliber: ".post ; caliber.postln ;
		"variation: ".post ; variation.postln ;
		"site: ".post ; site.postln ;
		"dynamics: ".post ; dynamics.postln ;
		"group: ".post ; group.postln ;
		"text: ".post ; text.postln ;
		"rank: ".post ; rank.postln ;
		"class: ".post ; class.postln ;
		"O-----------------------------O\n\n\n".postln
		}


	printAll {
		if (objectDict.keys.size == 0, { "There are no objects in the objectDict".postln ; ^this }) ;
		objectDict.keys.do({ arg key ;
			this.print (key)
		})
	}

 // Interface towards other classes

	makeGui { SOGui(this) }

	makeSpace { SOSpace (this) }

	makeScore { score = SOScore (this) }



}

*/