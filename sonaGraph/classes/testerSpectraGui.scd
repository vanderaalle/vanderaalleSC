g = SonaGraphGui.new(a, ~sample, hStep:5, vStep:6).makeGui(-40) ;
g.select(0, 40) ;g.drawSelected ;
m = SpectraDaw.from(g.selected, -30);

SD.play(m, 15, 20) ;
SpectraDawGui(m, 4).makeGui
SD.play(m, 4, 20) ;

j = SD.transpose(m, 12) ;
SD.play(j, 4, 15)

l = SD.transpose(m, -12)

SD.play(l, 4, 15)

h = SD.mix([m, j, l])
SD.play(h, 4, 10)
SpectraDawGui(h, 4).makeGui

u = SD.concatenate(Array.fill(4, {h}))
SpectraDawGui(u, 4).makeGui

SD.play(u, 4, 10)

//
m = SD.stretch(m, 3) ;
l = SD.stretch(l, 5) ;
m = SD.concatenate(Array.fill(4, {m}))
l = SD.concatenate(Array.fill(3, {l}))
j = SD.concatenate(Array.fill(6, {j}))
h = SD.mount([l, m, j], [0, 4, 8])
h = SD.cut(h, 14, 259)
SpectraDawGui(h, 4).makeGui ;
Chronometer.new

(
// Apollo 11 sample file spectral canon
// aka SPACE INVADER COUNTERPOINT
~arr = Array.fill(8, {|i| SD.transpose(m, i*7-30)})
.collect{|i, j|	SD.stretch(i, (j+1)*2)} ;

~arr = SD.mount(~arr, Array.fill(~arr.size, {|i| i*6})) ;
~arr = SD.cut(~arr, 14, 410) ;
~arr = ~arr++SD.reverse(~arr) ;
SpectraDawGui(~arr, 1).makeGui(labStep:20) ;
)