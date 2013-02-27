
+ UGen {

	// polling support
	poll2 { arg interval = 0.1, label = "UGen:", arr ;
		^SynthDef.wrap({
			var id, responder;
			id = this.hash & 0x7FFFFF;
			switch(this.rate, 
				\audio, {SendTrig.ar(Impulse.ar(interval.reciprocal), id, this)},
				\control, {SendTrig.kr(Impulse.kr(interval.reciprocal), id, this)}
			);
			responder = OSCresponderNode(nil,'/tr',{ arg time, rder, msg;
				if(msg[2] == id, { arr = arr.add(label.asString + msg[3]).postln;});
			}).add;
			CmdPeriod.doOnce({ responder.remove; arr.postln });
			this;
		})
	}


}