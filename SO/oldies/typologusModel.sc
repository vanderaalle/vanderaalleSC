/*
TypologusModel : Model {

	var <>objectDict ; 	// the global dict to store all data
	var <>rank ;			// can be useful to have a progressive ordering
	var <>collection ;		// a string defining a name for the collection
	var <>classDict ;		// a dict containing className -> [range]
	var <>folder ;		// the folder where the audio files are places



	*new { arg folder ;
		folder = folder? (String.scDir++"/sounds/")
		^super.new.initModel(folder)
		}

	initModel { arg aFolder ;
		folder = aFolder ;
		objectDict = IdentityDictionary.new ; // just creating a Dict
		rank = 0 ;
		collection = "a Collection" ;
		classDict = IdentityDictionary[
			\Hn		-> [[-2.5, -1.5],[0.0, 1.0],[0.0, 1.0]],
			\N 		-> [[-1.5, -0.5],[0.0, 1.0],[0.0, 1.0]],
			\Ni 		-> [[-0.5, 0.5],[0.0, 1.0],[0.0, 1.0]],
			\Nii		-> [[0.5, 1.5],[0.0, 1.0],[0.0, 1.0]],
			\Zn		-> [[1.5, 2.5],[0.0, 1.0],[0.0, 1.0]],
			\Hx 		-> [[-2.5, -1.5],[1.0, 2.0],[0.0, 1.0]],
			\X 		-> [[-1.5, -0.5],[1.0, 2.0],[0.0, 1.0]],
			\Xi 		-> [[-0.5, 0.5],[1.0, 2.0],[0.0, 1.0]],
			\Xii 	-> [[0.5, 1.5],[1.0, 2.0],[0.0, 1.0]],			\Zx 		-> [[1.5, 2.5],[1.0, 2.0],[0.0, 1.0]],
			\P 		-> [[1.5, 2.5],[0.0, 2.0],[1.0, 2.0]],
			\Yii 	-> [[0.5, 1.5],[0.0, 2.0],[1.0, 2.0]],
			\Yi 		-> [[-0.5, 0.5],[0.0, 2.0],[1.0, 2.0]],
			\Y 		-> [[-1.5, -0.5],[0.0, 2.0],[1.0, 2.0]],
			\T 		-> [[-1.5, -2.5],[0.0, 2.0],[1.0, 2.0]],
			\E 		-> [[-1.5, -2.5],[0.0, 2.0],[2.0, 3.0]],
			\W		-> [[-1.5, -0.5],[0.0, 2.0],[2.0, 3.0]],
			\Phi		-> [[-0.5, 0.5],[0.0, 2.0],[2.0, 3.0]],
			\K		-> [[0.5, 1.5],[0.0, 2.0],[2.0, 3.0]],
			\A 		-> [[1.5, 2.5],[0.0, 2.0],[2.0, 3.0]]
				] ;


	 }


	addObject { arg name, position, remarks ;
		// name: string (-> symbol)
		// you can pass a string or a symbol
		// position: array [x, y, z]
		// remarks: string
		objectDict.add(name.asSymbol -> [position, remarks, rank]) ;
		rank = rank + 1
	}

	removeObject { arg name ;
		// name: string -> asSymbol
		// you can pass a string or a symbol
		objectDict.removeAt(name.asSymbol)
	}

	clean {
		objectDict = IdentityDictionary.new ;
		rank = 0 ;
		collection = "a Collection" ;
	}

	retrieve { arg name ;
		if ( objectDict[name.asSymbol].isNil.not,
			{ ^objectDict[name.asSymbol] },
			{^[[],[],[]] } )
	}

	changePosition { arg name, newPosition ;
		var position, remarks, rank ;
		name = name.asSymbol ;
		#position, remarks, rank = this.retrieve(name) ;
		objectDict[name] = [newPosition, remarks, rank] ;
		this.changed(\position, [name, position, remarks, rank])
	}

	changeRemarks { arg name, newRemarks ;
		var position, remarks, rank ;
		name = name.asSymbol ;
		#position, remarks, rank = this.retrieve(name) ;
		objectDict[name] = [position, newRemarks, rank]
	}

	rename { arg name, newName ;
		var position, remarks, rank ;
		name = name.asSymbol ;
		#position, remarks, rank = this.retrieve(name) ;
		this.removeObject(name) ;
		objectDict[newName.asSymbol] = [position, remarks, rank] ;
	}

/* Storing/retrieving in internal format */
	writeToArchive { arg path ;
		this.writeArchive(path)
	}

	readFromArchive { arg path ;
		^Object.readArchive(path)
	}
/**/



// for back-compatibilty with Python implementation
// NOTE: also in ViewController
	createOrderedDictionary {
		var orderedDict = IdentityDictionary.new ;
		var position, remarks, rank ;
		objectDict.keys.do({ arg key ;
			#position, remarks, rank = objectDict[key] ;	 		orderedDict.add(rank -> [key, position, remarks]) ;
		}) ;
		^orderedDict
	}

	createElementList {
		// returns an elementList = [[],[],[]]
		var nameList = [], positionList = [], remarkList = [] ;
		var orderedDict = this.createOrderedDict ;
		var name, position, remarks ;
		orderedDict.keys.asArray.sort({ arg key ;
			#name, position, remarks = orderedDict[key] ;
			nameList = nameList.add(name) ;
			positionList = positionList.add(position) ;
			remarkList = remarkList.add(remarks)
		}) ;
		^[nameList, positionList, remarkList]
	}
///////////


// writing and reading the xml file
	writeToFile { arg outFilePath = "/typologus.xml";
		var xmlFile = File.new(outFilePath, "w") ;
		var orderedDict = this.createOrderedDictionary ;
		var header = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>
<!--
Created with Typologus
Andrea Valle 2007
andrea.valle@unito.it
http://www.cirma/unito.it/andrea
!-->
" 			;
		var name, position, remarks ;
		xmlFile.write(header) ;
		xmlFile.write("<collection>" + collection + "\n") ;
		orderedDict.keys.asArray.sort.do({ arg key ;
				#name, position, remarks = orderedDict[key] ;
				xmlFile.write("<object>\n") ;
				xmlFile.write("<name> " + name + "</name>\n") ;
				xmlFile.write("<position> " + position + "</position>\n") ;
				xmlFile.write("<remarks> " + remarks + "</remarks>\n") ;
				xmlFile.write("</object>\n") ;
		}) ;
		xmlFile.write("</collection>\n") ;
		xmlFile.close
	}


	// rudimental parser
	// known bug: if the collection is name "collection" it's a problem
	// (and so on)
	readFromFile { arg inFilePath ;
		var xmlFile = File(inFilePath, "r") ;
		var data = xmlFile.readAllString.postln ;
		var newData = [] ;
		var name, position, remarks ;
		data = data.split($<) ;
		data.do({ arg datum ; datum = datum.split($>) ;
			newData = newData.addAll(datum) ;
		}) ;
		this.clean ;
		data = [] ;
		newData.do({ arg item ;
					item = item.replace(" ", "") ;
					item = item.replace("\n", "") ;
					data = data.add(item) ;
					}) ;
		data.do({ arg item, index ;
			case { item == "collection" }
					{ collection = data[index+1] }
				{ item == "name" }
					{ name = data[index+1] }
				{ item == "position" }
					{ position = data[index+1].interpret }
				{ item == "remarks" }
					{ remarks = data[index+1] }
				{ item == "/object" }
					{ this.addObject(name, position, remarks) }
		}) ;
	}


//// SPECIAL //// just to import some old stuf
	// rudimental parser
	// known bug: if the collection is name "collection" it's a problem
	// (and so on)

	readFromHTMLFile { arg inFilePath ;
		var xmlFile = File(inFilePath, "r") ;
		var data = xmlFile.readAllString ;
		var newData = [] ;
		var name, position, remarks ;
		data = data.split($<) ;
		data.do({ arg datum ; datum = datum.split($>) ;
			newData = newData.addAll(datum) ;
		}) ;
		this.clean ;
		newData.do({ arg item, index ;
			item.postln ;
			case { item == "h1" }
					{ collection = newData[index+1].replace(" ", "") }
				{ item == "h3" }
					{ name = newData[index+1] ;
					name.postln ;
					//.replace(" ", "")
					}
				{ item[0].asSymbol == "(".asSymbol }
					{
				position = item.replace(" ", "").replace("(", "[")
							.replace(")", "]").interpret }
				{ item[..2] ==  "Pro" }
					{
					remarks = item ;
					} ;
				this.addObject(name, position, remarks)
		}) ;
	}
//////////////////////////////


// inspecting
	findInClass { arg className ;
		var range = classDict[className.asSymbol] ;
		var objectList, position ;
		var x, y, z ;
		objectDict.do({ arg item ;
			#x,y,z =  item[0] ;
			if ( ((x >= range[0][0])
				.and(x < range[0][1])
				.and(y >= range[1][0])
				.and(y < range[1][1])
				.and(z >= range[2][0])
				.and(z < range[2][1]))
				,
				{ objectList = objectList.add(objectDict.findKeyForValue(item))
					} )
		})
		^objectList
		}

	// returns all the sustained objects
	findSustained {
		var objectList = [] ;
		[\Hn, \N, \Hx, \X, \T, \Y, \E, \W].do({
			arg className ;
			objectList = objectList.addAll(this.findInClass(className))

		}) ;
		^objectList
	}

	// returns all the impulsive objects
	findImpulsive {
		var objectList = [] ;
		[\Ni, \Yi, \Phi, \Xi].do({
			arg className ;
			objectList = objectList.addAll(this.findInClass(className))

		}) ;
		^objectList
	}


	// returns all the iterative objects
	findIterative {
		var objectList = [] ;
		[\Zn, \Nii, \Zx, \Xii, \Yii, \P, \A, \K].do({
			arg className ;
			objectList = objectList.addAll(this.findInClass(className))

		}) ;
		^objectList
	}

	// returns all the central objects
	findCentral {
		var objectList = [] ;
		[\N, \Ni, \Nii, \X, \Xi, \Xii, \Y, \Yi, Yii].do({
			arg className ;
			objectList = objectList.addAll(this.findInClass(className))
		}) ;
		^objectList
	}


	// returns all the excentric objects
	findExcentric {
		var objectList = [] ;
		[\E, \W, \Phi, \K, \A].do({
			arg className ;
			objectList = objectList.addAll(this.findInClass(className))
		}) ;
		^objectList
	}

	// returns all the homogeneous objects
	findHomogeneous {
		var objectList = [] ;
		[\T, \E, \P, \A].do({
			arg className ;
			objectList = objectList.addAll(this.findInClass(className))
		}) ;
		^objectList
	}

	// return a list of all the neighbors of start
	// ordered from nearest to farthest
	findNeighbours { arg startName, number = objectDict.size ;
		var distDict = Dictionary.new ;
		var position = objectDict[startName.asSymbol][0] ;
		var distance, name, keys, neighbours ;
		objectDict.do({ arg obj ;
			distance = this.calculateDistanceByPosition(position, obj[0]) ;
			name = objectDict.findKeyForValue(obj) ;
			distDict.add(distance -> name)
		}) ;
		keys = distDict.keys.asArray.sort ;
		keys.do({ arg key ;
			neighbours = neighbours.add(distDict[key])
		})
		^neighbours[1..(number-1)]
	}

	calculateDistanceByPosition	{ arg aPosition, anotherPosition ;
		var distance, x1, y1, z1, x2, y2, z2 ;
		#x1, y1, z1 = aPosition ;
		#x2, y2, z2 = anotherPosition ;
		distance = sqrt((x2-x1).squared + (y2-y1).squared + (z2-z1).squared) ;
		^distance
	}

	printObject { arg name ;
		var position, remarks, rank ;
		#position, remarks, rank = objectDict[name.asSymbol] ;
		"O+++++++++++++++++++++++++++++O".postln ;
		"name: ".post; name.postln ;
		"position: ".post; position.postln ;
		"remarks: ".post; remarks.postln ;
		"rank: ".post; rank.postln ;
		"O-----------------------------O\n\n\n".postln
		}

	printAllObjects {
		objectDict.keys.do({ arg key ;
			this.printObject (key)
		})
	}

////////////////

// play the file
	play { arg name, loop = false ;
		var path = folder++name ;
		var format ;
		[".aiff", ".aif", ".wav"].do({ arg ext ;
			if ( File.exists(path++ext), { format = ext }) ;
		}) ;
		if (format.isNil, { "no file found".postln ; ^this } );
		path = path++format ;
		if (Server.local.serverRunning.not, { Server.local.boot } ) ;
		Server.local.doWhenBooted{
				Buffer.read(Server.local, path, action: {|buf| buf.play(loop) }) ;
		}
	}

}

*/