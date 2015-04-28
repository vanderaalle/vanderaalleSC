/*
29/08/11


- Includes all info about a graph
- Modelled after Graph
- Vertices are separate objects including scheduling capabilities

*/


APGraph {

	var <>graphDict ;	//
	var <>vNum, <>eNum ; // a counter for unique edges and vertices ID
	var <>on, <>off ; // two sets for activity bookkeeping
	var <>activationDur ; // time interval for edge process
	var <>task ; // the reactivation task
	var <>prob ; // reswitch prob
	var <>zeroTime ; // we start counting time here
	var <>events ; // we collect here the events

	*new { arg dur = 1, prob = 0.5 ;
		^super.new.initGraph (dur, prob)
	}

	initGraph { arg d, p ;
		activationDur = d; prob = p ;
		vNum = 0 ; eNum = 0 ;
		on = Set[] ; off = Set[] ;
		graphDict = IdentityDictionary.new ;
		events = [] ; zeroTime = 0 ;
		task = Task({
			inf.do{
			off.do{|v|
				// intersection between on and edges not void
				if ((on & graphDict[v][1]).size > 0)
					{ // throw dice and eventually activate
						if (prob.coin) { graphDict[v][0].activate}
					}
				} ;
			// wait
			activationDur.wait
				}
			}) ;
	}

	reset {this.initGraph (activationDur, prob) ; this.changed}

	/* Storing/retrieving in internal format */

	// for consistence use .gra extension
	write { arg path ;
		this.writeArchive(path)
	}

	// this means you can do:
	// a = Graph.read("/test.gra")
	*read { arg path ;
		^Object.readArchive(path) ;

	}
	/**/


/*	Basic usage */

	// add an empty vertex
	// so that it exists

	addVertex { arg dur = 1, prob = 0.5 ;
		var vID = vNum;
		graphDict.add(vID -> [APVertex.new(dur, prob, vID, this), Set[]]) ; // 2nd set for edges
		vNum = vNum + 1 ;
		this.changed;
		^vID; //return the vID created so that external application could reuse it to modify vertex info
	}


	// add an edge
	addEdge { arg start, end ;
		// adding the edge arr
		// no loops
		if (start != end){
			graphDict[start][1] = graphDict[start][1].add(end) ;
			graphDict[end][1] = graphDict[end][1].add(start) ;
			eNum = eNum + 1 ;
			this.changed
		}{"loops not allowed".postln}
	}

/* setting up process */


// here we start the vertex process
	activateVertex { arg v ; graphDict[v][0].activate }

	activateVertices { arg arr = [] ;
		graphDict.keys.asArray.do{|v| this.activateVertex(v) }
	}

	// this collects times --> zeroTime
	activateAll {
		zeroTime = thisThread.seconds ;
		vNum.do{|v| this.activateVertex(v) }
	}


	event { arg id ;
		var ev = [thisThread.seconds-zeroTime, id];
		("an event has been generated: "+id).postln ;
		events = events.add(ev);
		this.changed(this, ev) ;
		}

// now the edge process

	reactivate { arg flag = true;
		if (flag) {task.start}{task.stop} }




/*

/* Generation and processing methods */


	createRandom { arg nameList, eNum = 10, eMin = 1, eMax = 1 ;
		// a list of symbols, number of  edges connecting the list
		// max and min duration
		var start, end, dur, label ;
			nameList.do({ arg label ;
			this.addVertex(1200.rand, 800.rand, 0, label:label)
		}) ;

		eNum.do({ arg i ;
			start = nameList.size.rand+1 ;
			//label = nameList[start] ;
			end = nameList.size.rand+1 ;
			dur = rrand(eMin.asFloat, eMax.asFloat) ;
			this.addEdge(start, end, dur) ;
		}) ;
		this.changed ;
	}

	// there are two strategies:
	// 1. add in and out to each vertex if lacking
	// 2. cut a vertex without one in and one out
	makeCyclic {
	}

	// check me please
	createRandomCyclic { arg nameList = [], eNum = 10, eMin = 1, eMax = 1, noLoop = true ;
		// a list of symbols,
		// number of  edges connecting the list beyond I/O
		// max and min duration
		var start, end, dur, cleanNameList, label, arr ;

		this.createRandom(nameList, eNum, eMin, eMax) ;

		nameList.size.do({ arg vertex ;
			arr = Array.series(10)+1 ;
			if (noLoop, { arr.remove(vertex+1) }) ;
			start = arr.choose;
			end = arr.choose;
			dur = rrand(eMin.asFloat, eMax.asFloat) ;
			this.addEdge(start, vertex+1, dur) ;
			dur = rrand(eMin.asFloat, eMax.asFloat) ;
			this.addEdge(vertex+1, end, dur) ;
		}) ;
		this.changed ;
	}
*/

}

APScoreGui {

	var <>graph, <>dict, <>window, <>w, <>h;
	var <>windowSize ; // in sec, how much time to display

	*new { arg apGraph, w = 800, h = 400, windowSize = 60 ;
		^super.new.initScoreGui (apGraph, w, h, windowSize)
	}

	initScoreGui { arg ag, ww, hh, ws ;
		graph = ag ;
		w = ww ; h = hh ; windowSize = ws ;
		dict = IdentityDictionary.new ;
		graph.addDependant(this) ;

		window = Window( "Alexander Process Score Graph", Rect( 100, 200, w, h ), resizable: false ).front;

	window.drawFunc = {
		var x, y, r, yl ;
		Pen.fillColor_(Color.black) ;
		Pen.strokeColor_(Color.black) ;
		graph.vNum.do{|i|
			yl = i.linlin(0, graph.vNum, 50, h-50) ;
			Pen.line(0 @ yl, w @ yl)} ;
		Pen.stroke ;
		graph.events.do{ |ev|
			x = ev[0].linlin(0, windowSize, 50, w-50) ;
			y = ev[1].linlin(0, graph.vNum, 50, h-50) ;
			r = Rect(x-5,y-5, 10, 10) ;
			Pen.fillOval(r)
			}
		}
	}

	update { arg theChanged, theChanger, more;
		{window.refresh}.defer ;
	}

}

APGraphGui {

	var <>graph, <>dict, <>window, <>w, <>h ;

	*new { arg apGraph, w = 400, h = 400 ;
		^super.new.initGraphGui (apGraph, w, h)
	}

	initGraphGui { arg ag, ww, hh ;
		graph = ag ;
		w = ww ; h = hh ;
		dict = IdentityDictionary.new ;
		graph.graphDict.keys.do{|k| dict[k] = graph.graphDict[k].add([10+(w-20).rand,10+(h-20).rand]) } ;
		graph.addDependant(this) ;

		window = Window( "Alexander Process Graph", Rect( 100, 200, w, h ), resizable: false ).front;
	window.drawFunc = {
	var x, y, xe, ye, r ;
	Pen.font = Font( "SansSerif", 8 );
	graph.graphDict.keys.do {|k|
		x = dict[k][2][0] ;
		y = dict[k][2][1] ;
		r = Rect(x-5,y-5, 10, 10) ;
		if (dict[k][0].state == 0) {Pen.fillColor_(Color.black)}{Pen.fillColor_(Color.red)} ;
		Pen.fillOval(r) ;
		Pen.fillColor_(Color.hsv(0.1, 1 ,1)) ;
		Pen.stringAtPoint(k.asString, x@y) ;
		Pen.strokeColor_(Color.black) ;
		dict[k][1].do{|end|
			xe = dict[end][2][0] ;
			ye = dict[end][2][1] ;
			Pen.line(x@y, xe@ye) ;
			Pen.stroke ;
				}
			}
		}

	}

	update { arg theChanged, theChanger, more;
		{window.refresh }.defer ;
	}


}

/*
g = APGraph.new(2,1) ; // always reactivate

g.addVertex(1,0.5) // id 0 only one shot
g.addVertex(1,0) // id 1 always on

g.addEdge(0,1) // connect them

p = APGraphGui(g)

g.activateVertices([0,1]) // activate id 0 and 1

g.reactivate


g.prob_(0) // don't reactivate
g.prob_(1) // reactivate

g = APGraph.new(2,1) ; // always reactivate
~num = 8 ;
~num.do{g.addVertex(1,0.15) } ; ~num.do{g.addEdge(~num.rand, ~num.rand) }
p = APGraphGui(g, 700, 700)
c = APScoreGui(g, 1000, 500)

g.activateAll; g.reactivate
*/


/*

g = APGraph.new(0.2,1) ; // always reactivate
~num = 25 ;
~num.do{g.addVertex(rrand(0.5, 2).round(0.1),0.5) } ; ~num.do{g.addEdge(~num.rand, ~num.rand) }
p = APGraphGui(g, 700, 700)
c = APScoreGui(g, 1000, 500)
g.reset
g.activateAll; g.reactivate

*/
