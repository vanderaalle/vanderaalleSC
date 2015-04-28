~root = "/HotRats/";
a = SampleListGenerator.fromPath(~root) ;
{
a.do{|p|
	i = p.asString.replace(" ", "\ ") ;
	("/usr/local/bin/lame "++~root++p++".aiff"+~root++p++".mp3").postln.unixCmd ;
		5.wait
}
}.fork