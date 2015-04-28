/*
29/08/11

Represents a vertex in an Alexander Process Graph

*/

APVertex {
	
	var <>prob ; // disactivation probability
	// prob = 1 --> off after first event ; prob = 0 --> always on
	var <>dur ;	// event generation dur
	var <>state ; 	// the state: 0 is off, and 1 is on
	var <>task ; 	// the event gen task
	var <>id ; // a numerical ID
	var <>graph ; // the relative graph instance, we could use dependancy instead
	
	*new { arg dur = 1, prob = 0.5, id = 0, graph ; 
		^super.new.initVertex(dur, prob, id, graph) 	
	}

	initVertex { arg f, p, i, g ;
		dur = f ; prob = p ; id = i; graph = g ; // setting vars
		state = 0 ; // we start with vertex off
		graph.off.add(id) ;
		graph.on.remove(id) ;
		task = Task({
			inf.do { 
			this.generateEvent ;
			dur.wait }
			})
	}

	activate { 
		if (state == 0) {
			state = 1 ;
			graph.on.add(id) ;
			graph.off.remove(id) ;
			task.start ;
		}
	}
	
	generateEvent {
		// do something: the event
		// then throw dice 
		if (prob.coin) { 	
			task.stop; 
			state = 0 ; 
			graph.off.add(id) ;
			graph.on.remove(id) } {		
		graph.event(id)} ; // we need to add timestamp to ID
	} 
	
}