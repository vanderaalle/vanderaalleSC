/* 
// a class to get the mp3s from WordReference
// --> http://www.wordreference.com/
// works until site structure changes..
// Usage:
//
// WordReferencer.new(aFolder).getWord(word, version, convert, clean)
//
// ex.:
// var myAudioFolder = "/tmp" ;
// WordReferencer.new(myAudioFolder).getWord("test", 1, false, false)
//
// --> test.mp3 is downloaded to aFolder (def: /tmp)
// --> version 1/2: us/uk (def: 1)
// --> converted to wav (it uses sox: use what you prefer, see source) (def: true)
// --> mp3 is deleted (def:true)
//
// shorter, assuming conversion is ok:
// var myAudioFolder = "/tmp" ;
// WordReferencer.new(myAudioFolder).getWord("clever")
// 
// Note: not all wordreference words are spoken
// tested on macosx (uses cmdline)
// andrea v 28/10/07
*/


WordReferencer {

	var <>mp3Folder ;
	
	*new { arg mp3Folder = "/tmp" ; 
		^super.new.initWordReferencer(mp3Folder) 
	}

	initWordReferencer { arg anMp3Folder ;
		mp3Folder = anMp3Folder ;
	}

	downloadHtml { arg word ;
		var p, l, url, path ;
		url = "http://www.wordreference.com/definition/"++word ;
		path = "/tmp/"++word++".html" ;
		p = Pipe.new("curl"+url+"-o"+path, "r");	
		l = p.getLine;							
		while({l.notNil}, { l = p.getLine });	
		p.close ;	
	}

	getMp3Name { arg word ; 
		var path, text, mp3Name ;
		path = "/tmp/"++word++".html" ;
		text = File( path, "r" ).readAllString ;
		mp3Name = text
			.replace("http://www.wordreference.com/audio/en/uk/","@")
			.replace("-2.mp3", "@")
			.split($@)[1] ;
		^mp3Name  
	}
	
		
	downloadMp3 { arg mp3Name, word, version = 1 ;
		var p, l, url, path ;
		if (version == 1, { mp3Name = mp3Name++"-1.mp3" ;
			url = "http://www.wordreference.com/audio/en/us/"++mp3Name 
			}, {
			mp3Name = mp3Name++"-2.mp3" ;
			url = "http://www.wordreference.com/audio/en/uk/"++mp3Name 
			}) ;
		path = mp3Folder++"/"++word++".mp3" ;
		p = Pipe.new("curl"+url+"-o"+path, "r");	
		l = p.getLine;							
		while({l.notNil}, {l.postln ; l = p.getLine; });	
		p.close ;	
	}
	
	convertToWav { arg word ;
		var p, l ;
		var path = mp3Folder++"/"++word ;
		// var command = ("lame --decode"+path++".mp3"+path++".wav").postln ;
		// still not able to have it working on my machine from SC
		// I'll go with sox app
		var command = ("/Applications/SoXWrap.app/Contents/Resources/sox"
			+path++".mp3" + "-w" + path++".wav") ;
		p = Pipe.new(command, "r");
		l = p.getLine;							
		while({l.notNil}, {l.postln ; l = p.getLine; });
		p.close ;	
	}
	
	removeMp3 { arg word ;
			var p, l ;
			var path = mp3Folder++"/"++word ;
			p = Pipe.new("rm"+path++".mp3", "r");
			l = p.getLine;							
			while({l.notNil}, {l.postln ; l = p.getLine; });	
			p.close ;
	}
	

	getWord { arg word, version = 1, convert = true, clean = true ;
		var name = this.downloadHtml(word)
					.getMp3Name(word) ;
		this.downloadMp3(name, word, version) ;
		("rm /tmp/"++word++".html").unixCmd ;
		if ( convert, { this.convertToWav(word, true)}) ;
		if ( clean, 	{ this.removeMp3( word ) }) ;
	}

}
