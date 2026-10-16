"""Original seamless outboard loops. No third-party sound recordings."""
from pathlib import Path
import sys, json
sys.path.insert(0,str(Path(__file__).parent/'python-audio'))
import numpy as np
import soundfile as sf

root=Path(__file__).parent/'boat-motors-v2/src/main/resources/assets/boat_motors'
out=root/'sounds'
out.mkdir(exist_ok=True)
rate=44100
duration=4
t=np.arange(rate*duration)/rate
rng=np.random.default_rng(63126)
events={}
for tier,(f0,cylinders) in enumerate([(47,2),(43,2),(40,3),(36,4),(32,6)],1):
    # Integer cycles over four seconds, including slow modulation, keep seams continuous.
    phase=2*np.pi*f0*t + .045*np.sin(2*np.pi*2*t)
    pulses=sum(np.sin(h*phase+0.13*h)/(h**1.05) for h in range(1,18))
    low=.48*np.sin(phase/2) + .21*np.sin(phase*1.5)
    gear=.10*np.sin(phase*7+cylinders*.2*np.sin(2*np.pi*3*t))
    noise=rng.normal(0,1,len(t))
    spec=np.fft.rfft(noise)
    hz=np.fft.rfftfreq(len(t),1/rate)
    spec*=np.exp(-hz/1700)*(1-np.exp(-hz/70))
    air=np.fft.irfft(spec,n=len(t))
    air/=max(np.max(np.abs(air)),1e-9)
    combustion=.8+.2*np.sin(phase)
    wave=(.44*pulses+.38*low+gear+.16*air*combustion)
    wave=np.tanh(wave*1.2)
    wave*=.78/max(np.max(np.abs(wave)),1e-9)
    sf.write(out/f'engine_{tier}.ogg',wave,rate,format='OGG',subtype='VORBIS')
    events[f'engine_{tier}']={'subtitle':'subtitles.boat_motors.engine','sounds':[{'name':f'boat_motors:engine_{tier}','attenuation_distance':24}]}
    decoded,sr=sf.read(out/f'engine_{tier}.ogg')
    assert sr==rate and len(decoded)==rate*duration and np.max(np.abs(decoded))>0.5
    print(f'engine_{tier}.ogg: {len(decoded)/sr:.1f}s, peak {np.max(np.abs(decoded)):.3f}')
(root/'sounds.json').write_text(json.dumps(events,indent=2))
lang=root/'lang/en_us.json'
strings=json.loads(lang.read_text(encoding='utf-8-sig'))
strings['subtitles.boat_motors.engine']='Boat motor running'
lang.write_text(json.dumps(strings,indent=2))
