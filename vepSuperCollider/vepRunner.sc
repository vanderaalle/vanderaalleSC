// Andrea Valle, started on Nov 2009

// VepRunner is the general interface between VepMachine (Java) and sources (see Source)
// it knows both
// Functions:
// - handles communication with VepMachine (manages reception of OSC messages)
// - handles incoming events
// - instantiates and keeps tracks of sources (on/off)
// - defines where to read track files
// - creates defs for sources
// - manages physical speaker positions for VBAP panning

// NOTE: bad encapsulation between runner and source 
// in the first approach, the synthDef was sent by each source
// and each source should have managed buffer allocation for VBAP techniques
// but this would have lead to 325 compilations of the same def
// and to 325 identical buffers allocated

// Just to specify t_trig.
// no sense. To be restructured


VepRunner {
		// init stuff
	var <>def ; // the synthDef we pass, if we pass one
	var <>sources ; 	// an array containing all the registered sources
	var <>actives ; 	// a dict containing active sources (id: [x,y]). 
					// needed for updating when listener moves
		// Playback of the poeme files 
	var <>trackArr, <>playerBusArr, <>playerArr, <>outBusArr ;
		  // arrays of track buffers, busses, track players, output internal busses ;
	var <>folder, <>project ; // all the files in the folder/project will be loaded
	var <>numTracks ; // number of tracks. Used to autoset players and busses
	var <>numSources ;	// how many virtual sources 
		// VBAP stuff
	var <>directions ; // an array containing speaker locations in degrees
	var <>speakerPositions ; // the same but in degree, external interface  
	var <>dim ; // 2, ring, or 3, dome, se VBAP
	var <>vbapSpeakerArray ; // special array needed for vbap 
	var <>vbapBuffer ; // the buffer required by VBAP 
		// Controls: when set, are set to all sources
	var <spread ; // we can control the spread of all sources
	var <>maxDist ; // the max distance in the pavilion					
		// Responders
	var <>startResp, <>stopResp, <>trackerResp, <>speakerRespStart, <>speakerRespStop ;
	var 	<>setAudioResp ;
		// the listener data
	var <>listener ; // we store it and keep it updated 
	
	*new { arg folder, project, numSources, vol = 1, subVol = 0.25,
			def, 
			// azimuthal ring as default
			dim  = 2, // over the plane 
			// NOTE!!!! THIS IS OUTPUT BUS ORDER: 
			// that is: you take each out and connect to related physical speaker
			/*
						0
					1		7
				2				6
					3		5
						4	
						
			*/ 
			speakerPositions = [0, pi/4, pi/2, (3/4)*pi, pi, (3/4)*pi.neg, pi.neg/2, pi.neg/4],
			spread = 0,
			// used for normalize dist while passing to the source synth
			maxDist = 30 ;
		^super.new.initVepRunner(folder, project, numSources, def, dim, speakerPositions, spread, maxDist, true) 
	}


	// same but do not set load stuff. Useful for register a depenendancy before syncing
	*newPaused { arg folder, project, numSources, vol = 1, subVol = 0.25,
			def, 
			dim  = 2,  
			speakerPositions = [0, pi/4, pi/2, (3/4)*pi, pi, (3/4)*pi.neg, pi.neg/2, pi.neg/4],
			spread = 0,
			maxDist = 30 ;
		^super.new.initVepRunner(folder, project, numSources, def, dim, speakerPositions, spread, maxDist, false) 
	}

	

	initVepRunner { arg aFolder, aProject, aNumSources, aDef,  aDim, aSpeakerPositions, aSpread, aMaxDist, sync  ; 
		folder = aFolder ;
		project = aProject ;
		if (folder.isNil) {"\n\nWARNING! Folder does not exists!".postln} ;
		if (project.isNil) {"\n\nWARNING! You must still load a project!".postln} ;
		numSources = aNumSources ;
		dim = aDim ;
		// we put this here, because it's useless to do it for each source
		speakerPositions = aSpeakerPositions ;
		directions = speakerPositions.raddegree ; // VBAP wants degree 
		// here we allocate a set of consecutive internal busses 
		// where all the sources will write their n (8) chans
		// raddegree is not in main distro, but in mine
		// a special array required by VBAP
		vbapSpeakerArray = VBAPSpeakerArray.new(dim, directions).postln ;
		spread = aSpread ;
		maxDist = aMaxDist ;
		def = aDef ? 
// we asssume that dist is passed in a normalized range [0,1]
			// azimuth and elevation are in degree (this is VBAP)
			SynthDef(\vepPlayer, { arg in = 1, out, vol = 1, dist = 0, 
				// vol is a general vol 		
				trig = 1, att = 0.02, release = 0.5,
				// trig: a triggger that must be reset
				// att: attack duration, release: when trig is reset, release the env
				// following are inherited from VBAP ugen
				numChans = 8, azimuth = 0, elevation = 0, spread = 0, vbapBuf ;
				/*var cutFrequency = dist.linexp(0, 1, 18000, 20) ; 
				// ATTENTION: if cutfreq > 22000 LPF does not correctly work 
				var distancedSource = LPF.ar(In.ar(in), cutFrequency)*vol ;
				*/ 
				var distancedSource = In.ar(in)*vol*(1-(dist.squared)) ; // *dist would scale 
				distancedSource = distancedSource * EnvGen.kr
					(Env([0, 1, 1, 0], [att, 0.01, release], 'linear', 2), trig, doneAction:1) ;
				// we're hardcoding numChans, seems that it is the only way 
				Out.ar(out, VBAP.ar(8, distancedSource, vbapBuf, azimuth, elevation, spread)					);
			}) ;
		sources = [] ; // collects all the sources, ordering allows accessing 
		actives = Dictionary.new ; // it's a dict of indices, no duplicates, indices are singletons 
		listener = [0, 0, 0, 0, 0, 0] ; // default position 
		if (sync) { this.syncToServer }
		}
	
	
		
	syncToServer {
		// load all the stuff
		Server.local.waitForBoot({
		{
		outBusArr = Array.fill(speakerPositions.size, { Bus.audio }) ;
		// we need to load the VBAP array to a buffer 
		vbapBuffer = vbapSpeakerArray.loadToBuffer ; // so now we have loaded a buffer 
		def.send(Server.local) ; // send the def						// minimal track
		SynthDef(\trackPlayer, { arg trackBuf, out, t_trig = 1 ;
			Out.ar(out, PlayBuf.ar(1, trackBuf, trigger:t_trig))
		}).send(Server.local)  ;
		Server.local.sync ;  
		if (project.notNil)
			{ this.loadTracks ; // load the tracks into buffers
			"Tracks: \t\tOK".postln } ; 
		Server.local.sync ;
		this.setPlayers ;	// set up the player tracks
		"Tracks players: \t\tOK".postln ;
		Server.local.sync ;
		this.setResponders ;	// set up the responders
		"Responders: \t\tOK".postln ;
		Server.local.sync ;		
		this.addSources ;	// add all the source (that is: synths) 
		"Sources: \t\tOK".postln ;
		Server.local.sync ;		
		this.changed(this, [\initOk]) ;
		}.fork
		})
	}
	
	
	loadTracks { 
		var sFiles ; 
		"\n\nLoading this tracks: ".postln ;
		sFiles = SoundFile.collect(folder++"/"++project++"/*") ;
		if (sFiles.size == 0 ) {"\n\nWARNING! No tracks to load!\n\n".postln }{ 
		numTracks = sFiles.size ;
		// Here we set the buffers, one for each track
		sFiles.do{|f| 
			trackArr = trackArr.add
				( Buffer.read(Server.local, f.path.postln) )} ;}
	}
	
	
// SOURCES INIT

	// init of all the server stuff related to sources,
	// that is representations of virtual source (i.e. synth + infos)
	addSources {
		numSources.do{ this.addSource(def.name) } ;
		this.changed(this, [\addSources]) ;
	}	
// substantially privatem used by previous method
	addSource { arg defName ;
		// we just add a source with its def 
		sources = sources.add( 
			VepSource.new(defName)
				.set(\vbapBuf, vbapBuffer.bufnum).set(\out, outBusArr[0])
		) ;
		this.changed(this, [\addSource]) ;
	}
	
/*
// hmmm this can be dangerous, better use it only on init 
// better: don't use it
// (and why? Ok, just for completeness)
	removeSource { arg sourceIndex ;
		// we just add a source 
		sources = sources.removeAt(sourceIndex) ;		
		actives = actives.removeAt(sourceIndex) ; // if active, remove from actives 
		this.changed(this, [\removeSource]) ;	
	}
*/

// PLAYERS
	// set all the stuff related to the track playback
	setPlayers { 
	// three player synths, one for each track
	// so we can route them to vepSources
		{		
				
		// here we have four busses for internal routing (poeme -3- + concret -1-)
		playerBusArr = Array.fill(numTracks, { Bus.audio }) ;
		
		Server.local.sync ;
		
		// routing
		playerArr = Array.fill(numTracks, {|i| 
			Synth.newPaused(\trackPlayer, [\out, playerBusArr[i], \trackBuf, trackArr[i]], 
				addAction: \addToHead)
			}) ;
		}.fork
	}
	
	// players interface: poeme 
	startPlayer { arg playerIndex;
		if(playerArr[playerIndex].notNil)
			{playerArr[playerIndex].set(\t_trig, 1).run ;
		this.changed(this, [\startPlayer, playerIndex]) }
	}
	
	stopPlayer { arg playerIndex ;
		if(playerArr[playerIndex].notNil) 
			{ playerArr[playerIndex].run(false) ;
		this.changed(this, [\stopPlayer, playerIndex]) }
	}


// with reset we: 
/*
	0. clean up all (buffers, busses, players)
	1. load tracks 
	2. 
*/
	setAudio { arg projectName ; 
		{
		project = projectName ;
		// protection
		playerArr.do{|i|i.free} ;
		trackArr.do{|i|i.free} ;
		playerBusArr.do{|i|i.free} ;
		//
		Server.local.sync ;
		this.loadTracks ;
		Server.local.sync ;
		this. setPlayers ;
		// added for TO-VRMMP
		this.changed(this, [\setup_audio_player]) 
		}.fork
	}

// RESPONDERS
// here we handle OSC communication 	

	setResponders {
	// the responders responding to msg sent via OSC by the VepMachine
		startResp = OSCresponder(nil, '/start_audio_player',
			{ arg time, resp, msg; 
			var playerIndices = msg[1..] ;
			playerIndices.do{|pl| this.startPlayer(pl) } ;
			}).add ;
		stopResp = OSCresponder(nil, '/stop_audio_player',
			{ arg time, resp, msg; 
			var playerIndices = msg[1..] ;
			this.stopAllSources ; // no audio routed out from sources 
			playerIndices.do{|pl| this.stopPlayer(pl) } ;
			}).add ;
		speakerRespStart = OSCresponder(nil, '/start_audio_source', 
			{ arg time, resp, msg; 
			// here we define what's going on when a new speaker in VEP is activated
			// activation is an activation event
			// deactivation occurs after a stop event
			var source, x, y, z, track ;
			//"SPEAKER".postln ;
			// let's get some data from VEPMachine message
			source = msg[1].asString.split($_)[1].asInteger ;
			x = msg[2].asFloat ; 
			y = msg[3].asFloat ; 
			z = msg[4].asFloat ;
			track = msg[5].asInteger ; 
			this.startSource(source, x, y, z, track)
			}).add ;
		speakerRespStop = OSCresponder(nil, '/stop_audio_source', 
			{ arg time, resp, msg; 
			// when receveid a stopSource is called
			// let's get some data from VEPMachine message
			var source = msg[1].asString.split($_)[1].asInteger ;
			this.stopSource(source)
			}).add ;
		trackerResp = OSCresponder(nil, '/tracker', // listener updates
			{ arg time, resp, msg; 
			var elevation, azimuth, tilt, x, y, z ;
			//"TRACKER".postln ;
			 elevation = msg[1].asFloat ;
			 azimuth = msg[2].asFloat ; 
			 tilt = msg[3].asFloat ; 
			 x = msg[4].asFloat ;
			 y = msg[5].asFloat ;
			 z = msg[6].asFloat ; 
			 // we update the listener
			 listener = [elevation, azimuth, tilt, x, y, z] ;
			 // and recalculate only for actives
			 this.updateSources ;
			}).add ;
		setAudioResp = OSCresponder(nil, '/setup_audio_player', 
			{ arg time, resp, msg; 
				var name, path ;
				"SETUP".postln ;
				name = msg[1].asString ;
				this.setAudio(name) ;
			}).add ;

	}

	removeAllResponders {
		[startResp, stopResp, trackerResp, speakerRespStart, speakerRespStop].do{|r| r.remove}
	}


// SOURCES CONTROL
	
	// starting a source means turning it on and feeding it
	startSource { arg sourceIndex, x, y, z, track ;
		var azimuth, dist, in, source ;
		#azimuth, dist = this.thetaRho(x,y,z) ;
		azimuth = azimuth.raddegree ;
		dist = dist.linlin(0, maxDist, 0, 1) ; 
		in = playerBusArr[track.asInteger] ;
		source = sources[sourceIndex] ;
		// controlling the Source 
		source.state_(\on) ; // attribute 
		source.setAll(in, azimuth, dist); // set the synth 
		source.set(\trig, 1) ; 
		source.run ;	// make it run 
		// put it in the actives
		actives.put(sourceIndex, [x, y, z]) ;
		this.changed(this, [\startSource]) ;
	}
	
	// stopping a source means pausing it and updating its state 
	stopSource { arg sourceIndex ;
		var source = sources[sourceIndex] ;
		//"I'm STOPPING a  source".postln ;
		source.state_(\off) ; // attribute
		actives.removeAt(sourceIndex)  ; // out of actives
		source.set(\trig, 0) ;
		this.changed(this, [\stopSource]) ;
	}

	// stops all
	stopAllSources {
		actives.keys.do{|key| this.stopSource(key)} ;
		this.changed(this, [\stopAllSources]) ;
	}

	// test method: writes to each track bus a different pulsive event
	test { 
		{
		SynthDef(\test, { arg out, pulse = 2, freq = 440 ; Out.ar(out, 			LFPulse.ar(pulse)*SinOsc.ar(freq))} ).send(Server.local) ;
		Server.local.sync ;
		playerBusArr.do{ |bus, i| Synth(\test, [\out, bus, \pulse, i+2, \freq, 150*(i+1)],  
			addAction:\addToHead) } ;
		}.fork ;
	}

	// test method: writes to each track bus a sound file
	// doesn't work
	test2 { arg filePath ; 
		var buf ; 
		{
		buf = Buffer.read(Server.local, filePath) ;
		SynthDef(\test2, { arg out, bufnum ; Out.ar(out, 			PlayBuf.ar(1, bufnum))} ).send(Server.local) ;
		Server.local.sync ;
		playerBusArr.do{ |bus| Synth(\test2, [\out, bus, \bufnum, buf],  
			addAction:\addToHead) } ;
		}.fork ;
	}



	// executed on actives when listener moves
	updateSources {
		var x, y, z ;
		// only for actives 
		actives.keys.do{|sourceIndex|
			#x, y, z = actives[sourceIndex] ;
			this.updateSource(sourceIndex, x, y, z) } ;
		this.changed(this, [\updateSources]) ;
	}
	// the method before calls the following:	
	updateSource { arg sourceIndex, x, y, z ;
		var theta, rho, source ;
		#theta, rho = this.thetaRho(x,y,z) ;
		source = sources[sourceIndex] ;
		source.synth.set(\azimuth, theta.raddegree, \dist, rho.linlin(0, maxDist, 0, 1)) ; 
//		this.changed(this, [\updateSource]) ;
	 }


/*
	// event handling
	// event is the activation and deactivation
	// synthis turned on, set, and after dur paused again
	setEvent { arg sourceIndex, x, y, z, dur, track ;
		{
		this.startSource( sourceIndex, x, y, z, dur, track ) ;
		(dur-0.02).wait ; // event duration 
		this.stopSource(sourceIndex) ;
		}.fork
	}
*/

// GENERAL CONTROLS
// are sent to all actives sources 
		


	spread_ { arg newSpread ;
		spread = newSpread  ;
		sources.do{|source| source.set(\spread, spread)};
		// better do it after all the changes happened
		this.changed(this, [\spread]) ;
	}
		

// maths

// returns theta and rho
// by passing x, y, z of the source (that is: speaker)
// we use x and z, as y is for vertical dimension
// Reference System is OpenGL compliant:
// x-right, z-towards observer, y-up
// azimuth is the rotation about y-axis. 
// azimuth==0 -> looking towards z-neg, azimuth positive -> look left

// after Fabrizio Nunnari 
	thetaRho {
		// input
		arg x, y, z ; // the source coords
		var x1, y1, z1, azimuth; // the listener coords and looking angle on the plane
		
		// output
		var theta, rho ;

		var xT, zT ; // translated coordinates on the orizontal plane
		var xR, zR ; // translated and rotated coords on the orizontal plane

		// we grab the listener position, stored in var listener (array)
		#x1, y1, z1 = listener[3..] ;
		azimuth = listener[1] ;

		// Now align the source relative position
		// new pos related to listener as origin
		// that is: speaker - listener
		xT = x-x1 ; 
		zT = z-z1 ;
		zT = zT.neg ;
		// we use -zT to conform to the standard x,y cartesian plane orientation
		// we rotate the source relative position to include the listener azimuth
		// see: http://en.wikipedia.org/wiki/Cartesian_coordinate_system -> Rotation

		azimuth = azimuth.neg ;
		// to align the systems we need to invert the rotation angle
		xR = (xT * cos(azimuth)) - (zT * sin(azimuth)) ;
		zR = (xT * sin(azimuth)) + (zT * cos(azimuth)) ;
		
		// here we calculate tetha in range [-pi, pi] following:
		// http://it.wikipedia.org/wiki/Coordinate_polari
		// for easier compatibility with listener (in same range)
	
/* 
atan2(y,x)
For any real arguments x and y not both equal to zero, 
atan2(y, x) is the angle in radians between the positive x-axis of a plane 
and the point given by the coordinates (x, y) on it. 
The angle is positive for counter-clockwise angles (upper half-plane, y > 0), 
and negative for clockwise angles (lower half-plane, y < 0).
*/	 
		theta = atan2(zR, xR) ;
		// now tetha = 0 -> look towards x-pos axis
		// pi/2 -> look towards z-neg, -pi/2 look towards z-pos
		theta = theta - (pi/2) ; // aligned to camera 
		// now tetha = 0 -> look towards z-neg, 
		// pi/2 -> look towards x-neg, -pi/2 look towards x-pos

		// distance is easy, calculated in the 3D space 
		rho = ((x1-x).squared + (y1-y).squared + (z1-z).squared).sqrt ;
		// returns data as an array
		^[theta, rho]
	}

}

