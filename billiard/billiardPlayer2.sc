// WHAT WAS IT MEAN FOR? BK?
/*
BilliardPlayer2 {
	
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

Routine({
			SynthDef.new(\pulsePlayer, 
				{ arg freq, rate = 1, out = 0, dur = 2.5, t_trig = 1,
						amp = 1, cutFreq = 22050, room = 0.5, pan = 0 ; 
				Out.ar(out, 
					Pan2.ar(
					amp *
					FreeVerb.ar
					(
							LPF.ar(
								EnvGen.ar(Env.perc, gate: t_trig) * Pulse.ar(freq)
								 ,
							cutFreq)
							, room: room
						),
					pan
							)
						)}
			).send(server) ;

			2.wait ; // so tricky
		collisionSynth = Synth(\ratePlayer) ;
		// sequencing		
		order = [8, 9, 10, 6, 7, 5, 4, 3, 11, 2,1, 12, 13, 14, 15] ;
		order = order.addAll(Array.series(order.size, 16, 1)) ;
		order = order[..(size-1)] ; // easy way 
 		synthArr = [] ;
		order.size.do({ arg item, index ;
			synthArr = synthArr.add(
				Synth(\pulsePlayer).set(\freq, rrand(30, 100), \rate, rateArray[index], 
						\amp, ampArray[index],
						\cutFreq, cutArray[index],
						\room, roomArray[index],
						\pan, panArray[index],
						\t_trig, 1)
				) ;
			taskArr = taskArr.add(Task.new({
				inf.do({
					synthArr[index].set(\freq, (35+index+index.rand).midicps, 
						\rate, rateArray[index], 
						\amp, ampArray[index],
						\cutFreq, cutArray[index],
						\room, roomArray[index],
						\pan, panArray[index],
						\t_trig, 1) ;
						((index+1+propArray[index])*dur).wait ;
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
		velocity = velocity - 0.5 ;
		velocity.do({ arg item, index ;
		//	propArray[index] = mulArr[item.round] }) ;
			propArray[index] = item.round(0.25) }) ;
	}

/* you can probably delete me*/
	mapCollision {
		collisionSynth.set(\bufnum, rrand(31, 38), \t_trig, 1, \amp, 1.5) ;
	}
	
	update { arg theChanged, theChanger, more ;
		case { [\pan, \listener].includes(more[0]) }
				{ 	this.mapDist(generator.distArray) ;
					panArray = generator.panArray ; 
				}
			 { more[0] == \velocity }
				{ this.mapVel(generator.velocity) }
/*
			{ more[0] == \collision }
				{ this.mapCollision }
*/
	}

}


*/