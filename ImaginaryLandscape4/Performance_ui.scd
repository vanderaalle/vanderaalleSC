
p = IM4Performer.new("/Users/andrea/Dropbox/Progetto Imaginary Landscape IV/SuperCollider/ScoreCoding/ScoreCoding") ;

p.play([1, 2 , 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]) ;


p.play([1]) ;
p.playAll ;
p.stop ;
p.reset ;
"/Users/Eime/Dropbox/Progetto Imaginary Landscape IV/SuperCollider/ScoreCoding/ScoreCoding"++"01"
"cazzo 23".replace(" ", "") ;
p.put


a = SerialPort("/dev/tty.usbmodem24111", 9600) ;
b = SerialPort("/dev/tty.usbmodem24121", 9600) ;
c = SerialPort("/dev/tty.usbmodem24131", 9600) ;

~radio = 3 ;
~time = 0.07 ;
~which = 0 ;
t = Task{
	var index = 0 ;
	inf.do{|i|
		index = index + 1 ;
		if (index < 256){
			[a,b,c][~which].put(~radio) ;
			0.01.wait ;
			[a,b,c][~which].put(((255-index).log10 * 106).round)}{index = 0} ;
		[index.log10, index].postln ;
		~time.wait ;
	}
}
t.play ;


~radio = 3 ;
~time = 0.07 ;
~which = 0 ;
t = Task{
	var index = 0 ;
	inf.do{|i|
		index = index + 1 ;
		if (index < 256){
			[a,b,c][~which].put(~radio) ;
			0.01.wait ;
			[a,b,c][~which].put(((255-index).log10 * 106).round)}{index = 0} ;
		[index.log10, index].postln ;
		~time.wait ;
	}
}

// RESET
(
{
3.do{|i|
		"reset".postln ;
	4.do{|k|
			{[a,b,c][i].put(k); 0.01.wait ;[a,b,c][i].put(0)}.fork;
			0.01.wait ;

	} ;
0.1.wait ;
} ;
	"done".postln
}.fork ;
)

~which = 2 ; ~radio = 2 ;
{[a,b,c][~which].put(~radio); 0.01.wait ;[a,b,c][~which].put(255)}.fork;


b.put(250)

(2.41*106).round
b.close


SerialPort.listDevices


[a,b,c,].so{|i| i.close}