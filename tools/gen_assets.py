#!/usr/bin/env python3
"""Gera modelo, texturas, blockstate, lang, loot, receita e tags do Raptor 3 (assets em src/main/resources).
Uso: python3 tools/gen_assets.py  (precisa de Pillow). Opcional: --preview para a imagem de comparacao."""
import json, math, os, random, sys
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src/main/resources")
ASSETS = os.path.join(RES, "assets/rocketatmosphere")
DATA = os.path.join(RES, "data")
random.seed(3)

def write(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        if isinstance(obj, str): f.write(obj)
        else: json.dump(obj, f, indent=2, ensure_ascii=False)

# ------------------------------------------------------------------ texturas (atlas 16x16 de amostras 4x4)
SW = {'black': (0, 0), 'black2': (1, 0), 'steel': (2, 0), 'steeld': (3, 0),
      'gun': (0, 1), 'gunl': (1, 1), 'white': (2, 1), 'teal': (3, 1),
      'copper': (0, 2), 'copperd': (1, 2), 'pipe': (2, 2), 'dark': (3, 2)}
COL = {'black': (24, 24, 28), 'black2': (38, 38, 44), 'steel': (150, 156, 168), 'steeld': (96, 102, 114),
       'gun': (62, 67, 78), 'gunl': (94, 100, 112), 'white': (236, 236, 240), 'teal': (38, 172, 178),
       'copper': (178, 112, 66), 'copperd': (122, 74, 46), 'pipe': (30, 30, 35), 'dark': (6, 6, 8)}

def atlas():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    px = img.load()
    for name, (c, r) in SW.items():
        base = COL[name]
        for y in range(4):
            for x in range(4):
                n = random.randint(-5, 5) if name not in ('white', 'dark') else random.randint(-2, 2)
                px[c * 4 + x, r * 4 + y] = tuple(max(0, min(255, v + n)) for v in base) + (255,)
    for c, r in [(0, 3), (1, 3), (2, 3), (3, 3)]:          # amostras livres: cinza neutro
        for y in range(4):
            for x in range(4): px[c * 4 + x, r * 4 + y] = (90, 90, 96, 255)
    return img

def uv(name):
    c, r = SW[name]
    return [c * 4, r * 4, c * 4 + 4, r * 4 + 4]

# ------------------------------------------------------------------ geometria (eixo do motor = Z; topo em z=0, bocal em z=32)
elements = []
def faces(tex, caps, cap_tex):
    f = {s: {"uv": uv(tex), "texture": "#0"} for s in ("east", "west", "up", "down")}
    if caps[0]: f["north"] = {"uv": uv(cap_tex or tex), "texture": "#0"}
    if caps[1]: f["south"] = {"uv": uv(cap_tex or tex), "texture": "#0"}
    return f

def box(x0, y0, z0, x1, y1, z1, tex, name, caps=(True, True), cap_tex=None, rot=None, origin=None, shade=True):
    e = {"name": name, "from": [round(x0, 3), round(y0, 3), round(z0, 3)], "to": [round(x1, 3), round(y1, 3), round(z1, 3)],
         "faces": faces(tex, caps, cap_tex)}
    if rot:
        e["rotation"] = {"angle": rot, "axis": "z", "origin": origin or [8, 8, round((z0 + z1) / 2, 3)]}
    elements.append((e, tex))

ANG = {1: [0], 2: [0, 45], 4: [0, 22.5, 45, -22.5]}
DIV = {1: 1.0, 2: 1.25, 4: 1.36}      # raio medio da uniao de quadrados girados = a * DIV

def ring(cx, cy, z0, z1, r, tex, name, n=4, caps=(True, True), cap_tex=None):
    a = r / DIV[n]
    for k, ang in enumerate(ANG[n]):
        box(cx - a, cy - a, z0, cx + a, cy + a, z1, tex, f"{name}_{k}", caps, cap_tex,
            rot=ang if ang else None, origin=[cx, cy, round((z0 + z1) / 2, 3)])

def frame(z0, z1, r, wall, tex, name):
    a = r / DIV[4]
    for k, ang in enumerate(ANG[4]):
        o = [8, 8, round((z0 + z1) / 2, 3)]
        r_ = ang if ang else None
        box(8 - a, 8 + a - wall, z0, 8 + a, 8 + a, z1, tex, f"{name}_t{k}", rot=r_, origin=o)
        box(8 - a, 8 - a, z0, 8 + a, 8 - a + wall, z1, tex, f"{name}_b{k}", rot=r_, origin=o)
        box(8 - a, 8 - a + wall, z0, 8 - a + wall, 8 + a - wall, z1, tex, f"{name}_l{k}", rot=r_, origin=o)
        box(8 + a - wall, 8 - a + wall, z0, 8 + a, 8 + a - wall, z1, tex, f"{name}_r{k}", rot=r_, origin=o)


# --- topo / turbobombas (prata e cinza-escuro), como na foto
ring(8, 8, 0.0, 2.2, 2.6, 'steel', 'top_cap')
ring(8, 8, 2.2, 3.0, 4.0, 'steeld', 'top_flange')
ring(8, 8, 3.0, 9.7, 3.6, 'gun', 'pump_body', caps=(False, False))
ring(8, 8, 5.2, 5.8, 4.3, 'gunl', 'pump_band', caps=(False, False))
ring(3.7, 8.5, 3.4, 8.0, 1.9, 'steel', 'tp_left', n=2)
ring(11.2, 4.4, 3.6, 7.4, 1.5, 'gun', 'tp_low', n=2)
ring(12.2, 9.4, 5.0, 6.6, 1.1, 'teal', 'valve_teal', n=2)
ring(12.2, 9.4, 4.6, 5.0, 1.2, 'steel', 'valve_teal_cap', n=2)
box(5.5, 12.0, 5.0, 7.5, 14.0, 8.0, 'gunl', 'valve_a')
box(8.8, 12.3, 5.2, 10.8, 14.0, 7.6, 'steel', 'valve_b')
box(4.2, 3.2, 3.6, 5.2, 4.2, 6.4, 'copper', 'sensor_a')
box(6.6, 2.6, 3.6, 7.6, 3.6, 6.0, 'copperd', 'sensor_b')
# --- flange largo preto, garganta longa e sino (proporcoes medidas na foto: garganta ate z~18,5; sino abre ate r~6,9)
ring(8, 8, 9.7, 10.9, 5.4, 'black', 'wide_flange')
ring(8, 8, 10.9, 18.5, 3.0, 'black2', 'neck', caps=(False, False))
ring(8, 8, 12.6, 13.1, 3.9, 'steeld', 'neck_band', caps=(False, False))
ring(8, 8, 15.7, 16.7, 3.9, 'black', 'neck_torus', caps=(False, False))
def bell_r(z): return 3.6 + 3.3 * math.sqrt(max(0.0, (z - 18.5) / 13.5))
rings_n = 12
zs = [18.5 + 0.99 * i for i in range(rings_n + 1)]
for i in range(rings_n):
    zm = (zs[i] + zs[i + 1]) / 2
    last = i == rings_n - 1
    ring(8, 8, zs[i], zs[i + 1], bell_r(zm), 'black', f'bell_{i}', caps=(True, last), cap_tex='dark' if last else None)
ring(8, 8, 18.2, 18.8, 4.5, 'black2', 'bell_flange', caps=(False, False))
frame(30.4, 32.0, 7.2, 1.0, 'black2', 'lip')
# --- tubo grande em arco (lado direito) e numero "1" branco
box(13.4, 7.1, 3.4, 15.0, 8.9, 15.0, 'pipe', 'pipe_run')
box(10.6, 7.1, 3.4, 13.4, 8.9, 5.2, 'pipe', 'pipe_top')
box(10.6, 7.1, 13.2, 13.4, 8.9, 15.0, 'pipe', 'pipe_bottom')
box(14.6, 7.55, 26.0, 14.9, 8.35, 28.6, 'white', 'one_stem')
box(14.6, 8.35, 26.0, 14.9, 9.0, 26.8, 'white', 'one_flag')

model = {"credit": "Raptor 3 - gerado por tools/gen_assets.py",
         "ambientocclusion": False,
         "textures": {"0": "rocketatmosphere:block/raptor_3", "particle": "rocketatmosphere:block/raptor_3"},
         "elements": [e for e, _ in elements],
         "display": {
             "gui": {"rotation": [80, 0, 55], "translation": [0, 3.5, 0], "scale": [0.45, 0.45, 0.45]},
             "fixed": {"rotation": [80, 0, 55], "translation": [0, 3.5, 0], "scale": [0.45, 0.45, 0.45]},
             "ground": {"translation": [0, 2, 0], "scale": [0.3, 0.3, 0.3]},
             "thirdperson_righthand": {"rotation": [80, 0, 0], "translation": [0, 3, 1], "scale": [0.25, 0.25, 0.25]},
             "thirdperson_lefthand": {"rotation": [80, 0, 0], "translation": [0, 3, 1], "scale": [0.25, 0.25, 0.25]},
             "firstperson_righthand": {"rotation": [80, 0, 30], "translation": [0, 3, 0], "scale": [0.3, 0.3, 0.3]},
             "firstperson_lefthand": {"rotation": [80, 0, 30], "translation": [0, 3, 0], "scale": [0.3, 0.3, 0.3]}}}
for (e, _) in elements:
    for v in (e["from"], e["to"]):
        assert all(-16 <= c <= 32 for c in v), (e["name"], v)

write(os.path.join(ASSETS, "models/block/raptor_3.json"), model)
write(os.path.join(ASSETS, "models/item/raptor_3.json"), {"parent": "rocketatmosphere:block/raptor_3"})
os.makedirs(os.path.join(ASSETS, "textures/block"), exist_ok=True)
atlas().save(os.path.join(ASSETS, "textures/block/raptor_3.png"))

# blockstate: mesmas rotacoes do thruster do Propulsion (modelo canonico: FACING=north, bocal para +Z)
rot = {"north": {}, "south": {"y": 180}, "west": {"y": 270}, "east": {"y": 90}, "up": {"x": 270}, "down": {"x": 90}}
variants = {}
for face, r in rot.items():
    for multi in ("false", "true"):
        variants[f"facing={face},multi={multi}"] = dict({"model": "rocketatmosphere:block/raptor_3"}, **r)
write(os.path.join(ASSETS, "blockstates/raptor_3.json"), {"variants": variants})

# ------------------------------------------------------------------ fluidos e baldes
def fluid_tex(base, name, flow):
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            n = random.randint(-10, 10)
            wave = int(8 * math.sin((x + (y if flow else 0)) * 0.9))
            c = tuple(max(0, min(255, v + n + wave)) for v in base)
            px[x, y] = c + (215,)
    img.save(os.path.join(ASSETS, f"textures/block/{name}.png"))

fluid_tex((140, 165, 195), "methane_still", False); fluid_tex((140, 165, 195), "methane_flow", True)
fluid_tex((115, 175, 255), "liquid_oxygen_still", False); fluid_tex((115, 175, 255), "liquid_oxygen_flow", True)

def bucket(fill, name):
    os.makedirs(os.path.join(ASSETS, "textures/item"), exist_ok=True)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    outline, body, shade = (50, 52, 60, 255), (170, 175, 185, 255), (120, 124, 134, 255)
    for y in range(4, 15):
        inset = (y - 4) // 4
        d.line([(3 + inset, y), (12 - inset, y)], fill=body)
        d.point((3 + inset, y), fill=outline); d.point((12 - inset, y), fill=outline)
        d.point((11 - inset, y), fill=shade)
    d.line([(4, 14), (11, 14)], fill=outline)
    d.line([(3, 4), (12, 4)], fill=outline)
    d.rectangle([4, 5, 11, 6], fill=fill + (255,))
    d.arc([3, 0, 12, 7], 180, 360, fill=outline)
    img.save(os.path.join(ASSETS, f"textures/item/{name}.png"))
bucket((150, 175, 205), "methane_bucket"); bucket((120, 180, 255), "liquid_oxygen_bucket")
for n in ("methane_bucket", "liquid_oxygen_bucket"):
    write(os.path.join(ASSETS, f"models/item/{n}.json"),
          {"parent": "minecraft:item/generated", "textures": {"layer0": f"rocketatmosphere:item/{n}"}})

# ------------------------------------------------------------------ lang, loot, receita, tags
en = {"block.rocketatmosphere.raptor_3": "Raptor 3",
      "block.rocketatmosphere.methane": "Liquid Methane",
      "block.rocketatmosphere.liquid_oxygen": "Liquid Oxygen (LOX)",
      "item.rocketatmosphere.methane_bucket": "Liquid Methane Bucket",
      "item.rocketatmosphere.liquid_oxygen_bucket": "Liquid Oxygen (LOX) Bucket",
      "fluid.rocketatmosphere.methane": "Liquid Methane",
      "fluid.rocketatmosphere.liquid_oxygen": "Liquid Oxygen (LOX)",
      "itemGroup.rocketatmosphere.main": "Create: Rocket Atmosphere",
      "tooltip.rocketatmosphere.raptor.methane": "Methane (CH4): %1$s / %2$s mB",
      "tooltip.rocketatmosphere.raptor.lox": "Liquid oxygen (LOX): %1$s / %2$s mB"}
pt = dict(en)
pt.update({"block.rocketatmosphere.methane": "Metano Liquido", "block.rocketatmosphere.liquid_oxygen": "Oxigenio Liquido (LOX)",
           "item.rocketatmosphere.methane_bucket": "Balde de Metano Liquido",
           "item.rocketatmosphere.liquid_oxygen_bucket": "Balde de Oxigenio Liquido (LOX)",
           "fluid.rocketatmosphere.methane": "Metano Liquido", "fluid.rocketatmosphere.liquid_oxygen": "Oxigenio Liquido (LOX)",
           "tooltip.rocketatmosphere.raptor.methane": "Metano (CH4): %1$s / %2$s mB",
           "tooltip.rocketatmosphere.raptor.lox": "Oxigenio liquido (LOX): %1$s / %2$s mB"})
write(os.path.join(ASSETS, "lang/en_us.json"), en)
write(os.path.join(ASSETS, "lang/pt_br.json"), pt)

write(os.path.join(DATA, "rocketatmosphere/loot_table/blocks/raptor_3.json"), {
    "type": "minecraft:block", "random_sequence": "rocketatmosphere:blocks/raptor_3",
    "pools": [{"rolls": 1.0, "bonus_rolls": 0.0,
               "entries": [{"type": "minecraft:item", "name": "rocketatmosphere:raptor_3"}],
               "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
write(os.path.join(DATA, "minecraft/tags/block/mineable/pickaxe.json"), {"values": ["rocketatmosphere:raptor_3"]})
write(os.path.join(DATA, "rocketatmosphere/tags/fluid/methane.json"), {"values": [
    "rocketatmosphere:methane", "rocketatmosphere:flowing_methane", {"id": "northstar:methane", "required": False}]})
write(os.path.join(DATA, "rocketatmosphere/tags/fluid/lox.json"), {"values": [
    "rocketatmosphere:liquid_oxygen", "rocketatmosphere:flowing_liquid_oxygen"]})
write(os.path.join(DATA, "rocketatmosphere/recipe/raptor_3.json"), {
    "type": "minecraft:crafting_shaped", "pattern": ["NFN", "PTP", "IBI"],
    "key": {"N": {"item": "minecraft:netherite_ingot"}, "F": {"item": "create:fluid_pipe"},
            "P": {"item": "create:mechanical_pump"}, "T": {"item": "createpropulsion:thruster"},
            "I": {"item": "create:iron_sheet"}, "B": {"item": "create:brass_casing"}},
    "result": {"id": "rocketatmosphere:raptor_3"}})

print(f"modelo: {len(elements)} elementos")

# ------------------------------------------------------------------ pre-visualizacao lado a lado com a foto
if "--preview" in sys.argv:
    ref_path = sys.argv[sys.argv.index("--preview") + 1] if len(sys.argv) > sys.argv.index("--preview") + 1 else None
    H = 705
    s = H / 34.0
    W = int(16 * s)
    img = Image.new("RGB", (W, H), (210, 214, 218))
    d = ImageDraw.Draw(img)
    for e, tex in elements:
        x0, y0, z0 = e["from"]; x1, y1, z1 = e["to"]
        pts = [(x0, y0), (x1, y0), (x1, y1), (x0, y1)]
        ang = e.get("rotation", {}).get("angle", 0)
        if ang:
            o = e["rotation"]["origin"]; c, sn = math.cos(math.radians(ang)), math.sin(math.radians(ang))
            pts = [(o[0] + (x - o[0]) * c - (y - o[1]) * sn, o[1] + (x - o[0]) * sn + (y - o[1]) * c) for x, y in pts]
        xs = [p[0] for p in pts]
        base = COL[tex]
        left, right = int(min(xs) * s), int(max(xs) * s)
        top, bot = int((z0 + 1.0) * s), int((z1 + 1.0) * s)
        for xx in range(left, right):                    # sombreamento cilindrico simples
            t = (xx - left) / max(1, right - left - 1)
            k = 0.75 + 0.5 * (1 - abs(2 * t - 1))
            d.line([(xx, top), (xx, bot)], fill=tuple(min(255, int(v * k)) for v in base))
    if ref_path and os.path.exists(ref_path):
        ref = Image.open(ref_path).convert("RGB")
        ref = ref.resize((int(ref.width * H / ref.height), H))
        out = Image.new("RGB", (ref.width + W + 30, H), (255, 255, 255))
        out.paste(ref, (0, 0)); out.paste(img, (ref.width + 30, 0))
    else:
        out = img
    out.save(os.path.join(ROOT, "tools/raptor3-preview.png"))
    print("preview salvo")
