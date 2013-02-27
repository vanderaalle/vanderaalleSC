/*
// Andrea Valle, 30/12/09

A class to encapsulate some useful funcs to
generate a spatializazion score for Concret PH
- it assumes to deliver a mono file
- over the "horizontal belt" (route I)

*/

XenConcGen {
	
	// this is the general method to generate a "line", i.e. an array
	// representing a velocity curve 
	// the algo:
	// 	- chooses a duration in the range
	// 	- chooses a vel in the range
	// 	- interpolates between previous and actual vel for the given duration
	// 	- choices follow Gaussian distribution (ˆ la Xenakis)
	*generateLine { arg totalDur = 150, timeOffset = 0, pointNum = 1500,
					// dur in sec, offset filled with silence, array size 
						minDur = 0.1, maxDur = 3, minVel = 0, maxVel = 10;  
					// duration and velocity ranges 
		var start, stop, dur, segmentDur, points, line ;
		var range ;
		// better ensure we're going wioth floats
		minVel = minVel.asFloat ;
		maxVel = maxVel.asFloat ;
		minDur = minDur.asFloat ;
		maxDur = maxDur.asFloat ;
		dur = timeOffset ;
		line = Array.fill(timeOffset*pointNum/totalDur, {0}) ;
		range = (maxVel-minVel)*0.5 ;
		start = range.sum3rand.linlin(range.neg, range, minVel, maxVel);
		while ( { dur <  totalDur }, 
			{ 	
				range = (maxVel-minVel)*0.5 ;
				stop = range.sum3rand.linlin(range.neg, range, minVel, maxVel);
				range = (maxDur-minDur)*0.5 ;
				segmentDur = range.sum3rand.linlin(range.neg, range, minDur, maxDur) ;
				points = segmentDur*pointNum/totalDur ;
				line = line++this.generateSegment(start, stop, points) ;
				start = stop ;
				dur = dur + segmentDur ;
			}
		) ;
	^line ;		
	}
	
	// given a start and stop value,  
	// generates an array of points points by interpolation 
	*generateSegment { arg start, stop, points ;
		^[start, stop].resamp1(points) ;
	}


	// this method convert an array of vel into 
	// and array of events by a Sample&Hold-like strategy	// At every step (i.e. speaker):
	//	- sample vel from line arr
	//	- calculate the time to wait to reach the next step
	// 	- sample line again moving along of realtive number of points
	//	- do it until the duration is completed 
	*createEvents { arg line, totalDur = 150, minTime = 0.1 ;
		var vel = line[0] ; //init: first vel is taken from the array
		var dur = 0 ; //init: total duration now is 0
		var events = [] ;
		var timeToWait ;
		var pointsPerSec = line.size/totalDur ;
		// main cycle
		while (
			{ dur < totalDur },
			{ 
				timeToWait = (1/vel).max(minTime) ;
				vel = line[(dur*pointsPerSec).asInteger] ;
				events = events.add([dur, timeToWait, vel].postln) ;
				dur = dur+timeToWait ;
			} 
		)
	^events		
	}	
	
	// utility to plot an events array
	*plotEvents { arg events, pointNum = 1500, minval = 0, maxval = 10 ;
		var arr = [] ;
		var totalDur = events.reverse[0][0] ;
		var  timeToWait, dur, vel ;
		var start, stop, points ;
		events[1..].do { arg event, i ;
			#dur, timeToWait, vel = event ;
			start = events[i][2] ;
			stop = vel ;
			points = events[i][1]*pointNum/totalDur ; 
			arr = arr++this.generateSegment(start, stop, points) ;
			} ;
		^[arr, arr.plot(minval:minval, maxval:maxval)] ;
	}
	
	// supatricka method  to convert events into VepMachine score commands
	// generating an array of two strings, for lights and speakers
	// BUG: generates single speaker activation, but in the pavilion
	// there are always blocks of 5 consecutive speakers activated
	*createInternalScore { arg events, numTrack = 0 ;
		var lights = "\n################ Highligh for the speakers #######################\n\n", speakers = "\n\n\n################ Audio Source info for remote audio distribution engine #######################\n\n"  ;
		var speakerNum = 0 ; 
		var start = 52 ; // start of route I
		var timeToWait, dur, vel ;
		var name ; 
		events.do { arg event ;
			#dur, timeToWait, vel = event ;
			dur = (dur * 1000).round ;
			timeToWait = (timeToWait*1000).round ;
			if ((speakerNum+start) >= 100) { name = (speakerNum+start).asString } 
					{ name = "0"++(speakerNum+start)} ;
		// # att  command     speaker dur tape
			lights = lights+(dur+"highlight_object ls_"++name+timeToWait+"red\n") ;
		// 0 send_start_audio_source ls_309 1
 		// 5400 send_stop_audio_source ls_309
			speakers = speakers+
				(dur +"send_start_audio_source ls_"++name + numTrack ++"\n") ;
			speakers = speakers+
				((dur+timeToWait) +"send_stop_audio_source ls_"++name++"\n") ;
			speakerNum = (speakerNum+1)%51 ; // route I is 51 speaker long
		}
	^ [lights, speakers];
	}
	
	// supatricka method  to convert events into VepMachine score commands
	// generating an array of two strings, for lights and speakers
	// difference: this version generate 5 speaker commands at once
	// BUG: it should not start/stop speakers wtill active  the next step
	// solved by Fabrizio by parsing the strings
	*createInternalScore2 { arg events, numTrack = 0 ;
		var lightsStr = "\n################ Highligh for the speakers #######################\n\n", speakersStr = "\n\n\n################ Audio Source info for remote audio distribution engine #######################\n\n"  ;
		var speakers = [0, 1, 2, 3, 4] ; 
		var offSpeakers ;
		var start = 52 ; var lenght = 51 ; // start and lenght of route I
		var timeToWait, dur, vel ;
		var name ; 
		events.do { arg event ;
			#dur, timeToWait, vel = event ;
			dur = (dur * 1000).trunc ;
			timeToWait = (timeToWait*1000).round ;
			offSpeakers = speakers % lenght + start ; // route I is 51 speaker long
			offSpeakers.do { arg speaker ;
				// # att  command     speaker dur tape
				if (speaker >= 100) { name = speaker.asString } 
					{ name = "0"++speaker} ;
				lightsStr = lightsStr++(dur+"highlight_object ls_"++name+timeToWait+"red\n") ;
				// 0 send_start_audio_source ls_309 1
 				// 5400 send_stop_audio_source ls_309
				speakersStr = speakersStr++
					(dur +"send_start_audio_source ls_"++name + numTrack ++"\n") ;
				speakersStr = speakersStr++
					((dur+timeToWait) +"send_stop_audio_source ls_"++name++"\n") ;
			} ;
			speakers = speakers+1 ;
		}
	^ [lightsStr, speakersStr];
	}
	
	
}