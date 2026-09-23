# SafeCracker

Party igra za grupu prijatelja - igrači otkrivaju šifru sefa pomoću hint kartica, trejdovanja informacija i socijalne dedukcije.

---

## Tech Stack

- **Kotlin** + **Jetpack Compose** - jezik i UI
- **Firebase Realtime Database** - real-time sync između telefona
- **Firebase Anonymous Auth** - automatski jedinstveni ID po igraču bez registracije
- **MVVM arhitektura** - Model / ViewModel / View

---

## Arhitektura

Projekat koristi MVVM pattern. Svaki sloj ima jednu odgovornost i ne zna za slojeve iznad sebe.

```
model/          → čisti podaci (data classes), nema logike
repository/     → jedino mjesto gdje se piše Firebase kod
usecase/        → logika koja se dijeli između više ViewModela
viewmodel/      → logika ekrana, prima podatke iz Repositorya
ui/             → Compose ekrani, samo prikazuje i javlja klikove
ui/components/  → dijelovi UI koji se ponavljaju između ekrana
navigation/     → NavGraph koji prebacuje ekrane na osnovu faze igre
```

### Pravilo koje mora biti jasno

> **UI ne zna da postoji Firebase. Ikad.**

Ako u Composable funkciji vidiš `Firebase.database` - nešto nije u redu. Sav Firebase kod ide isključivo u `repository/`.

### Tok podataka

```
Korisnik klikne nešto
→ View pozove funkciju na ViewModelu
→ ViewModel provjeri logiku i pozove Repository
→ Repository upiše u Firebase
→ Firebase pushuje promjenu svim igračima
→ Repository Flow emituje novu vrijednost
→ ViewModel update-uje uiState
→ Compose se automatski recrta
```

### Firebase struktura

```
rooms/
└── [roomCode]/
    ├── phase: "NIGHT" | "DAY" | "VOTING" | "ATTEMPT" | "ENDED"
    ├── round: 2
    ├── hostId: "uid_abc123"
    ├── players/
    │   └── [playerId]/
    │       ├── name: "Igor"
    │       ├── failedAttempts: 1
    │       ├── isEliminated: false
    │       └── hints/
    │           └── [0..2] → { type: "RED"|"GREEN"|"YELLOW", content: "..." }
    ├── gameRooms/
    │   └── [roomId]/
    │       └── hints/ → hintovi dostupni u toj sobi
    ├── nightChoices/
    │   └── [playerId]: "roomId"   ← skriveno do jutra
    ├── votes/
    │   └── [playerId]: "targetPlayerId" | ""
    └── codeAttempts/
        └── [0..n]/
            ├── playerId: "uid_abc123"
            ├── attempted: "4829"
            └── correctDigits: ["4", "2"]
```

### Ko piše šta u Firebase

| Path | Ko piše |
|------|---------|
| `phase`, `round` | samo host |
| `gameRooms/hints` | samo host (pri generisanju) |
| `players/[mojId]/` | svako piše samo za sebe |
| `nightChoices/[mojId]` | svako piše samo za sebe |
| `votes/[mojId]` | svako piše samo za sebe |
| `codeAttempts/` | aktivni igrač koji pokušava |

---

## Setup za nove developere

### 1. Kloniraj repo

```bash
git clone https://github.com/[username]/safecracker.git
cd safecracker
```

### 2. Preuzmi `google-services.json`

Ovaj fajl **nije u repou** jer sadrži Firebase API ključeve.

1. Idi na [console.firebase.google.com](https://console.firebase.google.com)
2. Otvori projekat **SafeCracker**
3. Project Settings → Your apps → Android app → Preuzmi `google-services.json`
4. Stavi fajl u `app/` folder (ne u subfolder, direktno u `app/`)

```
safecracker/
├── app/
│   ├── google-services.json   ← ovdje
│   └── src/
└── build.gradle.kts
```

### 3. Otvori u Android Studiju

File → Open → izaberi root folder projekta. Sačekaj da Gradle završi sync (može potrajati par minuta prvi put).

### 4. Provjeri da sve radi

Pokreni app na telefonu ili emulatoru. U Logcatu filtriraj po `FIREBASE_TEST` - treba da se pojavi poruka sa UID-om.

---

## Testiranje na fizičkom uređaju (preporučeno)

Fizički telefon je brži i stabilniji od emulatora. Dva načina:

### USB (jednostavnije)

1. Na telefonu idi u **Podešavanja → O telefonu** i klikni 7 puta na **Broj verzije** - aktivira se Developer Options
2. U **Podešavanja → Developer Options** uključi **USB Debugging**
3. Povezi telefon kablom sa računarom
4. U Android Studiju, u gornjem dropdown meniju za uređaje treba da se pojavi tvoj telefon
5. Klikni Run ▶

### Bežično / QR kod (Android 11+)

1. Uključi **Developer Options** kao gore
2. U **Developer Options** uključi **Wireless debugging**
3. U Android Studiju: **Run → Pair Devices Using Wi-Fi**
4. Skeniraj QR kod koji se pojavi u Android Studiju
5. Telefon i računar moraju biti na **istoj Wi-Fi mreži**
6. Nakon prvog pariranja, naredni put se telefon automatski pojavljuje bez QR koda dok je na istoj mreži

> **Napomena:** USB je pouzdaniji za svakodnevni razvoj, QR / bežično je zgodno kad ne želiš kabel.

---

## Podjela rada

Svaka osoba posjeduje **cijeli ekran** - i ViewModel i UI za taj ekran. Ne dijeliti po slojevima.

| Ekran | ViewModel | Opis |
|-------|-----------|------|
| `LobbyScreen` | `LobbyViewModel` | Kreiranje i pridruživanje sobi |
| `NightPhaseScreen` | `NightPhaseViewModel` | Biranje sobe, uzimanje hintova |
| `DayPhaseScreen` | `DayPhaseViewModel` | Pregled hintova, trejdovanje |
| `VotingScreen` | `VotingViewModel` | Glasanje ko ide na sef |
| `SafeAttemptScreen` | `SafeAttemptViewModel` | Unos šifre, prikaz rezultata |

`GameSessionViewModel` je shared - drži globalnu fazu igre i listu igrača, svi ekrani ga čitaju.
