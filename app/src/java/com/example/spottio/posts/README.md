# Spottio — Posts & Media Sharing Module

Il modulo **Posts & Media** di **Spottio** è un'infrastruttura client Android per la gestione, la creazione, l'archiviazione e la paginazione di post multimediali all'interno della piattaforma.

---

## 🚀 Caratteristiche Principali

- 📸 **Supporto Multi-Media**: Caricamento contestuale di immagini e video (fino a 10 elementi per post) tramite l'API `ActivityResultContracts.GetMultipleContents()`.
- ☁️ **Integrazione Cloudinary**: Upload asincrono dei file multimediali su cloud tramite `CloudinaryHelper` prima della persistenza del post.
- 🏷️ **Gestione Dinamica delle Categorie**:
    - Selezione e creazione in tempo reale di nuove categorie con tag dinamici (Material `ChipGroup` / `Chip`).
    - Sincronizzazione automatica della collezione `categories` su Firebase Firestore.
- 🔄 **Paginazione e Repository Pattern**:
    - Paginazione efficiente dei post con `Query.startAfter(lastVisible)` e `DocumentSnapshot`.
    - Gestione atomica di like, commenti, modifiche testo e segnalazioni post.
- ⚡ **Coroutines & Asincronismo**: Utilizzo di `lifecycleScope`, `Dispatchers.Main` e `await()` per l'esecuzione reattiva e non bloccante delle operazioni di I/O.

---

## 📐 Architettura e Struttura del Codice

Il modulo segue le linee guida di **Clean Architecture** e pattern **MVVM**, suddividendo le responsabilità tra modelli di dati, repository e componenti UI:

```text
com.example.spottio
├── posts
│   ├── create
│   │   ├── AddPostFragment.kt      # Fragment di creazione e pubblicazione post
│   │   └── MediaPreviewAdapter.kt   # Adapter RecyclerView per la preview media in fase di creazione
│   └── shared
│       ├── data
│       │   ├── MediaItem.kt         # Data model per i media (URL + flag video)
│       │   └── Post.kt              # Data model principale del Post (compatibile Firestore)
│       └── repository
│           └── PostRepository.kt    # Data layer per interazione con Firebase Firestore
```

---

## 🛠️ Tech Stack & Librerie

- **Linguaggio**: Kotlin
- **Database & Backend**: [Firebase Firestore](https://firebase.google.com/docs/firestore) (Post, Categorie, Segnalazioni)
- **Media Storage**: Cloudinary (gestito via `CloudinaryHelper`)
- **Immagini & Preview**: [Glide](https://github.com/bumptech/glide)
- **UI & Material Design**: Material Components (`Chip`, `ChipGroup`, `AutoCompleteTextView`), ViewPager2, RecyclerView
- **Asincronismo**: Kotlin Coroutines (`kotlinx-coroutines-play-services`, `lifecycleScope`)

---

## 📄 Dettaglio Modelli Dati

### `Post`
Rappresenta un singolo post all'interno del feed.

```kotlin
data class Post(
    var user: String = "",
    var author: String? = null,
    var text: String? = null,
    var mediaUri: String? = null,       // Campo legacy per retrocompatibilità
    var isVideo: Boolean = false,       // Campo legacy per retrocompatibilità
    var mediaList: MutableList<MediaItem> = mutableListOf(),
    var categories: MutableList<String> = mutableListOf(),
    var timestamp: Date = Date(),
    var comments: MutableList<Comment> = mutableListOf(),
    var likes: MutableList<String> = mutableListOf()
)
```

### `MediaItem`
Rappresenta un singolo elemento multimediale all'interno di un post.

```kotlin
data class MediaItem(
    var url: String = "",
    var isVideo: Boolean = false
) : Serializable
```

---

## ⚙️ Principali Componenti

### 1. `PostRepository`
Gestisce tutte le operazioni Firestore correlate ai post:
- `getUserInterests(uid: String)`: Recupera la mappa degli interessi dell'utente.
- `getPosts(limit: Long, startAfter: DocumentSnapshot?)`: Recupera i post paginati in ordine cronologico decrescente.
- `toggleLike(postId, likesList)`: Aggiorna l'elenco dei `likes`.
- `deletePost(postId, onSuccess)`: Elimina un post dal database.
- `updatePostText(postId, newText, onSuccess)`: Modifica il testo di un post esistente.
- `updateComments(postId, comments, onSuccess)`: Aggiorna l'elenco dei commenti.
- `submitReport(report, onSuccess, onFailure)`: Invia una segnalazione alla collection `reports`.

### 2. `AddPostFragment`
Gestisce la UI di creazione dei post:
- Selezione file dalla galleria con controllo limite massimo (max 10 file).
- Preview orizzontale dei media selezionati tramite `MediaPreviewAdapter` e Glide.
- Gestione dinamica dei tag/categorie con aggiunta automatica su Firestore.
- Upload sequenziale asincrono su Cloudinary ed inserimento finale su Firestore.

---

## 📋 Requisiti & Setup

1. **Configurazione Firebase**:
    - Assicurati che il file `google-services.json` sia integrato nella directory `app/`.
    - Collezioni Firestore richieste: `posts`, `categories`, `users`, `reports`.

2. **Configurazione Cloudinary**:
    - Assicurati che `CloudinaryHelper` sia configurato con le chiavi d'accesso ed il preset corretto per l'upload dei media.

---

## 📜 Licenza

Progetto ad uso interno per l'applicazione **Spottio**. Tutti i diritti riservati.