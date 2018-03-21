C {
	var <>symbol, <>root, <>name, <>structure, <>transp ;
	classvar <>symbols ;
	classvar <>decay, <>release ;

	*initClass {
		var t ;
		decay = 0.5;
		release = 0.5 ;
		symbols = () ;
		t = "CM Cmaj	{0,4,7}
Cm C- Cmin	{0,3,7}
C+ Caug CM#5 CM+5	{0, 4, 8}
C° Cdim Cmb5 Cm˚5	{0, 3, 6}
C7 Cdom7	{0, 4, 7, 10}
CM7 CMa7 Cj7 Cmaj7	{0, 4, 7, 11}
CmM7 Cm#7 C−M7 Cminmaj7	{0, 3, 7, 11}
Cm7 C-7 Cmin7	{0, 3, 7, 10}
C+M7 Caugmaj7 CM7#5 CM7+5	{0, 4, 8, 11}
C+7 Caug7 C7#5 C7+5	{0, 4, 8, 10}
CØ CØ7 Cø Cø7 Cmin7dim5 Cm7b5 Cm7°5 C−7b5 C−7°5	{0, 3, 6, 10}
Co7 C°7 Cdim7	{0, 3, 6, 9}
C7b5 Cdom7dim5	{0, 4, 6, 10}
CM9 Cmaj9	{0, 4, 7, 11, 14}
C9 Cdom9	{0, 4, 7, 10, 14}
CmM9 C−M9 Cminmaj9	{0, 3, 7, 11, 14}
Cm9 C−9 Cmin9	{0, 3, 7, 10, 14}
C+M9 Caugmaj9	{0, 4, 8, 11, 14}
C+9 C9#5 Caug9	{0, 4, 8, 10, 14}
CØ9	{0, 3, 6, 10, 14}
CØ9b9	{0, 3, 6, 10, 13}
C°9 Cdim9	{0, 3, 6, 9, 14}
C°b9 Cdimb9	{0, 3, 6, 9, 13}
C11 Cdom11	{0, 4, 7, 10, 14, 17}
CM11 Cmaj11	{0, 4, 7, 11, 14, 17}
CmM11 C−M11 Cminmaj11	{0, 3, 7, 11, 14, 17}
Cm11 C−11 Cmin11	{0, 3, 7, 10, 14, 17}
C+M11 Caugmaj11	{0, 4, 8, 11, 14, 17}
C+11 C11#5 Caug11	{0, 4, 8, 10, 14, 17}
CØ11	{0, 3, 6, 10, 13, 17}
C°11	{0, 3, 6, 9, 13, 16}
CM13 Cmaj13	{0, 4, 7, 11, 14, 17, 21}
C13 Cdom13	{0, 4, 7, 10, 14, 17, 21}
CmM13 C−M13 Cminmaj13	{0, 3, 7, 11, 14, 17, 21}
Cm13 C−13 Cmin13	{0, 3, 7, 10, 14, 17, 21}
C+M13 Caugmaj13	{0, 4, 8, 11, 14, 17, 21}
C+13 C13#5 Caug13	{0, 4, 8, 10, 14, 17, 21}
CØ13	{0, 3, 6, 10, 14, 17, 21}
C6 CM6	{0,4,7,9}
Cm6 Cminmaj6	{0,3,7,9}
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
				symbols[key] = ch[1]
			}
		}

	}

	*new { arg symbol;
		^super.new.initC(symbol)
	}

	*audio {

		Server.local.waitForBoot{
			SynthDef(\piano, {|note = 60, vol = -6, decay, release|
				Out.ar(0,
					MdaPiano.ar(note.midicps, decay:decay, release:release)* vol.dbamp*Line.kr(1,1,6, doneAction:2))
			}).add

		}
	}

	initC { arg aSymbol ;
		var data, sus, add ;
		symbol = aSymbol ;
		transp = 60 ;
		data =  this.getRoot(symbol.asString) ;
		root = data[0] ; name = data[1];
		structure = symbols[symbol.asString.replace(name.asString, "")
			.replace("sus", "@").split($@)[0]
			.replace("add", "@").split($@)[0]
			.asSymbol] ;
		sus = this.processSus(symbol) ;
		if(sus.notNil) {
			structure.remove(3) ;
			structure.remove(4) ;
			structure = structure.add(sus)
		} ;
		add = this.processAdd(symbol) ;
		if (add.notNil) {
			structure = structure.add(add)
		}

	}

	processSus {|symbol|
		var sus = nil ;
		if (symbol.asString.contains("sus")){
			sus = symbol.asString.replace("sus", "@").split($@)[1];
			case
			{sus == ""}{sus = 5}
			{sus == [4]}{sus = 5}
			{sus == [2]}{sus = 2}
		} ;
		^sus
	}

	processAdd {|symbol|
		var add = nil ;
		var base ;
		var dict = (\9: 14, \11: 17, \13: 21) ;
		if (symbol.asString.contains("add")){
			add = symbol.asString.replace("add", "@").split($@)[1];
			base = dict[add.replace("b", "").replace("#", "").asSymbol] ;
			case {add.split($b)[1].notNil}{
				add = base-1 ;
			}
			{add.split($#)[1].notNil}{
				add = base+1 ;
			}
			{(add.split($#)[1].isNil) && (add.split($b)[1].isNil) }
			{add = base;
			}
		} ;
		^add
	}

	// c = C('CMadd9') ; // c.structure

	getRoot  { arg symbol ;
		var chr, act, root, rootName, alt ;
		var noteBase = (\C:0,\D:2,\E:4,\F:5,\G:7,\A:9,\B:11) ;
		name = symbol[0] ;
		root = noteBase[name.asSymbol] ;
		if ([$b, $#].includes(symbol[1]) ){
			name = name++symbol[1] ;
			root = root + [-1,1][[$b, $#].indexOf(symbol[1])]
		} ;
		^[root, name.asSymbol] ;
	}

	play {|vol = -9|
		(transp+structure+root).do{|i|
			Synth(\piano,  [\note, i, \vol, vol, \decay, decay, \release, release])
		}

	}
}

// C.audio;
// C.decay = 0.25; C.release = 0.1
// c = C(\C7add13).play ; c.structure.postln
// c.root
// C('Gbmaj9').play
/*
{
"Eb7 Dm7 Eb7 Dm7 Eb7 Dm7 Em7b5 A7b5 Dm7".split($ ).collect{|i|
C(i.postln.asSymbol).play;
1.wait
}
}.fork


{
//re incarnation of a love bird
"Gm7 Ebmaj7 Am7b5 D7#9 Gm7 Ebmaj7 Am7b5 D7 Gm Ebmaj7 Cm7 F7 Am7b5 D7 Gm".split($ ).collect{|i|
C(i.postln.asSymbol).play;
1.wait
}
}.fork

*/