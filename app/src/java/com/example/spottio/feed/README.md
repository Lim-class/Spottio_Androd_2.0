# 📱 Spottio - Feed Module (Android / Jetpack Compose)

Modulo per la gestione del feed social di **Spottio**. Il modulo gestisce la visualizzazione dinamica dei post, il caricamento paginato basato sugli interessi dell'utente, la riproduzione multimediale (immagini e video), l'integrazione dei commenti, le interazioni (like, report, modifiche) ed il supporto alle funzioni riservate agli amministratori.

---

## 🏗 Architettura e Integrazione Hybrid (Compose in RecyclerView)

Per garantire prestazioni ottimali nelle liste a scorrimento lungo ed evitare la riscrittura completa dell'infrastruttura `RecyclerView` preesistente, il modulo adotta un approccio **ibrido**:

- **`PostAdapter` (`RecyclerView.Adapter`)**: Mantiene la gestione dei ViewHolder e l'integrazione nativa con il ciclo di vita delle liste Android.
- **`ComposeView`**: Utilizzato come View radice per ciascun ViewHolder.
- **`PostItemContent` / `CommentItem` (`@Composable`)**: Tutta l'interfaccia utente visiva, dai bottoni di reazione alle bolle dei commenti, è implementata in **Jetpack Compose / Material 3**.

---

## 📁 Struttura dei Package e File

```
com.example.spottio.feed
│
├── HomeFragment.kt                 # Fragment principale che ospita la RecyclerView e gestisce il ciclo di vita
├── HomeViewModel.kt                # ViewModel con logica di business, paginazione Firestore ed algoritmo d'interessi
│
├── data/
│   └── Comment.kt                  # Data class serializzabile per i commenti (integrazione Firestore)
│
└── ui/
    ├── adapter/
    │   └── PostAdapter.kt          # Adapter RecyclerView per integrare i Composable item
    │
    ├── components/
    │   ├── CommentItem.kt          # Composable UI per il singolo commento utente
    │   └── PostMediaCarousel.kt    # Carosello multimediale con supporto HorizontalPager (Glide + VideoView)
    │
    ├── dialogs/
    │   └── PostDialogHelper.kt     # Helper statico per Dialog e BottomSheet (Elimina, Modifica, Segnala, Commenti)
    │
    └── screen/
        ├── PostItem.kt             # Bridge Composable (gestisce il caricamento async dei dati utente tramite UserCache)
        └── PostItemContent.kt      # Layout UI puro del post (Header, Media, Testo, Barra Interazioni)
```

---

## 🛠 Componenti Principali

### 1. `HomeViewModel.kt`
- **Paginazione**: Carica i post in blocchi (`LIMIT = 10`) salvando il riferimento all'ultimo `DocumentSnapshot`.
- **Personalizzazione Feed**: Ordina i post in base ai punteggi delle categorie d'interesse (`getUserInterests`) salvati nel profilo utente, ripiegando sul timestamp per le priorità secondarie.
- **Interazioni con Firestore**: Delegato a `PostRepository` per operazioni di like, cancellazione, modifica e segnalazione (`Report`).

### 2. `PostMediaCarousel.kt`
- Gestisce collezioni multi-immagine o video per singolo post tramite `HorizontalPager` di Compose.
- **Integrazione Legacy**: Usa `AndroidView` per incapsulare `Glide` (immagini con supporto zoom) e `VideoView` nativi.
- Mostra il badge contatore pagine (`1 / N`) in sovrimpressione.

### 3. `CommentItem.kt` & `PostDialogHelper.kt`
- I commenti vengono visualizzati all'interno di un `BottomSheetDialog` tramite un'istanza embedded di `ComposeView`.
- Risoluzione asincrona degli utenti tramite `UserCache` (con badge di verifica dinamico).

---

## 🚀 Requisiti e Dipendenze

Il modulo richiede i seguenti componenti già configurati nel progetto:

```groovy
// AndroidX & Jetpack Compose
implementation "androidx.compose.ui:ui:$compose_version"
implementation "androidx.compose.material3:material3:$material3_version"
implementation "androidx.compose.ui:ui-tooling-preview:$compose_version"

// Coil (Caricamento Immagini in Compose)
implementation "io.coil-kt:coil-compose:2.x.x"

// Glide (Caricamento Immagini in AndroidView)
implementation "com.github.bumptech.glide:glide:4.x.x"

// Firebase Firestore
implementation "com.google.firebase:firebase-firestore-ktx"

// Zoomy (Pinch-to-Zoom per immagini)
implementation "com.github.ablanco:zoomy:1.1.0"
```

---

## ⚡ Utilizzo Iniziale

Per integrare il feed all'interno del layout di navigazione (es. `BottomNavigationView` o `Activity` radice):

```kotlin
supportFragmentManager.beginTransaction()
    .replace(R.id.fragment_container, HomeFragment())
    .commit()
```

---

## 📝 Funzionalità Gestite
- [x] **Feed Paginato con Ordinamento per Interessi**
- [x] **Visualizzazione Multi-Media (Immagini & Video)**
- [x] **Zoom sulle Immagini (`Zoomy`)**
- [x] **Like in tempo reale con tracciamento preferenze (`InteractionTracker`)**
- [x] **Gestione Commenti via Bottom Sheet in Compose**
- [x] **Segnalazione Post con motivi predefiniti**
- [x] **Modifica / Eliminazione autorizzata (Autore o Admin)**
- [x] **Condivisione del contenuto tramite Intent di sistema**