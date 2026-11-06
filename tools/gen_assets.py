#!/usr/bin/env python3
"""Gera modelos, texturas, blockstates, lang, loot, receitas e tags de TODOS os motores e fluidos
(assets em src/main/resources). Uso: python3 tools/gen_assets.py [--preview FOTO_RAPTOR]  (precisa de Pillow)."""
import json, math, os, random, shutil, sys
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src/main/resources")
ASSETS = os.path.join(RES, "assets/rocketatmosphere")
DATA = os.path.join(RES, "data")
random.seed(3)

for d in (ASSETS, os.path.join(DATA, "rocketatmosphere")):
    shutil.rmtree(d, ignore_errors=True)

def write(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        if isinstance(obj, str): f.write(obj)
        else: json.dump(obj, f, indent=2, ensure_ascii=False)

# ------------------------------------------------------------------ paleta / atlas 16x16 (amostras 4x4)
SW = {'black': (0, 0), 'black2': (1, 0), 'steel': (2, 0), 'steeld': (3, 0),
      'gun': (0, 1), 'gunl': (1, 1), 'white': (2, 1), 'teal': (3, 1),
      'copper': (0, 2), 'copperd': (1, 2), 'pipe': (2, 2), 'dark': (3, 2)}
BASE = {'black': (24, 24, 28), 'black2': (38, 38, 44), 'steel': (150, 156, 168), 'steeld': (96, 102, 114),
        'gun': (62, 67, 78), 'gunl': (94, 100, 112), 'white': (236, 236, 240), 'teal': (38, 172, 178),
        'copper': (178, 112, 66), 'copperd': (122, 74, 46), 'pipe': (30, 30, 35), 'dark': (6, 6, 8)}
PAL = {
    'raptor_3': {}, 'raptor_vacuum': {},
    'merlin_1d': {'black': (62, 64, 74), 'black2': (88, 90, 102), 'steel': (180, 184, 194), 'copper': (206, 150, 72), 'copperd': (150, 104, 50)},
    'rs_25': {'black': (150, 112, 72), 'black2': (112, 84, 54), 'copper': (186, 110, 60), 'steel': (176, 180, 190), 'gun': (74, 78, 90)},
    'f_1': {'black': (38, 38, 44), 'black2': (58, 58, 66), 'gun': (84, 88, 98), 'steeld': (112, 116, 126)},
    'be_4': {'black': (30, 36, 52), 'black2': (46, 54, 74), 'gun': (60, 70, 92)},
    'rd_180': {'black': (96, 74, 62), 'black2': (70, 54, 46), 'copper': (190, 120, 70), 'gun': (88, 86, 92)},
    'rutherford': {'black': (18, 18, 22), 'black2': (30, 30, 36), 'copper': (190, 120, 66)},
    'rl10': {'black': (196, 156, 74), 'black2': (150, 118, 56), 'steel': (190, 194, 202), 'copper': (190, 110, 60)},
}

def palette(engine):
    c = dict(BASE); c.update(PAL.get(engine, {})); return c

def atlas(col):
    img = Image.new("RGBA", (16, 16), (90, 90, 96, 255))
    px = img.load()
    for name, (c, r) in SW.items():
        base = col[name]
        for y in range(4):
            for x in range(4):
                n = random.randint(-5, 5) if name not in ('white', 'dark') else random.randint(-2, 2)
                px[c * 4 + x, r * 4 + y] = tuple(max(0, min(255, v + n)) for v in base) + (255,)
    return img

def uv(name):
    c, r = SW[name]
    return [c * 4, r * 4, c * 4 + 4, r * 4 + 4]

# ------------------------------------------------------------------ geometria (eixo do motor = Z; topo em z=0, bocal ate z=32)
ANG = {1: [0], 2: [0, 45], 4: [0, 22.5, 45, -22.5]}
DIV = {1: 1.0, 2: 1.25, 4: 1.36}      # raio medio da uniao de quadrados girados = a * DIV

class Model:
    def __init__(self, engine):
        self.engine = engine
        self.el = []                    # (elemento, textura)

    @staticmethod
    def _faces(tex, caps, cap_tex):
        f = {s: {"uv": uv(tex), "texture": "#0"} for s in ("east", "west", "up", "down")}
        if caps[0]: f["north"] = {"uv": uv(cap_tex or tex), "texture": "#0"}
        if caps[1]: f["south"] = {"uv": uv(cap_tex or tex), "texture": "#0"}
        return f

    def box(self, x0, y0, z0, x1, y1, z1, tex, name, caps=(True, True), cap_tex=None, rot=None, origin=None):
        e = {"name": name, "from": [round(x0, 3), round(y0, 3), round(z0, 3)], "to": [round(x1, 3), round(y1, 3), round(z1, 3)],
             "faces": self._faces(tex, caps, cap_tex)}
        if rot:
            e["rotation"] = {"angle": rot, "axis": "z", "origin": origin or [8, 8, round((z0 + z1) / 2, 3)]}
        self.el.append((e, tex))

    def ring(self, cx, cy, z0, z1, r, tex, name, n=4, caps=(True, True), cap_tex=None):
        a = r / DIV[n]
        for k, ang in enumerate(ANG[n]):
            self.box(cx - a, cy - a, z0, cx + a, cy + a, z1, tex, f"{name}_{k}", caps, cap_tex,
                     rot=ang if ang else None, origin=[cx, cy, round((z0 + z1) / 2, 3)])

    def frame(self, cx, cy, z0, z1, r, wall, tex, name):
        a = r / DIV[4]
        for k, ang in enumerate(ANG[4]):
            o = [cx, cy, round((z0 + z1) / 2, 3)]
            rr = ang if ang else None
            self.box(cx - a, cy + a - wall, z0, cx + a, cy + a, z1, tex, f"{name}_t{k}", rot=rr, origin=o)
            self.box(cx - a, cy - a, z0, cx + a, cy - a + wall, z1, tex, f"{name}_b{k}", rot=rr, origin=o)
            self.box(cx - a, cy - a + wall, z0, cx - a + wall, cy + a - wall, z1, tex, f"{name}_l{k}", rot=rr, origin=o)
            self.box(cx + a - wall, cy - a + wall, z0, cx + a, cy + a - wall, z1, tex, f"{name}_r{k}", rot=rr, origin=o)

    def bell(self, cx, cy, z0, z1, r0, r1, n, tex, name, power=0.5, alt=None, last_cap=False):
        """Sino: n aneis de z0 a z1, raio r0->r1 (perfil parabolico). `alt` alterna a textura (tubulacao)."""
        zs = [z0 + (z1 - z0) * i / n for i in range(n + 1)]
        for i in range(n):
            zm = (zs[i] + zs[i + 1]) / 2
            r = r0 + (r1 - r0) * ((zm - z0) / (z1 - z0)) ** power
            t = alt if (alt and i % 2 == 1) else tex
            last = last_cap and i == n - 1
            self.ring(cx, cy, zs[i], zs[i + 1], r, t, f"{name}_{i}", caps=(True, last), cap_tex='dark' if last else None)

def raptor_top(m, pump_end=9.7):
    m.ring(8, 8, 0.0, 2.2, 2.6, 'steel', 'top_cap')
    m.ring(8, 8, 2.2, 3.0, 4.0, 'steeld', 'top_flange')
    m.ring(8, 8, 3.0, pump_end, 3.6, 'gun', 'pump_body', caps=(False, False))
    m.ring(8, 8, 5.2, 5.8, 4.3, 'gunl', 'pump_band', caps=(False, False))
    m.ring(3.7, 8.5, 3.4, 8.0, 1.9, 'steel', 'tp_left', n=2)
    m.ring(11.2, 4.4, 3.6, 7.4, 1.5, 'gun', 'tp_low', n=2)
    m.ring(12.2, 9.4, 5.0, 6.6, 1.1, 'teal', 'valve_teal', n=2)
    m.ring(12.2, 9.4, 4.6, 5.0, 1.2, 'steel', 'valve_teal_cap', n=2)
    m.box(5.5, 12.0, 5.0, 7.5, 14.0, 8.0, 'gunl', 'valve_a')
    m.box(8.8, 12.3, 5.2, 10.8, 14.0, 7.6, 'steel', 'valve_b')
    m.box(4.2, 3.2, 3.6, 5.2, 4.2, 6.4, 'copper', 'sensor_a')
    m.box(6.6, 2.6, 3.6, 7.6, 3.6, 6.0, 'copperd', 'sensor_b')

def big_pipe(m, z_end=15.0):
    m.box(13.4, 7.1, 3.4, 15.0, 8.9, z_end, 'pipe', 'pipe_run')
    m.box(10.6, 7.1, 3.4, 13.4, 8.9, 5.2, 'pipe', 'pipe_top')
    m.box(10.6, 7.1, z_end - 1.8, 13.4, 8.9, z_end, 'pipe', 'pipe_bottom')

def build_raptor_3():
    m = Model('raptor_3'); raptor_top(m)
    m.ring(8, 8, 9.7, 10.9, 5.4, 'black', 'wide_flange')
    m.ring(8, 8, 10.9, 18.5, 3.0, 'black2', 'neck', caps=(False, False))
    m.ring(8, 8, 12.6, 13.1, 3.9, 'steeld', 'neck_band', caps=(False, False))
    m.ring(8, 8, 15.7, 16.7, 3.9, 'black', 'neck_torus', caps=(False, False))
    m.bell(8, 8, 18.5, 30.4, 3.6, 6.9, 12, 'black', 'bell', last_cap=True)
    m.ring(8, 8, 18.2, 18.8, 4.5, 'black2', 'bell_flange', caps=(False, False))
    m.frame(8, 8, 30.4, 32.0, 7.2, 1.0, 'black2', 'lip')
    big_pipe(m)
    m.box(14.6, 7.55, 26.0, 14.9, 8.35, 28.6, 'white', 'one_stem')
    m.box(14.6, 8.35, 26.0, 14.9, 9.0, 26.8, 'white', 'one_flag')
    return m, 32.0

def build_raptor_vacuum():
    m = Model('raptor_vacuum'); raptor_top(m)
    m.ring(8, 8, 9.7, 10.9, 5.4, 'black', 'wide_flange')
    m.ring(8, 8, 10.9, 14.0, 3.0, 'black2', 'neck', caps=(False, False))
    m.ring(8, 8, 12.3, 12.9, 3.9, 'steeld', 'neck_band', caps=(False, False))
    m.bell(8, 8, 14.0, 22.0, 3.5, 6.2, 6, 'black', 'bell_up')
    m.bell(8, 8, 22.0, 30.4, 6.3, 7.5, 8, 'steel', 'bell_ext', power=0.7, last_cap=True)
    m.ring(8, 8, 24.0, 24.5, 7.4, 'steeld', 'rib_a', caps=(False, False))
    m.ring(8, 8, 27.0, 27.5, 7.6, 'steeld', 'rib_b', caps=(False, False))
    m.frame(8, 8, 30.4, 32.0, 7.8, 1.0, 'steeld', 'lip')
    big_pipe(m, 14.0)
    return m, 32.0

def build_merlin():
    m = Model('merlin_1d')
    m.ring(8, 8, 0.0, 2.0, 2.2, 'steel', 'top_cap'); m.ring(8, 8, 2.0, 2.7, 3.4, 'steeld', 'top_flange')
    m.ring(8, 8, 2.7, 9.0, 3.2, 'gun', 'pump', caps=(False, False))
    m.ring(3.6, 6.6, 3.0, 8.2, 1.6, 'steel', 'turbopump', n=2)
    m.ring(12.6, 9.5, 3.4, 7.6, 1.2, 'copper', 'gg_exhaust', n=2)
    m.box(6.0, 11.5, 3.4, 10.0, 13.4, 7.8, 'gunl', 'plumbing')
    m.box(10.4, 3.4, 3.6, 12.0, 5.0, 7.0, 'copperd', 'valve')
    m.ring(8, 8, 9.0, 9.8, 3.8, 'black', 'chamber_flange')
    m.ring(8, 8, 9.8, 15.0, 2.3, 'steeld', 'chamber', caps=(False, False))
    m.ring(8, 8, 11.0, 11.5, 3.0, 'copper', 'chamber_band', caps=(False, False))
    m.bell(8, 8, 15.0, 26.0, 2.7, 5.0, 8, 'black', 'bell')
    m.bell(8, 8, 26.0, 32.0, 5.0, 5.9, 4, 'copper', 'extension', last_cap=True)
    return m, 32.0

def build_rs25():
    m = Model('rs_25')
    m.ring(8, 8, 0.0, 2.0, 2.4, 'steel', 'top')
    m.ring(4.4, 10.8, 0.5, 7.0, 2.0, 'steel', 'preburner_l', n=2)
    m.ring(11.6, 10.8, 0.5, 7.0, 2.0, 'steel', 'preburner_r', n=2)
    m.ring(8, 8, 2.0, 9.0, 3.8, 'gun', 'pump_body', caps=(False, False))
    m.box(2.4, 6.8, 4.0, 4.0, 9.2, 15.0, 'steel', 'duct_l')
    m.box(12.0, 6.8, 4.0, 13.6, 9.2, 15.0, 'steel', 'duct_r')
    m.ring(8, 8, 9.0, 11.0, 5.0, 'steeld', 'manifold')
    m.ring(8, 8, 11.0, 15.0, 3.2, 'black2', 'chamber', caps=(False, False))
    m.bell(8, 8, 15.0, 30.4, 3.4, 7.3, 10, 'black', 'bell', alt='copper', last_cap=True)
    m.frame(8, 8, 30.4, 32.0, 7.5, 1.0, 'steeld', 'lip')
    return m, 32.0

def build_f1():
    m = Model('f_1')
    m.ring(8, 8, 0.0, 2.5, 3.6, 'steeld', 'top')
    m.ring(8, 8, 2.5, 9.0, 4.8, 'gun', 'turbopump', caps=(False, False))
    m.ring(10.5, 4.0, 3.0, 8.0, 2.2, 'gunl', 'pump2', n=2)
    m.ring(5.0, 12.0, 3.5, 7.5, 1.8, 'steel', 'pump3', n=2)
    m.ring(8, 8, 9.0, 11.0, 6.6, 'steeld', 'exhaust_manifold')
    m.ring(8, 8, 11.0, 16.0, 3.4, 'black2', 'chamber', caps=(False, False))
    m.bell(8, 8, 16.0, 24.0, 3.8, 6.2, 6, 'black', 'bell_up')
    m.ring(8, 8, 19.5, 23.5, 7.0, 'gun', 'exhaust_skirt')
    m.bell(8, 8, 24.0, 30.4, 6.4, 7.2, 5, 'steel', 'extension', power=0.8, last_cap=True)
    m.frame(8, 8, 30.4, 32.0, 7.5, 1.0, 'gun', 'lip')
    return m, 32.0

def build_be4():
    m = Model('be_4')
    m.ring(8, 8, 0.0, 2.0, 2.6, 'steel', 'top_cap'); m.ring(8, 8, 2.0, 2.8, 4.0, 'steeld', 'top_flange')
    m.ring(8, 8, 2.8, 10.0, 4.2, 'gun', 'pump_body', caps=(False, False))
    m.ring(12.8, 8.0, 2.5, 9.5, 2.4, 'gunl', 'big_pump', n=2); m.ring(12.8, 8.0, 2.0, 2.5, 2.6, 'steel', 'big_pump_cap', n=2)
    m.box(5.0, 12.2, 4.0, 8.0, 14.2, 8.0, 'gunl', 'valve_block')
    m.box(5.2, 2.2, 4.0, 6.4, 3.6, 7.0, 'copper', 'sensor')
    m.ring(8, 8, 10.0, 11.2, 5.2, 'black', 'wide_flange')
    m.ring(8, 8, 11.2, 17.0, 3.1, 'black2', 'neck', caps=(False, False))
    m.ring(8, 8, 14.0, 14.8, 3.8, 'steeld', 'neck_band', caps=(False, False))
    m.bell(8, 8, 17.0, 30.4, 3.7, 6.9, 10, 'black', 'bell', last_cap=True)
    m.frame(8, 8, 30.4, 32.0, 7.2, 1.0, 'black2', 'lip')
    return m, 32.0

def build_rd180():
    m = Model('rd_180')
    m.box(2.5, 4.5, 1.5, 13.5, 11.5, 9.0, 'gun', 'pump_block')
    m.box(3.5, 3.5, 2.5, 12.5, 12.5, 5.5, 'gunl', 'pump_cap')
    m.ring(8, 8, 0.0, 1.5, 2.0, 'steel', 'inlet')
    for i, cx in enumerate((4.2, 11.8)):
        m.ring(cx, 8, 9.0, 9.8, 2.7, 'steeld', f'flange{i}')
        m.ring(cx, 8, 9.8, 14.0, 1.9, 'black2', f'chamber{i}', caps=(False, False))
        m.bell(cx, 8, 14.0, 30.4, 2.2, 3.7, 8, 'black', f'bell{i}', last_cap=True)
        m.frame(cx, 8, 30.4, 32.0, 3.8, 0.7, 'black2', f'lip{i}')
    m.box(6.8, 6.8, 5.6, 9.2, 9.2, 9.0, 'copper', 'link')
    return m, 32.0

def build_rutherford():
    m = Model('rutherford')
    m.ring(8, 8, 0.0, 2.0, 1.8, 'steel', 'cap'); m.ring(8, 8, 2.0, 8.0, 2.4, 'steeld', 'pump', caps=(False, False))
    m.ring(8, 8, 4.0, 5.0, 2.8, 'teal', 'pump_band', caps=(False, False))
    m.box(10.2, 6.5, 2.5, 12.4, 9.5, 7.5, 'gunl', 'battery')
    m.ring(8, 8, 8.0, 12.0, 1.5, 'copper', 'chamber', caps=(False, False))
    m.bell(8, 8, 12.0, 22.6, 1.7, 3.2, 6, 'black', 'bell', last_cap=True)
    m.frame(8, 8, 22.6, 24.0, 3.5, 0.6, 'black2', 'lip')
    return m, 24.0

def build_rl10():
    m = Model('rl10')
    m.ring(8, 8, 0.0, 2.0, 2.2, 'steel', 'cap'); m.ring(8, 8, 2.0, 5.0, 2.8, 'gun', 'pump', caps=(False, False))
    m.ring(4.5, 7.5, 2.2, 5.5, 1.2, 'copper', 'turbo', n=2)
    m.box(10.2, 6.8, 2.6, 12.0, 8.6, 5.0, 'gunl', 'valve')
    m.ring(8, 8, 5.0, 6.0, 3.2, 'steeld', 'flange')
    m.ring(8, 8, 6.0, 9.0, 1.6, 'black2', 'chamber', caps=(False, False))
    m.bell(8, 8, 9.0, 19.0, 1.9, 4.2, 7, 'black', 'bell_a')
    m.bell(8, 8, 19.0, 30.4, 4.2, 6.4, 6, 'steel', 'bell_ext', power=0.7, last_cap=True)
    m.frame(8, 8, 30.4, 32.0, 6.6, 0.8, 'steeld', 'lip')
    return m, 32.0

ENGINES = [  # id, nome (en), nome (pt), builder, tier da receita
    ('raptor_3', 'Raptor 3', 'Raptor 3', build_raptor_3, 'big'),
    ('raptor_vacuum', 'Raptor Vacuum', 'Raptor Vacuum', build_raptor_vacuum, 'big'),
    ('merlin_1d', 'Merlin 1D', 'Merlin 1D', build_merlin, 'mid'),
    ('rs_25', 'RS-25', 'RS-25', build_rs25, 'mid'),
    ('f_1', 'F-1', 'F-1', build_f1, 'big'),
    ('be_4', 'BE-4', 'BE-4', build_be4, 'mid'),
    ('rd_180', 'RD-180', 'RD-180', build_rd180, 'big'),
    ('rutherford', 'Rutherford', 'Rutherford', build_rutherford, 'small'),
    ('rl10', 'RL10', 'RL10', build_rl10, 'small'),
]

DISPLAY = {
    "gui": {"rotation": [80, 0, 55], "translation": [0, 3.5, 0], "scale": [0.45, 0.45, 0.45]},
    "fixed": {"rotation": [80, 0, 55], "translation": [0, 3.5, 0], "scale": [0.45, 0.45, 0.45]},
    "ground": {"translation": [0, 2, 0], "scale": [0.3, 0.3, 0.3]},
    "thirdperson_righthand": {"rotation": [80, 0, 0], "translation": [0, 3, 1], "scale": [0.25, 0.25, 0.25]},
    "thirdperson_lefthand": {"rotation": [80, 0, 0], "translation": [0, 3, 1], "scale": [0.25, 0.25, 0.25]},
    "firstperson_righthand": {"rotation": [80, 0, 30], "translation": [0, 3, 0], "scale": [0.3, 0.3, 0.3]},
    "firstperson_lefthand": {"rotation": [80, 0, 30], "translation": [0, 3, 0], "scale": [0.3, 0.3, 0.3]}}

ROT = {"north": {}, "south": {"y": 180}, "west": {"y": 270}, "east": {"y": 90}, "up": {"x": 270}, "down": {"x": 90}}
models = {}
for eid, en, pt_, builder, tier in ENGINES:
    m, zend = builder()
    for (e, _) in m.el:
        for v in (e["from"], e["to"]):
            assert all(-16 <= c <= 32 for c in v), (eid, e["name"], v)
    models[eid] = (m, zend)
    write(os.path.join(ASSETS, f"models/block/{eid}.json"), {
        "credit": f"{en} - gerado por tools/gen_assets.py", "ambientocclusion": False,
        "textures": {"0": f"rocketatmosphere:block/{eid}", "particle": f"rocketatmosphere:block/{eid}"},
        "elements": [e for e, _ in m.el], "display": DISPLAY})
    write(os.path.join(ASSETS, f"models/item/{eid}.json"), {"parent": f"rocketatmosphere:block/{eid}"})
    os.makedirs(os.path.join(ASSETS, "textures/block"), exist_ok=True)
    atlas(palette(eid)).save(os.path.join(ASSETS, f"textures/block/{eid}.png"))
    variants = {}
    for face, r in ROT.items():           # mesmas rotacoes do thruster do Propulsion (FACING=north, bocal para +Z)
        for multi in ("false", "true"):
            variants[f"facing={face},multi={multi}"] = dict({"model": f"rocketatmosphere:block/{eid}"}, **r)
    write(os.path.join(ASSETS, f"blockstates/{eid}.json"), {"variants": variants})
    print(f"{eid}: {len(m.el)} elementos, bocal ate z={zend}")

# ------------------------------------------------------------------ fluidos e baldes
FLUIDS = [  # nome, cor, en, pt
    ('methane', (140, 165, 195), 'Liquid Methane', 'Metano Líquido'),
    ('liquid_oxygen', (115, 175, 255), 'Liquid Oxygen (LOX)', 'Oxigênio Líquido (LOX)'),
    ('rp1', (176, 132, 52), 'RP-1 (Rocket Kerosene)', 'RP-1 (Querosene de Foguete)'),
    ('liquid_hydrogen', (205, 232, 255), 'Liquid Hydrogen', 'Hidrogênio Líquido'),
]

def fluid_tex(base, name, flow):
    img = Image.new("RGBA", (16, 16)); px = img.load()
    for y in range(16):
        for x in range(16):
            n = random.randint(-10, 10)
            wave = int(8 * math.sin((x + (y if flow else 0)) * 0.9))
            px[x, y] = tuple(max(0, min(255, v + n + wave)) for v in base) + (215,)
    img.save(os.path.join(ASSETS, f"textures/block/{name}.png"))

def bucket(fill, name):
    os.makedirs(os.path.join(ASSETS, "textures/item"), exist_ok=True)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); d = ImageDraw.Draw(img)
    outline, body, shade = (50, 52, 60, 255), (170, 175, 185, 255), (120, 124, 134, 255)
    for y in range(4, 15):
        inset = (y - 4) // 4
        d.line([(3 + inset, y), (12 - inset, y)], fill=body)
        d.point((3 + inset, y), fill=outline); d.point((12 - inset, y), fill=outline); d.point((11 - inset, y), fill=shade)
    d.line([(4, 14), (11, 14)], fill=outline); d.line([(3, 4), (12, 4)], fill=outline)
    d.rectangle([4, 5, 11, 6], fill=fill + (255,)); d.arc([3, 0, 12, 7], 180, 360, fill=outline)
    img.save(os.path.join(ASSETS, f"textures/item/{name}.png"))

for fname, color, en, pt_ in FLUIDS:
    fluid_tex(color, f"{fname}_still", False); fluid_tex(color, f"{fname}_flow", True)
    bucket(color, f"{fname}_bucket")
    write(os.path.join(ASSETS, f"models/item/{fname}_bucket.json"),
          {"parent": "minecraft:item/generated", "textures": {"layer0": f"rocketatmosphere:item/{fname}_bucket"}})
    write(os.path.join(ASSETS, f"models/block/{fname}.json"), {"textures": {"particle": f"rocketatmosphere:block/{fname}_still"}})
    write(os.path.join(ASSETS, f"blockstates/{fname}.json"), {"variants": {"": {"model": f"rocketatmosphere:block/{fname}"}}})

# ------------------------------------------------------------------ lang
en = {"itemGroup.rocketatmosphere.main": "Create: Rocket Atmosphere",
      "tooltip.rocketatmosphere.engine.propellant": "%1$s: %2$s / %3$s mB"}
pt = dict(en)
for eid, nen, npt, _, _ in ENGINES:
    en[f"block.rocketatmosphere.{eid}"] = nen; pt[f"block.rocketatmosphere.{eid}"] = npt
for fname, _, nen, npt in FLUIDS:
    en[f"block.rocketatmosphere.{fname}"] = nen; pt[f"block.rocketatmosphere.{fname}"] = npt
    en[f"fluid.rocketatmosphere.{fname}"] = nen; pt[f"fluid.rocketatmosphere.{fname}"] = npt
    en[f"item.rocketatmosphere.{fname}_bucket"] = f"{nen} Bucket"; pt[f"item.rocketatmosphere.{fname}_bucket"] = f"Balde de {npt}"
write(os.path.join(ASSETS, "lang/en_us.json"), en)
write(os.path.join(ASSETS, "lang/pt_br.json"), pt)

# ------------------------------------------------------------------ loot, receitas, tags
RECIPES = {
    'big': (["NFN", "PTP", "IBI"], {"N": "minecraft:netherite_ingot"}),
    'mid': (["DFD", "PTP", "IBI"], {"D": "minecraft:diamond"}),
    'small': (["IFI", "PTP", "IBI"], {}),
}
for eid, _, _, _, tier in ENGINES:
    write(os.path.join(DATA, f"rocketatmosphere/loot_table/blocks/{eid}.json"), {
        "type": "minecraft:block", "random_sequence": f"rocketatmosphere:blocks/{eid}",
        "pools": [{"rolls": 1.0, "bonus_rolls": 0.0,
                   "entries": [{"type": "minecraft:item", "name": f"rocketatmosphere:{eid}"}],
                   "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    pattern, extra = RECIPES[tier]
    key = {"F": {"item": "create:fluid_pipe"}, "P": {"item": "create:mechanical_pump"},
           "T": {"item": "createpropulsion:thruster"}, "I": {"item": "create:iron_sheet"},
           "B": {"item": "create:brass_casing"}}
    for k, v in extra.items(): key[k] = {"item": v}
    write(os.path.join(DATA, f"rocketatmosphere/recipe/{eid}.json"), {
        "type": "minecraft:crafting_shaped", "pattern": pattern, "key": key,
        "result": {"id": f"rocketatmosphere:{eid}"}})
write(os.path.join(DATA, "minecraft/tags/block/mineable/pickaxe.json"),
      {"values": [f"rocketatmosphere:{e[0]}" for e in ENGINES]})
write(os.path.join(DATA, "rocketatmosphere/tags/fluid/methane.json"), {"values": [
    "rocketatmosphere:methane", "rocketatmosphere:flowing_methane", {"id": "northstar:methane", "required": False}]})
write(os.path.join(DATA, "rocketatmosphere/tags/fluid/lox.json"), {"values": [
    "rocketatmosphere:liquid_oxygen", "rocketatmosphere:flowing_liquid_oxygen"]})
write(os.path.join(DATA, "rocketatmosphere/tags/fluid/rp1.json"), {"values": [
    "rocketatmosphere:rp1", "rocketatmosphere:flowing_rp1"]})
write(os.path.join(DATA, "rocketatmosphere/tags/fluid/lh2.json"), {"values": [
    "rocketatmosphere:liquid_hydrogen", "rocketatmosphere:flowing_liquid_hydrogen"]})

# ------------------------------------------------------------------ pre-visualizacao (folha de contato + comparacao do Raptor 3)
def render(m, scale, H, bg=(210, 214, 218)):
    W = int(16 * scale)
    img = Image.new("RGB", (W, H), bg); d = ImageDraw.Draw(img)
    col = palette(m.engine)
    for e, tex in m.el:
        x0, y0, z0 = e["from"]; x1, y1, z1 = e["to"]
        pts = [(x0, y0), (x1, y0), (x1, y1), (x0, y1)]
        ang = e.get("rotation", {}).get("angle", 0)
        if ang:
            o = e["rotation"]["origin"]; c, sn = math.cos(math.radians(ang)), math.sin(math.radians(ang))
            pts = [(o[0] + (x - o[0]) * c - (y - o[1]) * sn, o[1] + (x - o[0]) * sn + (y - o[1]) * c) for x, y in pts]
        xs = [p[0] for p in pts]; base = col[tex]
        left, right = int(min(xs) * scale), int(max(xs) * scale)
        top, bot = int((z0 + 1.0) * scale), int((z1 + 1.0) * scale)
        for xx in range(left, right):
            t = (xx - left) / max(1, right - left - 1)
            k = 0.75 + 0.5 * (1 - abs(2 * t - 1))
            d.line([(xx, top), (xx, bot)], fill=tuple(min(255, int(v * k)) for v in base))
    return img

if "--preview" in sys.argv:
    i = sys.argv.index("--preview")
    ref_path = sys.argv[i + 1] if len(sys.argv) > i + 1 else None
    H = 705; s = H / 34.0
    img = render(models['raptor_3'][0], s, H)
    if ref_path and os.path.exists(ref_path):
        ref = Image.open(ref_path).convert("RGB"); ref = ref.resize((int(ref.width * H / ref.height), H))
        out = Image.new("RGB", (ref.width + img.width + 30, H), (255, 255, 255))
        out.paste(ref, (0, 0)); out.paste(img, (ref.width + 30, 0))
    else:
        out = img
    out.save(os.path.join(ROOT, "tools/raptor3-preview.png"))
    # folha de contato com todos os motores
    cell_w, cell_h, sc = 210, 430, 11.0
    cols = 5
    rows = (len(ENGINES) + cols - 1) // cols
    sheet = Image.new("RGB", (cols * cell_w, rows * cell_h), (250, 250, 250)); sd = ImageDraw.Draw(sheet)
    for n, (eid, en_, _, _, _) in enumerate(ENGINES):
        r, c = divmod(n, cols)
        im = render(models[eid][0], sc, int(34 * sc))
        sheet.paste(im, (c * cell_w + (cell_w - im.width) // 2, r * cell_h + 28))
        sd.text((c * cell_w + 12, r * cell_h + 8), en_, fill=(20, 20, 20))
    sheet.save(os.path.join(ROOT, "tools/engines-preview.png"))
    print("previews salvos")
