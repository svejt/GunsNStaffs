# Igra za Blaza

## Contributors:
- Svit Logonder
- Dominik Ojo
- David Vasilev
- Jan Lucas Redek

## Tema:
Roguelike/Bullet Hell/Dungeon Crawler (ideja kot [Tiny Rogues](https://store.steampowered.com/app/2088570/Tiny_Rogues/))

## Orodja:
### IDE:
- [VS Code](https://code.visualstudio.com/)
- [IntelliJ IDEA](https://www.jetbrains.com/idea/)

### Sprite editor:
- [Piskel](https://www.piskelapp.com/)
- [Aseprite](https://www.aseprite.org/)

### Audio:
- [JSFXR](https://sfxr.me/)

## Postopek po katerem bomo/smo delali

### Milestone 1: Osnove projekta in premikanje
Cilj: Postaviti projekt in prikazati igralca, ki se premika po zaslonu.

- Nastavitev okolja: Namesti Java JDK, IDE (priporočljivo IntelliJ IDEA) in uporabi LibGDX Setup Tool, da ustvariš nov projekt.

- Prikaz lika: Nariši preprost kvadrat ali 2D sprite (sliko igralca) na zaslonu.

- Nadzor igralca: Poveži tipke WASD ali smerne puščice z gibanjem igralca.

- Omejitev zaslona: Prepreči igralcu, da bi stopil izven vidnega polja kamere.

### Milestone 2: Mehanični temelji (Bullet Hell)
Cilj: Omogočiti streljanje igralca in nasprotnika.

- Streljanje igralca: Ustvari mehaniko, kjer igralec ob kliku miške ali pritisku tipke izstreli projektil v smeri kurzorja.

- Statični nasprotnik: Postavi nepremičnega nasprotnika na zaslon.

- Napad nasprotnika (Bullet Hell): Naredi, da nasprotnik v rednih intervalih strelja projektile v krožnem vzorcu (npr. 8 ali 16 projektilov naenkrat).

- Zaznavanje zadetkov (Collisions): Implementiraj preprosto preverjanje prekrivanja (BoundingBox ali Circle v libGDX), da igralec oz. nasprotnik prejme škodo, ko ga zadane projektil.

### Milestone 3: Sobe in Postopno ustvarjanje (Dungeon Generation)
Cilj: Pretvoriti en sam zaslon v sistem sob.

- Mreža sob (Grid): Ustvari preprost sistem sobe (zaprt prostor z zidovi).

- Zaznavanje zidov: Dodaj fiziko ali preproste trke (collisions), da se igralec ne more premikati skozi zidove.

- Proceduralna generacija sob (Poenostavljena): Ustvari kodo, ki naključno poveže 5–10 sob z vrati (npr. z uporabo preprostega algoritma Random Walk ali mrežnega sistema).

- Prehod med sobami: Ko igralec premaga vse nasprotnike v sobi, se vrata odprejo in prehod v naslednjo sobo ga premakne na novo lokacijo.

### Milestone 4: Roguelike sistemi (Napredovanje in Oprema)
Cilj: Dodati elemente naključnosti, nadgradenj in opreme.

- Sistem zdravja (HP) in Smrt: Ko HP pade na 0, se igra konča (Game Over) in igralec se vrne na začetek (Permanent Death).

- Pobiranje predmetov (Loot): Nasprotniki ob smrti odvržejo kovance, izkušnje (XP) ali orožje.

- Raznolikost orožij: Ustvari razred za orožje, ki določa hitrost napada, škodo in vzorec streljanja (npr. samostrel vs. čarobna palica).

- Niveliranje (Leveling & Perks): Ko igralec nabere dovolj XP, se igra zaustavi in ponudi izbiro med 3 naključnimi pasivnimi bonusi (npr. +10% hitrost, +1 projektil).

### Milestone 5: Raznolikost nasprotnikov in Šefi (Bosses)
Cilj: Ustvariti dinamičen in izzivalen potek igre.

- Umetna inteligenca (AI) nasprotnikov: Ustvari različne tipe sovražnikov (npr. tisti, ki se le zaletavajo v igralca, in tisti, ki streljajo iz daljave).

- Šef na koncu nadstropja (Boss Fight): Implementiraj večjega nasprotnika z več stopnjami napada (phase 1, phase 2) in kompleksnejšimi vzorci projektilov.

### Milestone 6: Uporabniški vmesnik (UI) in Poliranje
Cilj: Pretvoriti prototip v pravo igro.

- HUD (Heads-Up Display): Uporabi libGDX Scene2D za prikaz HP traku, trenutnega orožja, stope XP in izbrani sobi/nadstropju.

- Meni in Začasna zaustavitev (Pause): Dodaj glavni meni (Start, Exit) in meni za premor.

- Zvočni učinki in Glasba: Dodaj zvoke za streljanje, zadetke, pobiranje predmetov in ozadno glasbo.
