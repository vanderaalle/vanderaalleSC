GeoGraphSynth  {

var <>vertexId, <>vertexXY, <>vertexDuration, <>edgeDuration, <>radius, <>position, 	<>synthDefs, <>synth, <>vertexX, <>vertexY;


	*new { arg vertexId, vertexX, vertexY, vertexDuration=0, 
		edgeDuration=0, radius=0, position=[0,0], defName = "SimpleSine"; // a default synthDef
		^super.new(defName=defName).init(vertexId, vertexX, vertexY, vertexDuration,
				edgeDuration, radius, position);
		
	}

	init { arg vertexId, vertexXY, vertexDuration,
				edgeDuration, radius, position;
		
		this.vertexId = 		vertexId;
		//this.vertexX = 		vertexXY[0]; 
		//this.vertexY = 		vertexXY[1];
		this.vertexX = 		vertexX; 
		this.vertexY = 		vertexY;
		this.vertexDuration =	vertexDuration; 
		this.edgeDuration = 	edgeDuration; 
		this.radius =			radius;
		this.position = 		position;
}


	status {
		^format ("
		vertexId:			%
		vertexX:			%
		vertexY:        	%
		vertexDuration: 	%
		edgeDuration: 	%
		radius: 			%
		position: 		%",
		this.vertexId, this.vertexX, this.vertexY, this.vertexDuration, this.edgeDuration,
		this.radius, this.position).postln;
	}

	// it assignes you a synth
	create { arg defName;
			this.synth = Synth.new(defName);		
	}

	play {
		this.synth.play;
	}
	
	
	free { 
		this.synth.free;
	}
	
	set { arg controlName, value;
		this.synth.set(controlName, value);	
	}

///////////////////// already in GeoGrapher/////////////////////
	calculateDist { arg vertexX, vertexY, positionX, positionY;
		var distance; 
		//	dist = sqrt(((vertexX-positionX)**2)+((vertexY-positionY)**2));
		distance = ((vertexX-positionX).pow(2) + (vertexY-positionY).pow(2)).sqrt;
		^distance.postln;
	}

	
	calculatePan1 { arg vXY, position;
		var pan1;
		^pan1	
	}


	calculatePan2 { arg vXY, position;
		var pan2;
		^pan2	
	}
/////////////////////				/////////////////////
	
	
}