
NimChimpsky : AGNotator {
		
		// SPECIALIZED FOR CONTOIDS
		// with ID better use symbols (so boring)
		
		// we use a graph-grammar instead of replacement rules
		// because the three layers should be different
		// so, non-determinism is required and
		// non-deterministic replacement rules are not interesting

		
		// a graph determining sequences of phones
		var <>nimGraph, <>maxConn, <>total, <>maxInConn ;
		var <>roots, <>leaves, <>loopingRoots ;
		
		
		
		*new { arg aNimGraph; ^super.new.initNim(aNimGraph) }
		
		
		
		initNim { arg aNimGraph ;
		// the usual graph. Format of phonation:
		// phone*register*continuationFlag* 
		// *: separator for easy string splitting (asString.split)
		// @: start of a multigram repr of IPA char (for Unicode replacement)
			nimGraph = aNimGraph ;
	
		maxConn = 0 ;
		nimGraph.do({arg value ;  
			if ( value.size > maxConn, { maxConn = value.size })
			});
		total = []  ;
		nimGraph.do({ arg value ;
			total = total.add( value ) ;
		}) ;
		total = total.flat ;
		maxInConn = this.maxInConnections ;
		# roots, leaves = this.rootsAndLeaves ;
		loopingRoots = this.calculateLoopingRoots ;
		}
	
	// an utterance is a path on the graph-grammar
	// i.e. a sequence of phones
	
	// note that utterance is an array of symbols
	// start: the phonation starting the utterance
	// maxLength: max utterance length 
	generateUtterance { arg startingPhone, maxLength ;
		var length = 1 ; 
		var start = startingPhone, next, nextAll ;
		var utterance = [ start ];
		while { length < maxLength } {
			nextAll = nimGraph[start] ;
			if ( nextAll != nil, { 	
					next = nextAll.choose ;
					utterance = utterance.add( next ) ; 
					start = next ;
					length = length + 1 ;
					}, 
					{ (start.asString + " --> terminal").postln ;length = inf } // terminal? Then close cycle
					 ) ;
								}
		^utterance
	}
		

	

	// the problem is that we have to sync the three layers
	// on the first one
	// so here we generate a structure
	createStartArray { arg inArray, referenceArr ;
		var newArr = [] ;
		var itemArr, indexArr ;
		var n = 0 ;
		var current = referenceArr[0] ;
		# itemArr, indexArr = inArray.clump(2).flop ;
		itemArr.do({ arg item, index ;
			var length = indexArr[index].postln ; 
			var infArr ;
			var startingPhone, startArr = [] ;
			if ( item == -inf,
						{ 
						infArr = Array.fill(length, current) ;
						newArr = newArr.addAll(infArr) ;
						//totalLength = totalLength + length ; 
						} 
				) ;
			if ( item == 60,  
						{ 
						startingPhone = referenceArr[n] ;
						startArr = Array.fill(length, startingPhone) ;
						newArr = newArr.addAll(startArr) ;
						current = startingPhone ;
						n = n+1 ;
						//totalLength = totalLength + length ;
						}	
				)
		})
		^newArr 
	}


	dot { arg path = "/musica/antigone/scores/grammarGraph.dot", utterance = [] ;
		var dotFile = File(path, "w") ;
		var header = "
digraph G {
graph [bgcolor=\"#66CCFF\"] ;
node[shape=rectangle, color=orange, fillcolor=white, fontcolor=orange, style=filled];
\"NODES\\n fill: 	black:no ins -->  white:max ins \\n border: 	black:no outs --> white: max outs \\n circle: connected \\n rectangle: roots \\n diamond: leaves \\n Msquare: looping roots \\n Mdiamond: looping leaves\\n EDGES\\n black: no outs --> white: max outs\"
" ;
		dotFile.write(header);
		(total.addAll(nimGraph.keys)).asSet.asArray.do({ arg key ;
		//nimGraph.keys.do({ arg key ;
			var shape, fillColor ;
			var outs = nimGraph[key].size ; // outColor
			var ins = total.occurencesOf(key) ;// inColor ;
			var outColor = Color(outs/maxConn, outs/maxConn, outs/maxConn) ;
			var inColor = Color(ins/maxInConn, ins/maxInConn, ins/maxInConn) ;
			outColor = "\#"++[outColor.red, outColor.green, outColor.blue]
				.collect{|x| (x*255)
				.asInteger.asHexString(2)}.join ;
			inColor = "\#"++[inColor.red, inColor.green, inColor.blue]
				.collect{|x| (x*255).asInteger
				.asHexString(2)}.join ;
			shape = case
				{ roots.includes(key).and(nimGraph[key]==[])} {"hexagon"}
			 	{ roots.includes(key) } 	{ "rectangle" }
			 	{ leaves.includes(key) }	{ "diamond" } 
			 	{ nimGraph[key]==[key] }	{ "Mdiamond" }
			 	{ loopingRoots.includes(key) } { "Msquare" } 
			 	{ total.includes(key) }		{ "circle" } 
			 	;
			if ( utterance.includes(key), 
					{ 	fillColor = Color(utterance.occurencesOf(key)/utterance.size*2, 0, 0.5) ;
						fillColor = "\#"++[fillColor.red, fillColor.green, fillColor.blue]
							.collect{|x| (x*255).asInteger
							.asHexString(2)}.join ;
					}, {fillColor = inColor }) ;
			
			key = key.asString.split($*)[0] ;
			dotFile.write(format("			
node[
color=\"%\", fillcolor=\"%\", fontcolor=orange,
fontname=Helvetica, shape=%, style=\"bold, filled\"
] ;\n", outColor, fillColor, shape)) ;
			dotFile.write(format("\"%\";\n", key)) ;	
		}) ;
		nimGraph.do({ arg value ;
			var key = nimGraph.findKeyForValue(value) ;
			var outs = nimGraph[key].size ; // outColor
			var outColor = Color(outs/maxConn, outs/maxConn, outs/maxConn) ;
 			outColor = "\#"++[outColor.red, outColor.green, outColor.blue]
				.collect{|x| (x*255)
				.asInteger.asHexString(2)}.join ;
 			key = key.asString.split($*)[0] ;
			value.do({ arg target ;
				target = target.asString.split($*)[0] ;
				dotFile.write(format("edge[color=\"%\"] ;\n", outColor)) ;
				dotFile.write(format("\"%\" -> \"%\";\n", key, target)) ;			}) ;
			}) ;
		dotFile.write("\}");
		dotFile.close ;	
		// unixCmd("python /musica/antigone/code/unicoder.py") //done later

	}
	
						
						
	rootsAndLeaves {
		var keys = nimGraph.keys ;
		var values = [], roots = [] , leaves = [] ;
		nimGraph.do({ arg value ;
			values = values.addAll(value) ;
		}) ;
		keys.do({ arg key ;
			if ( nimGraph[key] == [], { leaves = leaves.add( key ) }) ;
			if ( values.includes(key).not, 
				{ roots = roots.add(key) } ) ;
			}) ;
		"roots:".postln ;
		roots.do({ arg i ; i.postln}) ;
		"leaves:".postln ;
		leaves.do({ arg i ; i.postln}) ;
		^[roots, leaves]
	
	}
	
	calculateLoopingRoots {
		var loops = [] ; 
		var values = [] ;
		loopingRoots = [] ;
		nimGraph.do({ arg value ;
			var key = nimGraph.findKeyForValue(value) ;
			var newValue = value.deepCopy ;
			if ( value.includes(key), { newValue.remove(key) } );
			values = values.addAll(newValue) ;	
		}) ;
		nimGraph.keys.do({
			arg key ;	
			if ( nimGraph[key].includes(key), { loops = loops.add(key) } )
			}) ;		
		loops.do ({ arg loopKey ;
			if ( values.includes(loopKey).not, { loopingRoots = loopingRoots.add( loopKey ) } ) ;
		}) ;			
	^loopingRoots.postln
	}
	
	maxInConnections { 
		var max = 0 ;
		nimGraph.keys.do ({ arg key ;
			var maxIn = total.occurencesOf(key) ;
			if ( maxIn > max, { max = maxIn }) ;
		})
		^max.postln
	
	}
							
						
							
}
		




		
			/* test grammar
				(
				'@tCap*0*f*'	:	['@tCap*0*f*', 't@oCap*0*f*'],
				'k@oCap*-1*t*':	['@oCap*0*t*', '@nCap*0*t*'],
				's*0*t*'		:	['@sCap*0*t*', 't@oCap*0*f*'],
				't@oCap*0*f*'	:	['t@oCap*0*f*', '@oCut*-1*t*'],
				'@sCap*0*t*'	:	['@oCut*-1*t*', 's*0*t*', '@sCap*0*t*'],
				'@oCut*-1*t*'	:	['@nCap*0*t*', '@oCut*-1*t*'],
				'h*0*t*'		:	['h*0*t*'],
				'hi*1*t*'		:	['i*1*t*'],
				'pi*1*f*'		:	['pj@schwa*0*f*', 'hi*1*t*'],
				'pj@schwa*0*f*':	['@schwa*0*t*'],
				'x@tCap@oCap*0*f*'	:	['m*0*t*'],
				'ti*1*f*'		:	['tik*-1*f*'],
				'tik*-1*f*'	:	['tik*-1*f*'] ,	
				'te*1*f*'		:	['@glott@ae*1*t*', 'te*1*f*'],
				'st*0*f*'		:	['tum*-1*t*', 's*0*t*', '@tsch*0*f*'],
				'tum*-1*t*'	:	['tum*-1*t*', 'm*-1*t*'],
				'@tsch*0*f*'	:	['@tsch*0*f*', '@tschy*-1*f*','tik*-1*f*'],
				'@tschy*-1*f*':	['@tschy*-1*f*', 'm*0*t*'],
				'm*0*t*'		: 	['m*1*t*', 'm*-1*t*'],
				'm*-1*t*'		:	['m*0*t*', 'm*-1*t*'],
				'm*1*t*'		:	['m*0*t*'],
				'd@schwa*0*f*':	['@schwa*0*t*'],
				'@schwa*0*t*'	:	['x*0*t*'],		
				'f*0*t*'		:	['f*0*t*', '@tsch*0*f*'],
				'ka*-1*t*'	:	['@glott@ae*1*t*'],
				'@glott@ae*1*t*': 	['e@nCap*1*t*', 'h*0*t*'],
				'e@nCap*1*t*'	:	['e@nCap*1*t*','@nCap*0*t*'],
				'@nCap*0*t*'	:	['e@nCap*1*t*', '@schwa*0*t*'],
				'x*0*t*'		:	['tik*-1*f*', 'i*1*t*'],
				'i*1*t*'		: 	['y*0*t*', '@cCap*0*t*'],
				'y*0*t*'		:	['u*-1*t*'],
				'u*-1*t*'		:	['m*-1*t*', 'x*0*t*'],
				'to*1*t*'		:	['@oCap*0*t*'],
				'@oCap*0*t*'	:	['@oCut*-1*t*'],	
				'@cCap*0*t*'	:	['i*1*t*', '@cCap*0*t*'],
				'r*0*t*'		:	['r*0*t*', '@sCap*0*t*'],
				'g@ae*1*t*'	:	['@nCap*0*t*'], 
				'@iCap*0*t'	: 	[]//, '@iCap*0*t'] // to test diamond
				);*/