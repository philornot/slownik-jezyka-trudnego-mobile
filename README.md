# Słownik Języka Trudnego — Android

Natywna, nowoczesna aplikacja mobilna na platformę Android służąca do efektywnej nauki
wyrafinowanego słownictwa języka polskiego, oparta na metodzie powtórek w interwałach (Spaced
Repetition).

---

## ✨ Kluczowe Możliwości

- **Dwufazowa Sesja Dzienna**:
    - *Faza Prezentacji*: Poznawanie haseł, fonetyki, etymologii i zdań kontekstowych z funkcją
      interaktywnego odsłaniania znaczenia (*tap-to-reveal*).
    - *Faza Aktywnego Przypominania*: Quiz z 4 wariantami odpowiedzi oraz oceną trudności zgodną z
      algorytmem SM-2.
- **Algorytm SuperMemo SM-2 & Ochrona Poznawcza**:
    - Dynamiczne obliczanie współczynnika łatwości (`easeFactor`) i interwałów powtórek.
    - Adaptacyjny limit nowych słówek chroniący przed przeciążeniem przy zaległościach powtórkowych.
- **Katalog & Wyszukiwarka**:
    - Pełna baza wyselekcjonowanych haseł z podziałem na 9 kategorii tematycznych.
    - Wyszukiwanie w czasie rzeczywistym po haśle oraz treści definicji.
- **Statystyki i Monitorowanie Postępów**:
    - Śledzenie ciągłości nauki (*Streak*), wykres 7-dniowej aktywności i identyfikacja
      najtrudniejszych haseł.
- **Inteligentne Powiadomienia (WorkManager)**:
    - Kontekstowe przypomnienia uwzględniające aktualnie powtarzane słówka, serię dni, liczbę haseł
      w kolejce oraz preferowaną porę dnia.
- **Synchronizacja Chmurowa & Offline-First**:
    - Pełna funkcjonalność bez dostępu do sieci z automatyczną synchronizacją i zarządzaniem sesjami
      wielu urządzeń przez Firebase (Auth & Firestore).
- **Dostępność i Design System**:
    - Estetyka *Sage Green* (jasny i ciemny motyw), typografia `Libre Baskerville` + `Inter`,
      regulacja rozmiaru tekstu, tryb wysokiego kontrastu oraz redukcja ruchu.

---

## 🛠️ Stack Technologiczny

| Warstwa                | Technologie                                                                            |
|------------------------|----------------------------------------------------------------------------------------|
| **Język & Środowisko** | Kotlin, JDK 17, Android SDK 35 (min SDK 26)                                            |
| **UI & Architektura**  | Jetpack Compose, Material 3, Single Activity (Edge-to-Edge), ViewModel + MVI/StateFlow |
| **Pamięć & Usługi**    | SharedPreferences / Kotlinx Serialization, WorkManager                                 |
| **Backend & Sync**     | Firebase Authentication, Cloud Firestore (zabezpieczone regułami bezpieczeństwa)       |
| **Testy**              | JUnit 4, Kotlinx Coroutines Test                                                       |

---

## 📁 Struktura Projektu

```
app/src/main/java/com/philornot/slownikjezykatrudnego/
├── data/
│   ├── datasource/          # Baza haseł słownikowych (DictionaryWordsData.kt)
│   ├── model/               # Modele domenowe i DTO (DictionaryWord, UserWordProgress, itp.)
│   └── repository/          # Zarządzanie danymi lokalnymi i synchronizacją Firebase
├── domain/                  # Silnik SuperMemo SM-2, zarządca sesji (SessionManager.kt)
├── notifications/           # Harmonogram i generator inteligentnych powiadomień (WorkManager)
├── ui/
│   ├── theme/               # Kolorystyka (Sage Green), typografia, motywy
│   ├── components/          # Reużywalne komponenty interfejsu (TopBar, BottomNav, Card)
│   ├── lesson/              # Ekrany sesji nauki, quizu i podsumowania
│   ├── catalog/             # Przeglądarka haseł, filtry i arkusz szczegółów
│   ├── stats/               # Panel analityczny i statystyki nauki
│   ├── settings/            # Ustawienia powiadomień, dostępności i motywu
│   ├── account/             # Logowanie, rejestracja, profil i sesje urządzeń
│   ├── SjtViewModel.kt      # Główny stan aplikacji
│   └── SjtApp.kt            # Główny kontener Scaffold
└── MainActivity.kt          # Punkt wejścia aplikacji
```

---

## 🚀 Budowanie i Testy

```bash
# Uruchomienie testów jednostkowych
./gradlew testDebugUnitTest

# Zbudowanie pakietu instalacyjnego APK
./gradlew assembleDebug
```

