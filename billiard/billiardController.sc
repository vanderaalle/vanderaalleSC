BilliardController {

	var <>stateResponder ;
	var <>listenerResponder ;
	var <>velocityResponder ;	
	
	var <>state, <>velocity ;
	var <>distArray ; // the array of distances listener-others
	var <>panArray ;
	var <listener ; // index+1 of the listener
	var <>listenerOldPosition, <>listenerActualPosition ;
//	var <>geo ;
//	var <>bGui ;
	


	*new { 
		^super.new.initBilliardController
	}

	initBilliardController {
		listenerOldPosition = [2.0.rand, 1.0.rand] ; // rand init
		listenerActualPosition = [2.0.rand, 1.0.rand] ; // just to be clean
		stateResponder = OSCresponder(nil, '/state', 	
			{ arg time, resp, msg; 
			"state".postln ;
				state = msg[1..].clump(2) ;
				state.postln;
				this.changed(this, [\state]) ;
				//this.checkCollisions ;
				this.updatePans ;
			}).add ;
		velocityResponder = OSCresponder(nil, '/velocity', 
			{ arg time, resp, msg; 
				"vel".postln ;
				velocity = msg[1..] ;
				velocity.postln ;
				this.changed(this, [\velocity]) ;
			}).add ;
		listenerResponder = OSCresponder(nil, '/listener', 
			{ arg time, resp, msg; 
				listener = msg[1].asInteger ;
				listener.postln ;
				listener.postln ;
				if (listener != 0)
					{ 	listenerOldPosition = listenerActualPosition ;
						listenerActualPosition = state[listener-1] ; 
					} ;
			 this.changed(this, [\listener]) ;
			}).add ;
	}

// cleans up the responders (otherwise they keep on working)
	removeResponders {
		this.stateResponder.remove ;
		this.velocityResponder.remove ;
		this.listenerResponder.remove ;
	}

	updatePans {
		var x, y, pan, dist ;
		panArray = [] ;
		distArray = [] ;
		if (listener != 0)
			{
		state.do({ arg item, index ;
			# dist, pan = GeoGrapher(item, listenerActualPosition, listenerOldPosition).run ; 
			pan = pan[0] ;
			distArray = distArray.add(dist) ; 
			panArray = panArray.add(pan) ;	
			}) 
			}
			{ 
			distArray = Array.fill(15, 0) ;
			panArray = Array.fill(15, 0) ;
			} ;
		"dist and pan:".postln;
		distArray.postln ; panArray.postln ;
		this.changed(this, [\pan]) ;
	}

/*	
// useless: more, it should be placed in simulator

	checkCollisions { arg thresh = 0.1 ;
		distArray.do({ arg item, index ;
			if ((listener != 0) && (item < thresh) && (index != (listener-1)))
				{ 
				this.changed(this, [\collision]) ;
				 } 
		
		}) ;
	}
*/


}