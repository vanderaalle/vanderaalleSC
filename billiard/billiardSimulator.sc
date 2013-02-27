/*

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

BilliardSimulator {

	
	// ratio: 2:1
	var <>size ;
	var <>state ;
	var <>velocity ;
	var <>netAddr ;
	var <listener ; // index+1 of the listener
	var <>geo ;

	
	*new { arg size = 30 ;
		^super.new.initBilliardSimulator(size)
	}
	
	*newFromFile { arg path = "/musica/tabulaExCambio/osc_log.txt" ;
			^super.new.initBilliardSimulator2(path) ;
	}
	
	initBilliardSimulator { arg aSize ;
		size = aSize ;
		// set up a random initial state
		netAddr = NetAddr("127.0.0.1", 57120) ;
		state = Array.fill(size, { [2.0.rand, 1.0.rand] }) ;
		velocity = Array.fill(size, { 4.0.rand }) ;
		this.updateState ;
		this.updateVelocity ;
		listener = 0 ;
		netAddr.sendMsg("/listener", listener) ;
		this.changed(this, [\listener]) ;
	}
	

	listener_{ arg index ;
		listener = index ;
	netAddr.sendMsg("/listener", listener) ;
	this.changed(this, [\listener]) ;
	}


	updateState { arg incr = 0.1 ;
		var msg ;
		var newState = [] ;
		var pos ;
		state.do({ arg item ;
			// brownian not linear
			pos = [
					rrand(item[0]-incr, item[0]+incr).fold(0,2), 
					rrand(item[1]-incr, item[1]+incr).fold(0,1)
				] ;
			newState = newState.add(pos) ;
		});
		state = newState ;
		msg = ['/state']++state.flat ;
		netAddr.sendMsg(*msg) ;
		this.changed(this, [\state]) ;
	}

	updateVelocity {
		var msg ;
		// let's say speeds can be in a 0-1 range 
		velocity = Array.fill(size, { 1.0.rand }) ;
		msg = ['/velocity']++velocity ;
		netAddr.sendMsg(*msg) ;
		this.changed(this, [\velocity]) ;

	}

	play { arg rate = 5 ;
		var time = 1/rate ;
		Routine({ 
			inf.do({
				this.updateState ;	
				this.updateVelocity ;
				time.wait ;
			})
		}).play(SystemClock)
	
	}


	initBilliardSimulator2 { arg aPath ;
		var f = File(aPath, "r") ;
		var string = f.readAllString ;
		var arr = string.split($\n) ;
		var time, msg, data, toWait ;
		f.close ;
		listener = 0 ;
		netAddr = NetAddr("127.0.0.1", 57120) ;
		netAddr.sendMsg("/listener", listener) ;
		this.changed(this, [\listener]) ;
		Routine({
			2.wait ;
			arr.do({ arg item, index ;
				item = item.split($ ) ;
				time = item[0].asFloat ;
				msg = item [1] ;
				if (item[2..].size == 60 )
					// state
					{
					msg = ['/state']++item[2..].asFloat ;
					netAddr.sendMsg(*msg) ;
					this.changed(this, [\state]) ;
					}
					// velocity
					{
					msg = ['/velocity']++item[2..].asFloat ;
					netAddr.sendMsg(*msg) ;
					this.changed(this, [\velocity]) ;
					} ; 	
				toWait = (arr[index+1].split($ )[0].asFloat-time)/1000 ;
				toWait.wait ;
			})
		
		}).play(SystemClock) ;
	}

}