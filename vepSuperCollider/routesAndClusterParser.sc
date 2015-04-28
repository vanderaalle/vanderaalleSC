/*
VEP Project, 25/12/09

Parses the textfile containing grouping info on speakers
by Andrea Arghinenti and creates a comfortable data structure


Name of routes/clusters to be used when writing a score:
		group_N,
		cluster_A,
		cluster_B,
		cluster_C,
		cluster_J,
		cluster_U,
		L.T.,
		group_140_III,
		route_0,
		route_I,
		route_II,
		route_III,
		route_IV,
		route_V,
		route_VI,
		route_VIII

*/

RouteAndClusterParser {

	var <>speakerListFilePath ;
	var <>speakerListString ;
	var <>speakerDict ;

	*new { arg speakerListFilePath ;
		^super.new.initRouteAndClusterParser(speakerListFilePath)
	}


	initRouteAndClusterParser { arg aSpeakerListFilePath ;
		var file, keys ;
		speakerListFilePath = aSpeakerListFilePath ;
		file = File(speakerListFilePath, "r") ;
		speakerListString = file.readAllString ;
		file.close ;
		speakerDict = Dictionary.new ;
		keys = [
		\group_N,
		\cluster_A,
		\cluster_B,
		\cluster_C,
		\cluster_J,
		\cluster_U,
		'L.T.',
		\group_140_III,
		\route_0,
		\route_I,
		\route_II,
		\route_III,
		\route_IV,
		\route_V,
		\route_VI,
		\route_VIII
		] ;
		this.createDict ;
	}
	
	createDict {
		var dict = Dictionary.new ;
		speakerListString = speakerListString
			.split($@) ;
		speakerListString.do{|item, index| var k = item.split($#) ;
			dict.put(k[0].replace(" ", "").replace("\n", "").asSymbol, 
			k[1].replace("\n", "%").split($%).reject{|i| i.size <2}.clump(3))
		} ;
		dict[\cluster_A].postln ;
		dict.keys.asArray.do { |key|
			speakerDict.put(key, this.createSpeakersFromArr(dict[key.postln].postln)) ; 
		}
	}


	createSpeakersFromArr { arg arr ;
		var speakerList ;
		var name, pos ;
		arr.do{ |itm|
			name = itm[0].asSymbol ;
			pos = itm[1].split($ ).collect{ |i| i.asFloat } ;
			speakerList = speakerList.add([name, pos].flat) 
		}
		^speakerList ;	
	}

	archive { arg pathname ;
		speakerDict.writeArchive(pathname) ;
	}
}
