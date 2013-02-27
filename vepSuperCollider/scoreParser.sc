/*
(
// this create a viable data structure representing pavilion's loudspeaker
r = RouteAndClusterParser("/paperiProgetti/vep/vep8chans/scoreSpeakersData/vepSpeakers.txt")
r.archive("/paperiProgetti/vep/vep8chans/scoreSpeakersData/scSpeakerConfig.txt")
)

(
// here we create the score for VEP Machine
~config = "/paperiProgetti/vep/vep8chans/scoreSpeakersData/scSpeakerConfig.txt" ;
p = ScoreParser("/paperiProgetti/vep/vep8chans/scoreSpeakersData/scoreTest.txt", ~config) 
)
*/

ScoreParser {
	
	var <>speakerDict ;
	var <>lines ;
	var <>scoreFilePath ;
	var <>internalScoreFile ;
	var <>internalScoreString ;
	var <>lightString ;
	var <>colors ;

	*new { arg scoreFilePath, speakerConfigFilePath ;
		^super.new.initScoreParser(scoreFilePath, speakerConfigFilePath)
	}


	initScoreParser { arg aScoreFilePath, speakerConfigFilePath ;
		var file, scoreString ; 
		scoreFilePath = aScoreFilePath ;
		speakerDict = Object.readArchive(speakerConfigFilePath) ;
		file = File(scoreFilePath, "r") ;
		scoreString = file.readAllString ;
		file.close ;
		colors = ["red", "green", "blue"] ;
		lines = this.splitLines(scoreString) ;
	}

	splitLines { arg scoreString ;
		var arrLines = [] ;
		var txtLine = scoreString
			.split($\n)
			.reject{|i| i.size == 0 }
			.reject{|i| i[0]==$; }
			.collect{|i| i.replace("\t", " ") } ;
		txtLine.do{|line|		
			line = line.split($ ).reject{|i| i.size==0} ;
			line = line.collect{|it, id| 
				case 
					{id == 0} {it.asFloat}
					{id == 1} {it.asInteger}
					{id == 2} {it.asString}
					{id == 3} {it.asFloat}
					{id == 4} {it.asFloat}
					{id == 5} {it.asSymbol}
					{id == 6} {it.asFloat}
					} ;
			arrLines = arrLines.add(line)
		};
		^arrLines
	}
	
	createScore{ arg internalScorePath ;
		internalScoreString = "\n################ Audio Source info for remote audio distribution engine #######################\n\n\n" ;
		lightString = "################ Highligh for the speakers #######################


# 1 = red
# 2_ = green
# 3_ = blue
# att  command     speaker dur tape\n" ;
		lines.do{ |line|
			line.postln ;
			if (line.size == 6 ) 
				{ this.createCluster(line) }
				{ this.createRoute(line) }
		} ;
		internalScorePath ? internalScorePath = scoreFilePath++"Internal" ;
		internalScoreFile = File(internalScorePath, "w") ;
		internalScoreFile.write(lightString+"\n\n################# end of the section for highlight instructions\n\n") ;
		internalScoreFile.write(internalScoreString+"\n\n\n################# end of the section for audio sources info ################\n") ;
		internalScoreFile.close ;
	}
	
	createCluster { arg line ;
		var speakers ;
		var start, name, tape, end, dur, where ; 
		#start, name, tape, end, dur, where = line ;
		start = start*1000 ; end = end*1000 ; dur = dur *1000 ;
		tape = tape.asInteger ;
		speakers = speakerDict[where] ;
		speakers.do{ |sp|
			internalScoreString = 
				internalScoreString + (start+"send_start_audio_source"+sp[0]+tape++"\n" ) ;
			internalScoreString = 
				internalScoreString + (end+"send_stop_audio_source"+sp[0]++"\n" ) ;
			lightString = lightString + (start+"highlight_object"+sp[0]+dur+colors[tape-1]++"\n")
		}	
	}

	createRoute { arg line ; 
		var speakers ;
		var start, name, tape, end, dur, where, rate ; 
		var howMany, residualTime, sp ;
		var stepStart, stepEnd ;
		#start, name, tape, end, dur, where, rate = line ;
		start = start*1000 ; end = end*1000 ; dur = dur *1000; rate = rate*1000 ;
		tape = tape.asInteger ;
		speakers = speakerDict[where] ;
		howMany = (dur/rate).asInteger ; // how many loudspeakers involved 
		residualTime = dur-(howMany*rate) ;
		howMany.do{ |n|
			sp = speakers[n] ;
			stepStart = start+(n*rate) ;
			stepEnd =  start+((n+1)*rate) ;
			internalScoreString = 
				internalScoreString + (stepStart+"send_start_audio_source"+sp[0]+tape++"\n" ) ;
			internalScoreString = 
				internalScoreString + (stepEnd+"send_stop_audio_source"+sp[0]++"\n" ) ;
			lightString = 
				lightString + (stepStart+"highlight_object"+sp[0]+rate+colors[tape-1]++"\n")
		} ;
		stepStart = start+(howMany.size*rate) ;
		stepEnd =  start+(howMany.size*rate+residualTime) ;
		internalScoreString =
			internalScoreString + (stepStart+"send_start_audio_source"+sp[0]+tape++"\n" ) ;
		internalScoreString = 
			internalScoreString + (stepEnd+"send_stop_audio_source"+sp[0]++"\n" ) ;
		lightString = lightString + (stepStart+"highlight_object"+sp[0]+rate+colors[tape-1]++"\n")
	}

}