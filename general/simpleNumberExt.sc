+ String {

	play { arg releaseTime = 3.0;
		var id, name, s;
		var freq = this.notemidi.midicps ;
		s = Server.default;
		id = s.nextNodeID;
		name = "note_test" ++ id;
		SynthDef(name, { arg gate=1;
			Out.ar(0,
				SinOsc.ar(freq, pi/2, 0.3) * EnvGen.ar(Env.perc, gate, doneAction:2)
			)
		}).play(s);
	}

}