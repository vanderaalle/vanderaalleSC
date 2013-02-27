// Classes for ICMC12

DarkStoreICMC12 {

	var <>analyzer;
	var <>text, <>tArr, <>current, <>active ;
	var <>loudness, <>centroid, <>flatness ; 
	var <>task, <>dur, <>letterRank ;
	var <>minRate, <>maxRate ;
	var <>doc ;

	*new { arg analyzer ;
		^super.new.initDarkStore(analyzer)
	}

	initDarkStore{ arg anAnalyzer ;
		analyzer = anAnalyzer ;
		analyzer.addDependant(this) ;
		//current = 0 ;
		active = true ; // this is a flag for not/increasing sentence
		//this.setupTask ;
		this.setupTask ;
		centroid = 60 ;
		dur = 1 ;
		// number of events per sec (it's a rate)
		minRate = 1 ;
		maxRate = 1 ;
	}



	
	pushText { arg path ;
		var file =  File(path, "r") ;
		this.setupText(file.readAllString)  ;
		file.close ;
	}

	// split into sentences, we iterate over the array
	setupText { arg aText ; 
		text = aText ;
		text = if (aText == "") {" "} { aText } ; // protecting again nil string
		tArr = text.split($.).select{|i|i.size>0} ; // protecting againt a void element ("..")
		if (tArr == []) {tArr = [text]} ;
		current = 0 ;
		//this.setupTask ;
	}
	
	
	runAnalyzer { arg flag = true ;
		analyzer.onsets.run(flag).set(\thresh, 0.4) ;	
		analyzer.flatness.run(flag) ;
		analyzer.loudness.run(flag) ;
		analyzer.centroid.run(flag) ;
	}
	
	
	// setting up the sequences	
	setupTask{
		// avoiding the changing the element over which we iterate
		task = Task({
			var cur = current ;
			inf.do{
			tArr[cur].do{|letter|
				// we just forward the stuffa all around
				this.changed(this, 
					[\event, 
						letter.asSymbol,letter.toLower.asSymbol,
						loudness, centroid, flatness
					]) ;
				dur.wait ;
				cur = current ;
				} ;
				this.changed(this, [\restart]) ;
			}
		}) ;
		}
	
	increaseCurrent {
		if (current.notNil)
			{ current = (current+1) % tArr.size };
		//current.postln
		}
	
	update { arg theChanged, theChanger, more ;
		// we receive stuff and just round it up a little
		case 
			{ more[0] == \loudness }
				{ loudness = more[1].round(0.1)}
			{ more[0] == \centroid }
				{ centroid = more[1].cpsmidi.round }
			{ more[0] == \flatness }
				{ flatness = more[1].round(0.01)}
			{ more[0] == \onset }
				// event? so lets have a new sentence 
				{ 
					if (active){
						this.increaseCurrent ; 	
					} ;
					// in ev per sec
					dur = centroid.linlin(60, 120, maxRate.reciprocal, minRate.reciprocal)
				}
	}

}




AdapterICMC12 {
	
	// for ICMC12 setting: switch with
	// 1-5: 		phons
	// 6-10:	 	radios
	// 11-15:		solenoids

	var <>darkStore ;
	var <>dc, <>ranking, <>letters ;
	var <>minElement, <>maxElement ;
	
	*new { arg darkStore, offset = 2 ;
			^super.new.initAdapter(darkStore, offset)
	}

	initAdapter { arg store ;
		darkStore  = store ;
		this.createRank ;
		dc =  DeeCee(2) ; // we have just one
		minElement = 1 ;
		maxElement = 15 ;
		darkStore.addDependant(this) ;
	}


	createRank {
		// for ita, see Cifre
		var sourceRanking = ["11.79%", "11.74%", "11.28%", "9.83%", "6.88%", "6.51%", "6.37%", "5.62%", "4.98%", "4.5%", "3.73%", "3.05%", "3.01%", "2.51%", "2.10%", "1.64%", "1.54%", "0.95%", "0.92%", "0.51%", "0.49%" ].collect{|i|i.replace("%", "").interpret} ;
		letters = "e
a
i
o
n
l
r
t
s
c
d
p
u
m
v
g
h
f
b
q
z".split($\n).collect{|i| i.asSymbol} ;
		ranking = sourceRanking.collect{|it, id| sourceRanking[..id].sum } ;
	}
	
	getRank { arg letterAsSymbol ;
		^ranking[letters.indexOf(letterAsSymbol)].linlin(0, 100, minElement, maxElement+1).trunc
	}

	update { arg theChanged, theChanger, more ;
			// centroid is mapped by DarkStore
			// we need to map loudness, flatness
			// flatness: 0, 1 // we have no param for flatness
			// loudness: 5, 50
			/*
			[\event, letter.asSymbol, letter.toLower.asSymbol,
						loudness, centroid, flatness ]
			*/
		var rank, dur ;
		var letter, loudness, centroid, flatness ;
		if(more[0] == \event){
			letter = more[2] ;
			if (letters.includes(letter)) {
				#loudness, centroid, flatness = more[3..];
				rank = this.getRank(letter) ;
				dur = loudness.linlin(5,50, 0.5, 1) * darkStore.dur - 0.01;
				dc.event(rank, dur) ;
				//[darkStore.dur, dur].postln ;
				}
		}

	}

}

/*

~store = Dark

*/