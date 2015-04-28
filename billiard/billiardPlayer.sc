
BilliardPlayer {
	
	var <>generator, <>size ;
	var <>dur ;
	var <>folderPath ;
	var <>server ;
	var <>temp ;
	var <>diagonal ;

	var <>ampArray ;
	var <>rateArray ;
	var <>cutArray ;
	var <>roomArray ;
	var <>panArray ;
	var <>propArray ;
	var <>synthArr ;
	var <>taskArr ;
	var <>collisionSynth ;

	*new { arg generator, dur = 0.1, folderPath = "/musica/tabulaExCambio/" ;
		^super.new.initBilliard(generator, dur, folderPath) ;
	}

	initBilliard { arg aGenerator, aDur, aFolderPath ;
		var order ;
// JUST FOR LOG
		var time = thisThread.seconds ;
//
		folderPath = aFolderPath ;
		dur = aDur ;
		diagonal = sqrt(5) ;
		temp = TempoClock(1) ;
		generator = aGenerator ;	
		size = generator.state.size ;
		generator.addDependant(this) ;
		
		ampArray = Array.fill(size, 1) ;
		rateArray = Array.fill(size, 1) ;
		cutArray = Array.fill(size, 22050) ;
		roomArray = Array.fill(size, 0.5) ;
		panArray = Array.fill(size, 0.0) ;
		propArray = Array.fill(size, 0) ;
						
		server = Server.local ;
		server.boot.doWhenBooted({
			var i = 1 ;
			SampleListGenerator.fromPath (folderPath++"japanese")
				.do({ arg name ;
				Buffer.read(server, folderPath++"japanese/"++name++".aif", bufnum: i) ;
				i = i+1
				}) ;

			SampleListGenerator.fromPath (folderPath++"other")
				.do({ arg name ;
				Buffer.read(server, folderPath++"other/"++name++".wav", bufnum: i) ;
				i = i+1
				}) ;
			
			
			SampleListGenerator.fromPath (folderPath++"pitch")
				.do({ arg name ;
				Buffer.read(server, folderPath++"pitch/"++name++".wav", bufnum: i) ;
				i = i+1
				}) ;
			
// collision
			SampleListGenerator.fromPath (folderPath++"reyong")
				.do({ arg name ;
				Buffer.read(server,folderPath++"reyong/"++name++".wav", bufnum: i) ;
				i = i+1
				}) ;

Routine({
			SynthDef.new(\ratePlayer, 
				{ arg bufnum, rate = 1, out = 0, dur = 2.5, t_trig = 1,
						amp = 1, cutFreq = 22050, room = 0.5, pan = 0 ; 
				Out.ar(out, 
					Pan2.ar(
					amp *
					FreeVerb.ar
					(
							LPF.ar(
								PlayBuf.ar(1, bufnum, rate: BufRateScale.kr(bufnum) * rate,
								 loop: 0, trigger: t_trig)
								 ,
							cutFreq)
							, room: room
						),
					pan
							)
						)}
			).send(server) ;

			2.wait ; // so tricky
		collisionSynth = Synth(\ratePlayer) ; // useless?
		// sequencing		
		order = [8, 9, 10, 6, 7, 5, 4, 3, 11, 2, 1, 12, 13, 14, 15] ;
		order = order.addAll(Array.series(order.size, 16, 1)) ;
		order = order[..(size-1)] ; // easy way 
 		synthArr = [] ;
		order.size.do({ arg index ;
			synthArr = synthArr.add(
				Synth(\ratePlayer).set(\bufnum, order[index], \rate, rateArray[index], 
						\amp, ampArray[index],
						\cutFreq, cutArray[index],
						\room, roomArray[index],
						\pan, panArray[index],
						\t_trig, 1)
				) ;
			taskArr = taskArr.add(Task.new({
				inf.do({
					synthArr[index].set(\bufnum, order[index], 
						\rate, rateArray[index], 
						\amp, ampArray[index],
						\cutFreq, cutArray[index],
						\room, roomArray[index],
						\pan, panArray[index],
						\t_trig, 1) ;
								//______________________________
		// dirty for log, REMEMBER TO DELETE ME
		if (ampArray[index] != 0)
			{ File("eventLog.txt","a")
				.write([thisThread.seconds-time, index].asString++",\n")
			 	.close };
		//______________________________

//						((index+1+propArray[index])*dur).wait ;
						((index+1*propArray[index])*dur).wait ;
					})
			}, temp).play) ; 
		
		}) ;

	}).play(SystemClock) ;
})	
	}

	play {
		taskArr.do({ arg task ;
			task.resume ;
		})	
	}
	
	pause {
		taskArr.do({ arg task ;
			task.pause ;
		})
	}

	mapDist { arg distArray, thresh = 0.95 ; 
		var norm ;
		var listener = generator.listener ;
		distArray.do({ arg item, index ;
				norm = item/diagonal ;
			// amp
			 	ampArray[index] = 
			 	case 
			 	{ listener == 0 }
			 		{ 1 }
			 	{ norm == 0 }
			 		{ 1.5 }	
			 	{ norm <= thresh }
					{ 1-norm }
			 	{ norm > thresh }					
					{ 0 } ;
			// rate
			 	rateArray[index] =
			 	if (listener != 0 )	 
					{ (1-norm)*0.7+0.5 }
					{ 1 } ;
			// cut
				cutArray[index] = 
					case 
						{ listener != 0} 
					 		{ norm.linexp(0, 1, 22000, 20) } 
						{ listener == 0 }
							{ 22000 } ;
			// room
				roomArray[index] = 
					norm ;

		})
	
	}


	mapVel { arg velocity ;
		// assuming vel in normalized range (0,1)
		// var mulArr = [-0.5, -0.25, 0, 0.25, 0.5] ;
		//velocity = velocity - 0.5 ;
//		velocity = (1-velocity) * 3 + 0.125 ;
		velocity = velocity.linexp(0, 1, 5, 0.125) ;
		velocity.do({ arg item, index ;
		//	propArray[index] = mulArr[item.round] }) ;
			propArray[index] = item.round(0.125) }) ;
	}


	
	update { arg theChanged, theChanger, more ;
		case { [\pan, \listener].includes(more[0]) }
				{ 	this.mapDist(generator.distArray) ;
					panArray = generator.panArray ; 
				}
			 { more[0] == \velocity }
				{ this.mapVel(generator.velocity) }

	}


}


