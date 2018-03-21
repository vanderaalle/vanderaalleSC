// CAUTION: no5 untested
ChordAnalyzer {

	// reverse class from C

	classvar <>symbols ;

	*initClass {
		symbols = () ;
		"CM	{0,4,7}
Cm	{0,3,7}
C+	{0, 4, 8}
C°	{0, 3, 6}
C7	{0, 4, 7, 10}
C7no5	{0, 4, 10}
CM7	{0, 4, 7, 11}
CM7no5	{0, 4, 11}
CmM7	{0, 3, 7, 11}
CmM7no5	{0, 3, 11}
Cm7	{0, 3, 7, 10}
Cm7no5	{0, 3, 10}
C+M7	{0, 4, 8, 11}
C+7	{0, 4, 8, 10}
CØ	{0, 3, 6, 10}
Co7	{0, 3, 6, 9}
C7b5	{0, 4, 6, 10}
CM9	{0, 4, 7, 11, 14}
CM9no5	{0, 4, 11, 14}
C9	{0, 4, 7, 10, 14}
C9no5	{0, 4, 10, 14}
CmM9	{0, 3, 7, 11, 14}
CmM9no5	{0, 3, 11, 14}
Cm9	{0, 3, 7, 10, 14}
Cm9no5	{0, 3, 10, 14}
C+M9	{0, 4, 8, 11, 14}
C+9	{0, 4, 8, 10, 14}
CØ9	{0, 3, 6, 10, 14}
CØ9b9	{0, 3, 6, 10, 13}
C°9	{0, 3, 6, 9, 14}
C°b9	{0, 3, 6, 9, 13}
C11	{0, 4, 7, 10, 14, 17}
CM11	{0, 4, 7, 11, 14, 17}
CmM11	{0, 3, 7, 11, 14, 17}
Cm11	{0, 3, 7, 10, 14, 17}
C+M11	{0, 4, 8, 11, 14, 17}
C+11	{0, 4, 8, 10, 14, 17}
CØ11	{0, 3, 6, 10, 13, 17}
C°11	{0, 3, 6, 9, 13, 16}
CM13	{0, 4, 7, 11, 14, 17, 21}
C13	{0, 4, 7, 10, 14, 17, 21}
CmM13	{0, 3, 7, 11, 14, 17, 21}
Cm13	{0, 3, 7, 10, 14, 17, 21}
C+M13	{0, 4, 8, 11, 14, 17, 21}
C+13	{0, 4, 8, 10, 14, 17, 21}
CØ13	{0, 3, 6, 10, 14, 17, 21}
C7#9	{0, 4, 7, 10, 15}
C7b9	{0, 4, 7, 10, 13}
C7#11	{0, 4, 7, 10, 19}
C7b11	{0, 4, 7, 10, 17}
Csus4	{0, 5, 7}
Csus2	{0, 2, 7}
Cadd2	{0, 2, 4, 7}
Cadd2	{0, 2, 4, 7}"
		.split($\n)
		.collect{|i| i.split($\t)}.collect{|i|
			[
				i[0].split($\ ).collect{|j| j.replace("C", "").asSymbol},
				i[1].replace("{", "[").replace("}", "]").interpret

			]
		}.collect{|ch|
			ch[0].do{|key|
				symbols[key] = (ch[1]%12).sort
			}
		};

	}

	*reorder {|chord, note|
		var form = ((chord-note)%12).sort ;
		^(form-form[0])
	}

	*analyze {|chord|
		var order, name;
		var ch = [] ;
		chord.do{|note|
			order = this.reorder(chord, note) ;
			name = symbols.findKeysForValue(order) ;
			if(name.size>0){
				ch = ch.add([note%12, name[0]]) // only first name
			}
		};
		^ch[0] // only 1 option, but that's the case
	}

	*convertName {|sig|
		var dict = (0: "C", 1: "C♯",  2: "D", 3: "D♯",
            4: "E", 5: "F", 6: "F♯", 7: "G", 8: "G♯",
            9: "A", 10: "A♯", 11: "B");
		^(dict[sig.postln[0]].asString++sig[1].asString).postln
	}

}

/*
c = [0, 4, 11, 14]
ChordAnalyzer.convertName(ChordAnalyzer.analyze(c))
ChordAnalyzer.symbols['(C7)']
*/