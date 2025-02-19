This program is an assistive tool for the board game Twilight Imperium 4.
It currently takes input from the command-line and rolls dice for space combat.
Planned additions: faction choice, more combat phases (space cannon, anti-fighter barrage, bombardment, ground combat), switch to GUI based interraction.  
_____________________________________________________________________________________________________________________________
Compile the program: javac CombatSimulator.java

Run the program: java CombatSimulator (combat modifiers) (ship type) (number of ships) [(ship type) (number of ships)]
_____________________________________________________________________________________________________________________________
Modifiers legend:

-1all: apply -1 to all combat rolls

+1all: apply +1 to all combat rolls

+2all: apply +2 to all combat rolls

+1fighter: apply +1 to all combat rolls made by fighters in this combat

+2fighter: apply +2 to all combat rolls made by fighters in this combat  

+2flagship: apply +2 to all combat rolls made by flagships in this combat

+2mech: apply +2 to all combat rolls made by mechs in this combat
_____________________________________________________________________________________________________________________________
Ships legend:

c: carrier

c2: carrier 2

cr: cruiser

cr2: cruiser 2

d: destroyer

d2: destroyer 2 (or Argent Flight's Strike Wing Alpha 1)

d3: Argent Flight's Strike Wing Alpha 2

dr: dreadnought

dr2: dreadnought 2

dr3: L1Z1X's Super Dreadnought 2

f: fighter

f2: fighter 2 (or Naalu's Hybrid Crystal Fighter 1)

f3: Naalu's Hybrid Crystal Fighter 2

i: infantry

i2: infantry 2 or Sol's Spec Ops 1

i3: Sol's Spec Ops 2

m: mech

z_grav_eidolon: NRA's Space Mech

hil_colish, arc_secundus, son_of_ragh, inferno, dynamo, genesis, 001, arvicon_rex, memoria2, terror_between, ysia_yssrila, salai_sai_corian, duha_menaimon, quetzecoatl, artemiris, wrath_of_kenara, fourth_moon, memoria, ouranos, loncarra_ssodu, matriarch, alastor, van_hauge, cmorran_norr, jns_hylarim, visz_el_vir: flagships
