// Andrea Valle, started on Nov 2009

/*
// From Tabula
General mechanism:

		_____			____
	JAVA, 	|			|	SC
	SC, etc	|			|			dependancy
		 	|	via OSC	|    		----------> player
 simulator	|------------>| controller < 
			|			|			----------> GUI		____	|			|___			dependancy
					
The simulator generates states and handles listeners and 
communicates its internal state via OSC, just like the Java app

*/


VepSimulator {

	
	// ratio: 2:1
	var <>size ;
	var <>state ;
	var <>velocity ;
	var <>netAddr ;
	var <listener ; 
	var <>speakers ;
	var <>elevation, <>azimuth, <>tilt, <>xL, <>yL, <>zL ; //listener
	
	var <>available ;
	
	*new { arg size = 30 ;
		^super.new.initVepSimulator(size)
	}
	
	
	initVepSimulator { arg aSize ;
		size = aSize ;
		// set up a random initial state
		netAddr = NetAddr("127.0.0.1", 57120) ;
		#elevation, azimuth, tilt, xL, yL, zL = [0, pi, 0, 15, 15, 15] ;
		this.generateSpeakers ;
		//this.updateTracker ;
	}
	

	generateSpeakers { 
		speakers = [] ;
		size.do{ |i|
			speakers = speakers.add([i, 30.0.rand, 30.0.rand, 30.0.rand])
		} ;
		available = Array.series(size) ;
	
	}
	
	updateSpeaker { arg dur = 5 ; 
		var msg ;
		var x, y, z ;
		var track = [0, 1, 2, 3].choose ;
		var speakerInd = available.choose ;
		var speaker = speakers[speakerInd] ;
		var name = "ls_"++(speaker[0]).asString ;
		available.remove(speakerInd) ;
		#x, y, z = speaker[1..] ;
		{
		msg = ['/start_audio_source']++[name, x, y, z, track] ;
		netAddr.sendMsg(*msg) ;
		dur.wait ;
		msg = ['/stop_audio_source']++[name] ;
		netAddr.sendMsg(*msg) ;
		available = available.add(speakerInd) ;
		}.fork
	}


	updateTracker { arg incr = 5, incr2 = pi/8 ; // the guy moves randomly but with brown motion
		var msg ;
		elevation = (elevation + incr2.rand).wrap(-pi, pi) ;
		azimuth = (azimuth + incr2.rand).wrap(-pi, pi) ;
		tilt = (tilt + incr2.rand).wrap(0, 2pi) ;
		xL = (xL+incr.rand).wrap(0, 30) ;
		yL = (yL+incr.rand).wrap(0, 30) ;
		zL = (zL+incr.rand).wrap(0, 30) ;
		msg = ['/tracker']++[elevation, azimuth, tilt, xL, yL, zL] ;
		netAddr.sendMsg(*msg) ;

	}


// general random firing up
	play { arg rate = 5 ;
		var time = 1/rate ;
		Routine({ 
			inf.do({
				this.updateSpeaker ;	
				this.updateTracker ;
				time.wait ;
			})
		}).play(SystemClock)
	
	}

	sendStartAudioPlayer { arg players = [0,1,2] ;
		var msg  = ['/start_audio_player']++players ;
		msg.postln ;
		 netAddr.sendMsg(*msg) 
		}

	sendSetAudio { arg name ;
		var msg  = ['/setup_audio_player']++[name] ;
		msg.postln ;
		 netAddr.sendMsg(*msg) 
		}

// to send specific values and see what happens
	sendListener { arg elevation, azimuth, tilt, x, y, z ;
		var msg  = ['/tracker']++[elevation, azimuth, tilt, x, y, z] ;
		 netAddr.sendMsg(*msg) 
	}

	sendSpeaker { arg index, x, y, z, dur, track ;		
		var msg ;
		var name = "ls_"++(index).asString ;
		{
		msg = ['/start_audio_source']++[name, x, y, z, track] ;
		netAddr.sendMsg(*msg) ;
		dur.wait ;
		msg = ['/stop_audio_source']++[name] ;
		netAddr.sendMsg(*msg) ;
		}.fork
	}
	
}