"""Editable native Minecraft cuboid geometry and hand-authored pixel materials."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import json, random, math
ROOT=Path(__file__).parent/'boat-motors-v2/src/main/resources/assets/boat_motors'
TEX=ROOT/'textures/block'
TEX.mkdir(exist_ok=True)
def material(name,color):
    rng=random.Random(name)
    im=Image.new('RGB',(32,32),color)
    for y in range(32):
        for x in range(32):
            n=rng.choice([-5,-2,0,0,0,2,4])
            im.putpixel((x,y),tuple(max(0,min(255,c+n)) for c in color))
    d=ImageDraw.Draw(im)
    d.line((0,0,31,0),fill=tuple(min(255,c+20) for c in color))
    d.line((0,31,31,31),fill=tuple(max(0,c-18) for c in color))
    im.save(TEX/f'{name}.png')
for name,col in {'gunmetal':(46,51,57),'graphite':(25,29,35),'alloy':(116,125,135),'rubber':(17,20,24),'chrome':(181,190,197),'can_red':(158,24,23),'can_ridge':(195,37,31),'can_empty':(103,113,122),'can_empty_ridge':(145,154,162)}.items():material(name,col)
def box(a,b,tex,rotation=None):
    e={'from':a,'to':b,'faces':{f:{'uv':[0,0,16,16],'texture':'#'+tex} for f in ['north','south','east','west','up','down']}}
    if rotation:e['rotation']=rotation
    return e
def plane_badge(a,b,face):
    return {'from':a,'to':b,'faces':{face:{'uv':[0,0,16,16],'texture':'#badge'}}}
def save(name,data):
    dest=ROOT/'models/item'/f'{name}.json';dest.parent.mkdir(exist_ok=True,parents=True)
    dest.write_text(json.dumps(data,indent=2))
    (ROOT/'items'/f'{name}.json').write_text(json.dumps({'model':{'type':'minecraft:model','model':f'boat_motors:item/{name}'}},indent=2))
def displays(gui=.66):
    return {'gui':{'rotation':[12,-145,0],'translation':[0,1.7,0],'scale':[gui]*3},'ground':{'translation':[0,4,0],'scale':[.38]*3},'firstperson_righthand':{'rotation':[0,155,0],'translation':[0,2,0],'scale':[.5]*3},'firstperson_lefthand':{'rotation':[0,205,0],'translation':[0,2,0],'scale':[.5]*3},'thirdperson_righthand':{'rotation':[0,180,0],'translation':[0,2,0],'scale':[.44]*3},'fixed':{'rotation':[0,180,0],'scale':[.6]*3}}
for i,(name,hp,color,s) in enumerate([
 ('motor_9_9hp','9.9',(166,30,29),.68),('motor_20hp','20',(27,66,159),.81),('motor_25hp','25',(30,113,52),.94),('motor_50hp','50',(185,132,22),1.08),('motor_100hp','100',(42,45,51),1.23)]):
    material(name+'_paint',color)
    badge=Image.new('RGB',(64,64),(22,25,30));d=ImageDraw.Draw(badge)
    d.rectangle((2,2,61,61),outline=(139,147,156),width=2)
    font=ImageFont.truetype('C:/Windows/Fonts/consolab.ttf',31 if hp!='9.9' else 29)
    b=d.textbbox((0,0),hp,font=font);d.text(((64-(b[2]-b[0]))/2,12),hp,fill=(247,250,255),font=font,stroke_width=0)
    badge.save(TEX/f'{name}_badge.png')
    left=3.5 if i<2 else 3
    right=16-left
    e=[
     # Layered cowling, narrow upper lip, contrasting belly and bright seam.
     box([left,9.2,3.3],[right,16.3,12.7],'paint'),
     box([left+.5,16.3,3.8],[right-.5,17.1,12.2],'paint'),
     box([left+1.1,17.1,4.4],[right-1.1,17.5,11.6],'paint'),
     box([left-.12,9,3.18],[right+.12,9.65,12.82],'chrome'),
     box([left+.5,7.8,3.9],[right-.5,9,12.1],'graphite'),
     box([left+1.4,7,4.7],[right-1.4,7.8,11.3],'gunmetal'),
     # Midsection and exhaust housing with side ribs.
     box([6.1,-2.4,6],[9.9,7.5,9.9],'gunmetal'),
     box([6.6,-2.4,5.4],[9.4,5.6,6.05],'graphite'),
     box([5.7,-1,6],[6.1,5,9.4],'alloy'),
     box([9.9,-1,6],[10.3,5,9.4],'alloy'),
     # Cavitation plate, gearcase, nose, prop shaft, skeg.
     box([4.6,-2.8,5.4],[11.4,-2.1,12.2],'graphite'),
     box([6,-4.8,6.2],[10,-2.8,12.7],'gunmetal'),
     box([6.6,-4.5,4.7],[9.4,-3.1,6.2],'gunmetal'),
     box([7,-4.8,12.7],[9,-2.8,14.6],'alloy'),
     box([7.5,-7.4,6.2],[8.5,-4.6,10.2],'graphite'),
     # Four bladed propeller and end nut.
     box([3.8,-4.25,13.25],[12.2,-3.35,14.0],'alloy'),
     box([7.55,-7.7,13.25],[8.45,.1,14.0],'alloy'),
     box([7.1,-4.7,14],[8.9,-2.9,14.7],'graphite'),
     box([7.5,-4.3,14.7],[8.5,-3.3,15],'chrome'),
     # Transom clamps, tilt axle and visible bolt heads.
     box([3.2,5.1,1],[5.1,9.6,6],'gunmetal'),
     box([10.9,5.1,1],[12.8,9.6,6],'gunmetal'),
     box([3.2,8.5,.2],[5.1,9.6,1],'alloy'),
     box([10.9,8.5,.2],[12.8,9.6,1],'alloy'),
     box([4.2,6.1,3],[11.8,7.6,4.5],'alloy'),
     box([2.9,6.25,1.5],[3.4,7.25,2.5],'chrome'),
     box([12.6,6.25,1.5],[13.1,7.25,2.5],'chrome'),
     plane_badge([5.2,10.5,12.83],[10.8,15.8,12.85],'south'),
     plane_badge([right+.01,10.7,5.2],[right+.03,15.7,10.8],'east'),
     plane_badge([left-.03,10.7,5.2],[left-.01,15.7,10.8],'west')]
    # Cowling intake louvers and lower side stripes.
    for k in range(3):
        e.append(box([left+.9,12.3+k*.65,3.23],[right-.9,12.6+k*.65,3.31],'graphite'))
    if i<3:
        e += [box([1,10.2,-2],[6.2,11.1,4],'gunmetal'),box([-.8,9.95,-3],[2.1,11.4,-.3],'rubber')]
    else:
        e += [box([4.5,8.1,1.5],[11.5,8.9,3],'gunmetal'),box([5.2,9.8,2.7],[10.8,11.2,3.5],'graphite')]
    for part in e:
        for axis,anchor in enumerate([8,6,8]):
            part['from'][axis]=round(anchor+(part['from'][axis]-anchor)*s,3)
            part['to'][axis]=round(anchor+(part['to'][axis]-anchor)*s,3)
    textures={k:f'boat_motors:block/{k}' for k in ['gunmetal','graphite','alloy','rubber','chrome']}
    textures.update(paint=f'boat_motors:block/{name}_paint',badge=f'boat_motors:block/{name}_badge',particle=f'boat_motors:block/{name}_paint')
    save(name,{'credit':'Original detailed outboard model, reference-inspired cowling and drivetrain','textures':textures,'elements':e,'display':displays(.64)})
for name,paint,ridge in [('gas_can','can_red','can_ridge'),('empty_gas_can','can_empty','can_empty_ridge')]:
    e=[box([3.4,2,5.2],[12.6,12.4,10.8],'paint'),box([4,1.4,5.8],[12,2,10.2],'paint'),
       box([3.8,12.4,5.6],[12.2,13.1,10.4],'ridge'),box([3.1,2.2,5],[12.9,2.8,11],'ridge'),
       box([3.2,3,5],[3.8,11.6,11],'ridge'),box([12.2,3,5],[12.8,11.6,11],'ridge'),
       # An open carry handle, with a dark grip and actual gap underneath.
       box([7.2,13.1,6.2],[8.3,15.2,9.8],'ridge'),box([11,13.1,6.2],[12.1,15.2,9.8],'ridge'),
       box([7.2,15.2,6.2],[12.1,16.1,9.8],'ridge'),box([8.3,15.15,6.15],[11,16.15,9.85],'paint'),
       # Angled filler neck, black cap, small metallic latch.
       box([3.6,12.4,6.2],[5.9,14,9.8],'alloy',{'origin':[4.75,12.4,8],'axis':'z','angle':-22.5}),
       box([3.2,13.8,6],[6.3,14.5,10],'graphite',{'origin':[4.75,12.4,8],'axis':'z','angle':-22.5}),
       box([3.7,14.45,7.5],[5.8,14.9,8.5],'alloy',{'origin':[4.75,12.4,8],'axis':'z','angle':-22.5})]
    for z in [5.05,10.85]:
        for angle in [-45,45]:
            e.append(box([7.7,4.1,z],[8.3,10.7,z+.18],'ridge',{'origin':[8,7.4,z],'axis':'z','angle':angle}))
        e.append(box([6.8,6.4,z-.02],[9.2,8.4,z+.22],'paint'))
    tex={k:f'boat_motors:block/{v}' for k,v in [('paint',paint),('ridge',ridge),('alloy','alloy'),('graphite','graphite'),('particle',paint)]}
    display=displays(.87);display['gui']['translation']=[0,-.2,0]
    save(name,{'credit':'Original 3D jerry can with open handle, angled filler, and embossed reinforcement ribs','textures':tex,'elements':e,'display':display})
    print(name, len(e),'elements')
print('Five outboards and both gas cans updated.')
