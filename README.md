# Słownik Języka Trudnego — Android (Kotlin + Jetpack Compose)

Natywna aplikacja Android dla projektu **Słownik Języka Trudnego**, stworzona z myślą o ergonomii obsługi kciukiem na urządzeniach mobilnych, z zachowaniem 100% funkcji i estetyki wersji Web.

---

## 📱 Funkcje Aplikacji

1. **Dwufazowa Sesja Dzienna (Daily Session)**:
   - **Faza 1 (Prezentacja / Showcase)**: Poznawanie nowych słówek z odsłanianiem zamazanej definicji tapnięciem (`tap-to-reveal`), wymową fonetyczną, etymologią i przykładami w zdaniach.
   - **Faza 2 (Quiz Aktywnego Przypominania)**: 4 warianty odpowiedzi, natychmiastowy feedback wizualny, pełny kontekst i odnośnik do SJP PWN.
   - **Samoocena SM-2**: 4 przyciski oceny (Bardzo słabo [0], Słabo [3], Dobrze [4], Bardzo dobrze [5]). Przy ocenie 0 słówko wraca na koniec sesji.
   - **Ekran podsumowania**: Puchar gratulacyjny, zliczone hasła, licznik serii dni (`Streak`).

2. **Inteligentny Algorytm Spaced Repetition (SuperMemo SM-2)**:
   - Dynamiczny współczynnik łatwości (`easeFactor`), wyznaczanie dat kolejnych powtórek.
   - **Adaptacyjny limit słówek**: Automatyczne zmniejszanie/blokowanie puli nowych słów przy zaległościach powtórkowych, chroniąc przed przeciążeniem poznawczym.

3. **Katalog (Słowniczek)**:
   - Podział na **Poznane Słówka** (z odznakami powtórek) i **Oczekujące w kolejce**.
   - Wyszukiwarka na żywo (po haśle i treści definicji) oraz filtry kategorii.
   - Panel dolny szczegółów słowa (`ModalBottomSheet`).

4. **Statystyki i Analiza**:
   - Licznik serii dni (`Streak`).
   - Wykres słupkowy aktywności z ostatnich 7 dni.
   - Opanowanie słownictwa w podziale na 9 kategorii tematycznych.
   - Lista najbardziej wymagających haseł.

5. **Design System & Dostępność (a11y)**:
   - Paleta barw: **Wyrazista Przydymiona Szałwia** (Sage Green Light & Dark).
   - Typografia: `Libre Baskerville` (nagłówki) + `Inter` (interfejs).
   - Tryb wysokiego kontrastu, 3 poziomy wielkości czcionki (Standardowy, Średni, Duży), redukcja ruchu.
   - Bezpieczny reset postępów z 5-sekundowym odliczaniem.

6. **Offline-First**:
   - Pełna funkcjonalność i zapis postępów lokalnie na urządzeniu.

---

## 🛠️ Otwieranie w Android Studio

1. Uruchom **Android Studio**.
2. Wybierz **File -> Open...** i wskaż katalog:
   `c:\Users\filip\Antigravity\Słownik Języka Trudnego ANDROID`
3. Poczekaj na automatyczną synchronizację projektu Gradle (**Gradle Sync**).
4. Wybierz emulator lub podłączone urządzenie i kliknij **Run 'app'** (▶).

---

## 🏗️ Struktura Kodu

```
app/src/main/java/pl/slownikjezykatrudnego/app/
├── data/
│   ├── datasource/          # Baza 91 wyrafinowanych słów (DictionaryWordsData.kt)
│   ├── model/               # Modele DictionaryWord, UserWordProgress, UserSettings, SessionCard
│   └── repository/          # Zapis SharedPreferences / JSON (PreferencesRepository.kt)
├── domain/
│   ├── SuperMemoEngine.kt   # Algorytm SM-2, wyznaczanie interwałów, kalkulator serii dni
│   └── SessionManager.kt    # Generator sesji dziennej, Mulberry32 PRNG, adaptacyjny limit
├── ui/
│   ├── theme/               # Paleta szałwiowa (Color.kt), Typografia (Type.kt), Motywy (Theme.kt)
│   ├── components/          # Przyciski dotykowe, odznaki, pasek górny i dolny
│   ├── lesson/              # Faza 1 Showcase, Faza 2 Quiz, Podsumowanie sesji
│   ├── catalog/             # Wyszukiwarka, filtr kategorii, arkusz szczegółów hasła
│   ├── stats/               # Wykres 7 dni, statystyki kategorii, trudne słówka
│   ├── settings/            # Ustawienia limitu, motywu, dostępności, reset postępów
│   ├── account/             # Informacja o trybie offline / synchronizacji
│   ├── common/              # Polityka prywatności i formularz kontaktowy
│   ├── SjtViewModel.kt      # Główny stan aplikacji i przepływ lekcji
│   └── SjtApp.kt            # Główny interfejs Scaffold i BottomSheet
├── MainActivity.kt          # Aktywność główna z Edge-to-Edge
└── SjtApplication.kt        # Klasa aplikacji
```
