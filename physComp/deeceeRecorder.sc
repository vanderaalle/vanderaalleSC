// a logger for DeeCees
DeeCeeRecorder {

	var <>dcArr ; // an array of dcs, to be logged
	var <>log ; // an array of dc events
	var <>startTime ; // initial offset for rec, you change it when you start rec

	*new { arg dcArr ;
		^super.new.initDeeCeeRecorder(dcArr)
	}

	initDeeCeeRecorder { arg aDcArr ;
		dcArr = aDcArr ;
		dcArr.do{|dc| dc.addDependant(this) } ;
		log = [] ;
		startTime = thisThread.seconds ;
	}

	// when we start rec
	fromNow { startTime = thisThread.seconds }
	reset { log = []; this.fromNow }

	update { arg theChanged, theChanger, more;
		more.postln;
		log = log.add(more.add(thisThread.seconds-startTime))

	}

	writeScore { arg path ;
		var f = File(path, "w"), l, list, score ;
		var name, id, which ;
		var instr = (
			\girodisco1:1,
			\girodisco2:2,
			\girodisco3:3,
			\molatore1:4,
			\molatore2:5,
			\molatore3:6,
			\molatore4:7,
			\armonica1:8,
			\armonica2:9,
			\armonica3:10,
			\zampogno1:11,
			\zampogno2:12,
			\zampogno3:13,
			\zampogno4:14,

			\radio1:15,
			\radio2:16,
			\radio3:17,
			\mangiocassetta1:18,
			\mangiocassetta2:19,
			\mangiocassetta3:20,
			\segopiatto1:21,
			\segopiatto2:22,
			\segopiatto3:23,
			\segopiatto4:24,
			\spremoagrume:25,
			\rasoio:26,
			\meshugghello:27,
			\lampadina:28
		) ;
		l = log.deepCopy ;
		list = l.collect{|i| var n = instr.findKeyForValue(([0,14][i[0]-1]+i[1]).asInteger); [n,i[2]] } ;
		score = [] ;
		instr.keys.asArray.do{|key|
			if (list.flat.includes(key)) {
				score = score.add(
					list.select{|i| i[0] == key}
					.flop[1]
					.clump(2)
					.collect{|i| [key, i[0], i[1]-i[0]]}
				)
			}
		} ;
		f.write ("BPM 60\nTIMESIG (4 4)\n") ;
		score.flatten.do{|ev|
			f.write("\""++ev[0].asString.capitalize++"\""+ev[1]+ev[2]++"\n") ;
		} ;
		f.close
	}


}

//
//
/*
x = DeeCee1.new(2, 1); y = DeeCee1.new(2+4, 2);
r = DeeCeeRecorder([x, y]) ;
r.log

(
r.reset;

{
	10.do{|i|
		x.addEvent((i%3+1).postln) ;
		0.1.wait ;
		x.addEvent((i%3+1).postln) ;
		0.1.wait;
	} ;
}.fork ;

{
	10.do{|i|
		y.addEvent((i%4+1).postln) ;
		0.12.wait ;
		y.addEvent((i%4+1).postln) ;
		0.12.wait ;
	} ;

}.fork
)
// r.log
// r.log.collect{|i| if(i[0] == 2){i[1] = i[1]+14; i[1..]}{i[1..]} }
r.writeScore("/musica/regnumAnimale/comp/sources/9.Test.test.txt")


a = Object.read
//
//
*/

//a = Object.readArchive("/musica/regnumAnimale/comp/scores/18.Phola.reicha.regnum")

// b = Object.readArchive("/musica/regnumAnimale/comp/scores/test.regnum")