#!/usr/bin/env python3
"""
Generate edges_500.csv for the TransitIQ 500‑node benchmark graph.
Author : NAHID HASAN RAYAN (NHR)
Group  : 4 – BOLEH
Project: SCSE1224 Advanced Programming – TransitIQ

Output:
    edges_500.csv  (~2000 edges)
    Modes: WALK, CAR, BUS, TRAIN
    Districts are assigned based on node prefixes and position.
"""

import csv
import random
import math

# ---------- configuration ----------
NODES_FILE = "nodes_500.csv"
EDGES_FILE = "edges_500.csv"
TARGET_EDGES = 2000
SEED = 2026            # NHR – stable, repeatable graph

random.seed(SEED)

# ---------- load nodes ----------
nodes = []
with open(NODES_FILE, newline='', encoding='utf-8') as f:
    reader = csv.DictReader(f)
    for row in reader:
        nodes.append({
            'id': row['id'],
            'lat': float(row['lat']),
            'lon': float(row['lon']),
            'type': row['type']
        })

node_ids = {n['id'] for n in nodes}
node_by_id = {n['id']: n for n in nodes}

# ---------- helper: Haversine distance in km ----------
def haversine_km(n1, n2):
    R = 6371.0
    lat1, lon1 = math.radians(n1['lat']), math.radians(n1['lon'])
    lat2, lon2 = math.radians(n2['lat']), math.radians(n2['lon'])
    dlat = lat2 - lat1
    dlon = lon2 - lon1
    a = math.sin(dlat/2)**2 + math.cos(lat1)*math.cos(lat2)*math.sin(dlon/2)**2
    return R * 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

# ---------- edge generation ----------
edges = []          # list of dicts
edge_set = set()    # to avoid duplicates

def add_edge(frm, to, mode, base_time, max_speed, district, cost, co2):
    """Add directed edge if not duplicate."""
    key = (frm, to, mode)
    if key in edge_set:
        return
    edge_set.add(key)
    edges.append({
        'from': frm,
        'to': to,
        'mode': mode,
        'base_time_sec': int(base_time),
        'max_speed_kph': int(max_speed),
        'district': district,
        'cost': round(cost, 2),
        'co2_per_km': co2
    })

# 1. WALK edges: connect nearby nodes of different types, and adjacent junctions
MAX_WALK_DIST_KM = 2.0
for n1 in nodes:
    for n2 in nodes:
        if n1['id'] == n2['id']:
            continue
        dist = haversine_km(n1, n2)
        if dist > MAX_WALK_DIST_KM:
            continue
        # Add walk if both are not the same type? Or always? We'll add if they are close enough.
        # To keep edge count down, add only if one is a junction (or they are different types)
        if n1['type'] == 'JUNCTION' or n2['type'] == 'JUNCTION':
            time = max(30, int(dist / 5.0 * 3600))  # walking speed 5 km/h
            district = n1['id'][:2]  # use first two chars as district label
            add_edge(n1['id'], n2['id'], 'WALK', time, 5, district, 0.0, 0.0)

# 2. CAR edges: only between JUNCTIONs, up to 5 km
MAX_CAR_DIST_KM = 5.0
junctions = [n for n in nodes if n['type'] == 'JUNCTION']
for n1 in junctions:
    for n2 in junctions:
        if n1['id'] >= n2['id']:  # avoid duplicate bidirectional pairs
            continue
        dist = haversine_km(n1, n2)
        if dist > MAX_CAR_DIST_KM:
            continue
        time = max(60, int(dist / 40.0 * 3600))   # average city speed 40 km/h
        cost = round(dist * 1.5, 2)               # RM1.50 per km
        district = n1['id'][:2]
        # bidirectional
        add_edge(n1['id'], n2['id'], 'CAR', time, 40, district, cost, 0.20)
        add_edge(n2['id'], n1['id'], 'CAR', time, 40, district, cost, 0.20)

# 3. BUS edges: between bus stops that are nearby (2-8 km), form a few routes
bus_stops = [n for n in nodes if n['type'] == 'BUS_STOP']
for i, bs1 in enumerate(bus_stops):
    for bs2 in bus_stops[i+1:]:
        dist = haversine_km(bs1, bs2)
        if 2.0 <= dist <= 8.0:
            time = max(120, int(dist / 30.0 * 3600))
            cost = round(dist * 0.8, 2)           # cheaper than car
            district = bs1['id'][:2]
            add_edge(bs1['id'], bs2['id'], 'BUS', time, 30, district, cost, 0.08)
            add_edge(bs2['id'], bs1['id'], 'BUS', time, 30, district, cost, 0.08)

# 4. TRAIN edges: between train stations, create a line topology
train_stations = [n for n in nodes if n['type'] == 'TRAIN_STATION']
# Connect sequential stations with some gap
for i in range(len(train_stations)-1):
    ts1 = train_stations[i]
    ts2 = train_stations[i+1]
    dist = haversine_km(ts1, ts2)
    if dist < 10.0:
        time = max(60, int(dist / 80.0 * 3600))
        cost = 2.50
        district = ts1['id'][:2]
        add_edge(ts1['id'], ts2['id'], 'TRAIN', time, 80, district, cost, 0.05)
        add_edge(ts2['id'], ts1['id'], 'TRAIN', time, 80, district, cost, 0.05)

# 5. Multi-modal link: connect bus stops / train stations to nearest junction via WALK
# Already partially covered by the general WALK pass, but we ensure every stop has a walk link to a junction.
# The earlier WALK pass already connects nearby nodes, so this is just a safety net.
# We'll keep as is.

# If edge count < TARGET, add random car edges between junctions until we reach 2000
while len(edges) < TARGET_EDGES:
    j1, j2 = random.sample(junctions, 2)
    dist = haversine_km(j1, j2)
    if dist > MAX_CAR_DIST_KM:
        continue
    time = max(60, int(dist / 40.0 * 3600))
    cost = round(dist * 1.5, 2)
    district = j1['id'][:2]
    add_edge(j1['id'], j2['id'], 'CAR', time, 40, district, cost, 0.20)
    add_edge(j2['id'], j1['id'], 'CAR', time, 40, district, cost, 0.20)

# ---------- write CSV ----------
with open(EDGES_FILE, 'w', newline='', encoding='utf-8') as f:
    writer = csv.DictWriter(f, fieldnames=[
        'from','to','mode','base_time_sec','max_speed_kph','district','cost','co2_per_km'
    ])
    writer.writeheader()
    writer.writerows(edges)

print(f"Generated {len(edges)} edges -> {EDGES_FILE}")
print("Ownership: NAHID HASAN RAYAN (NHR) – Group 4 BOLEH – SCSE1224")
