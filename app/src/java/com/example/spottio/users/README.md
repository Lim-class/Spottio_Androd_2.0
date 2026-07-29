# Spottio Users Module

Il modulo `users` è responsabile della gestione, ricerca e visualizzazione degli utenti all'interno dell'applicazione Spottio. È strutturato seguendo i principi della **Clean Architecture** e del pattern **MVVM**, con la UI sviluppata in **Jetpack Compose** all'interno di standard Android Fragments.

---

## 🏗️ Architettura e Struttura dei Package

Per garantire la *Separation of Concerns (SoC)*, facilitare i test e la scalabilità, il codice è organizzato in strati: `data` e `presentation`.

```text
com.example.spottio.users
│
├── data/                       # Dati, Caching e Sorgenti Dati (Firestore)
│   ├── model/
│   │   └── User.kt             # Data class principale (Mappa documento Firestore)
│   ├── local/
│   │   └── UserCache.kt        # Caching locale basato su LruCache
│   └── repository/
│       └── UserRepository.kt   # Gestione query a Firestore
│
└── presentation/               # Strato UI (Jetpack Compose, Fragments, ViewModels)
    ├── search/
    │   ├── SearchUsersFragment.kt    # Schermata di Ricerca Utenti
    │   └── SearchUsersViewModel.kt   # Gestione stato (StateFlow) per la ricerca
    ├── connections/
    │   └── UserConnectionsFragment.kt# Schermata Connessioni (Follower/Following)
    └── components/
        └── UserUiComponents.kt       # Componenti UI condivisi (es. UserRow, UserListScreen)
```

---

## 🧩 Descrizione dei Componenti

### Strato `data`

*   **`User.kt`**: Modello dati che rispecchia la struttura nel database cloud (Firestore). Contiene identificativi, informazioni di profilo (bio, interessi), metriche sociali (follower, following) e flag di stato (isAdmin).
*   **`UserCache.kt`**: Un singleton che implementa una cache LRU (Least Recently Used) in memoria. È fondamentale per evitare letture Firestore ridondanti quando si visualizzano lunghe liste di utenti (es. nei commenti o nelle connessioni). Offre il recupero asincrono dei dati base dell'utente (avatar, nome, verifica).
*   **`UserRepository.kt`**: Centralizza tutte le chiamate esterne (Firebase Firestore). Attualmente implementa l'algoritmo di ricerca in tempo reale degli utenti basato sull'inizio dello username.

### Strato `presentation`

*   **`SearchUsersFragment.kt` & `SearchUsersViewModel.kt`**: Permettono agli utenti di cercare altri account sulla piattaforma. Il ViewModel espone query ed esiti tramite `StateFlow`, supportando debounce e reattività. Il Fragment collega il ViewModel all'UI in Compose e gestisce la navigazione verso i profili selezionati.
*   **`UserConnectionsFragment.kt`**: Un Fragment generico progettato per mostrare liste pre-caricate di utenti, come i Follower o i Following di uno specifico account.
*   **`UserUiComponents.kt`**: Contiene i `@Composable` riutilizzabili:
    *   `UserRow`: Il blocco costitutivo per le liste utenti. Integra automaticamente `UserCache` per mostrare avatar (tramite Coil) e nome, mostrando il caricamento se i dati non sono in cache.
    *   `UserListScreen`: Un layout di pagina completo con `LazyColumn`, intestazione, opzionale barra di ricerca e divisori, utilizzato per standardizzare l'aspetto delle liste.

---

## 🚀 Funzionalità Principali

1.  **Ricerca Reattiva**: Risultati aggiornati mentre l'utente digita, ottimizzati per mostrare fino a 10 risultati alla volta.
2.  **Caching Aggressivo della UI**: L'architettura fa forte affidamento su `UserCache` per rendere il caricamento delle liste immediato e ridurre il numero di documenti letti dal DB, risparmiando larghezza di banda e costi.
3.  **UI Moderna e Dinamica**: Uso esclusivo di **Jetpack Compose** per la renderizzazione delle view, supportando dinamicamente Temi Chiari/Scuri (`MaterialTheme`).
4.  **Integrazione Ibrida**: Il modulo utilizza l'interoperabilità di Compose (`ComposeView`) all'interno dell'ecosistema storico Android basato su Fragment Manager.
5.  **Gestione Badges**: Supporto visivo nativo per mostrare account verificati (`isVerified`).

---

## 🛠️ Stack Tecnologico

*   **Linguaggio**: Kotlin
*   **UI Toolkit**: Jetpack Compose, Material 3, Coil (AsyncImage)
*   **Architettura**: MVVM, Clean Architecture, Repository Pattern
*   **Gestione Stato/Concorrenza**: Kotlin Coroutines, StateFlow
*   **Backend / DB**: Firebase Firestore