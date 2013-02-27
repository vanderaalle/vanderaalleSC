// real draft
// just to recover functionaliity

StereoMixer {
	
	var <>busList, <>synth ;

	
	*new { arg busList; 
		^super.new.initStereoMixer(busList) 	
	}

	initStereoMixer { arg aBusList ;
		Server.local.waitForBoot{
			{
		busList = aBusList ;
		SynthDef(\mix, { arg in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11,
			in12, in13, in14, in15, in16, 
			pan1 = 0, pan2 = 0, pan3 = 0, pan4 = 0, pan5 = 0, pan6 = 0, pan7 = 0,
			pan8 = 0, pan9 = 0, pan10 = 0, pan11 = 0,
			pan12 = 0, pan13 = 0, pan14 = 0, pan15 = 0, pan16 = 0,
			vol1 = 1, vol2 = 1, vol3 = 1, vol4 = 1, vol5 = 1, vol6 = 1, vol7 = 1,
			vol8 = 1, vol9 = 1, vol10 = 1, vol11 = 1,
			vol12 = 1, vol13 = 1, vol14 = 1, vol15 = 1, vol16 = 1,
			main = 1 ;
			var arr = [
			Pan2.ar(In.ar(in1)*vol1, pan1),
			Pan2.ar(In.ar(in2)*vol2, pan2),
			Pan2.ar(In.ar(in3)*vol3, pan3),	
			Pan2.ar(In.ar(in4)*vol4, pan4),	
			Pan2.ar(In.ar(in5)*vol5, pan5),	
			Pan2.ar(In.ar(in6)*vol6, pan6),	
			Pan2.ar(In.ar(in7)*vol7, pan7),	
			Pan2.ar(In.ar(in8)*vol8, pan8),	
			Pan2.ar(In.ar(in9)*vol1, pan9),	
			Pan2.ar(In.ar(in10)*vol10, pan10),	
			Pan2.ar(In.ar(in11)*vol11, pan11),	
			Pan2.ar(In.ar(in12)*vol12, pan12),	
			Pan2.ar(In.ar(in13)*vol13, pan13),	
			Pan2.ar(In.ar(in14)*vol14, pan14),	
			Pan2.ar(In.ar(in15)*vol15, pan15),					Pan2.ar(In.ar(in16)*vol16, pan16)		
			] ;
			Out.ar(0, 
				Mix.new(arr)*main
			)
			}).add ;
		Server.local.sync ;
		synth = Synth(\mix) ;
		Server.local.sync ;
		busList.do{ |bus, i|
			synth.set(("in"++(i+1).asString).asSymbol, bus)
			}
			}.fork
	}
	}
	
	
	randPan { arg min = -1, max = 1 ;
		16.do{|i| synth.set(("pan"++i.asString).asSymbol, rrand(min, max)) }
	}	
	
	main { arg val ; synth.set(\main, val) }

	mute { arg in ;
		synth.set((\in++(in.asString)).asSymbol, 0)
		}
		
	unmute { arg in ;
		synth.set((\in++(in.asString)).asSymbol, busList[in-1])
		}
	
	muteAll { Array.series(16,1).do{|i|this.mute(i)} }

	unmuteAll { Array.series(16,1).do{|i|this.unmute(i)} } 
	
	zero { arg in ;
		synth.set((\vol++(in.asString)).asSymbol, 0)
		}

	one { arg in ;
		synth.set((\vol++(in.asString)).asSymbol, 1)
		}


	vol { arg in, val ;
		synth.set((\vol++(in.asString)).asSymbol, val)
		}
	
	zeroAll { Array.series(16,1).do{|i|this.zero(i)}} 
	
	oneAll { Array.series(16,1).do{|i|this.one(i)}} 

	
	solo { arg in ;
		var arr = Array.series(16, 1) ;
		arr.remove(in) ;
		arr.do{|i| this.mute(i)} 
	}
			
	unsolo { arg in ;
		var arr = Array.series(16, 1) ;
		arr.remove(in) ;
		arr.do{|i| this.unmute(i)} 
	}
}