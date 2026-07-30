# 👤 Spottio - Modulo Profilo (`com.example.spottio.profile`)

Questo pacchetto contiene l'intera logica di business, l'architettura di navigazione e l'interfaccia grafica per la gestione dei profili utente all'interno dell'applicazione Spottio. Il modulo è progettato per essere dinamico: gestisce sia le funzionalità complete per il profilo personale dell'utente (modifica, impostazioni, logout), sia la visualizzazione ristretta per i profili di terze parti (segui/non segui, invio messaggi).

Il modulo è strutturato seguendo il pattern **MVVM (Model-View-ViewModel)** arricchito da un approccio a **Coordinator** per la navigazione e fa uso di **Jetpack Compose** per il rendering della User Interface, interoperando con View Android classiche per mantenere la retrocompatibilità con gli adapter preesistenti.

---

## 🏗️ Architettura e Struttura dei File

### 1. Interfaccia Grafica (UI)
*   **`ProfileFragment.kt`**: È l'entry point principale del modulo all'interno dell'infrastruttura a Fragment dell'applicazione. Si occupa di:
    *   Inizializzare il `ProfileViewModel`, il `ProfileCoordinator` e il `ProfileUIManager`.
    *   Recuperare l'ID dell'utente loggato tramite `AuthManager` e capire se il profilo da mostrare è il proprio o quello di un altro utente.
    *   Implementare l'interfaccia `PostInteractionListener` per intercettare i click sui post (like, eliminazione, modifica, commento, segnalazione, condivisione e zoom dell'immagine).
    *   Restituire una `ComposeView` che aggancia il layout moderno in Compose al lifecycle del Fragment.
*   **`ProfileScreen.kt`**: È il componente (Composable) radice che definisce l'aspetto visivo della pagina. Include l'header con la foto profilo, nome, spunta di verifica, biografia, e statistiche di following/followers. Utilizza un blocco `AndroidView` per incorporare la `RecyclerView` tradizionale, assicurando che lo scorrimento dei post sia fluido e confinato (`clipToBounds()`, `weight(1f)`) senza causare bug di loop o sovrapposizioni visive.

### 2. Logica di Presentazione (Presentation)
*   **`ProfileViewModel.kt`**: È il motore dati della pagina. Comunica direttamente con **Firebase Firestore** e **FirebaseAuth** per:
    *   Ascoltare in tempo reale i cambiamenti sul documento dell'utente (username, bio, foto, stato di verifica).
    *   Caricare in tempo reale la lista dei post dell'utente, ordinati per timestamp in ordine decrescente.
    *   Gestire le azioni CRUD sul profilo: aggiornamento della biografia, aggiornamento dell'username, aggiornamento della foto profilo (URL).
    *   Gestire la logica per seguire o smettere di seguire un utente, aggiornando simultaneamente gli array `followers` e `following` nel database Firestore.
    *   Esporre i dati all'interfaccia in modo reattivo tramite `LiveData` (convertiti in `State` in Jetpack Compose).
*   **`ProfileCoordinator.kt`**: Estrae tutta la logica di navigazione (Routing) dal Fragment, rendendo il codice più pulito e testabile. Contiene i metodi per:
    *   Determinare se il profilo visualizzato è quello dell'utente corrente tramite la funzione `isMyProfile()`.
    *   Aprire le liste dei followers e dei seguiti passando i dati a `UserConnectionsFragment`.
    *   Avviare una chat privata con l'utente visualizzato tramite `ChatFragment`.
    *   Navigare verso la schermata delle impostazioni (`SettingsFragment`).
    *   Gestire le transizioni verso l'attività per le segnalazioni degli amministratori (`AdminReportsActivity`).
    *   Delegare il cambio lingua o il logout.

### 3. Componenti di Supporto (Components)
*   **`ProfileUIManager.kt`**: Un helper dedicato esclusivamente a snellire la UI da logiche ripetitive. Contiene metodi specializzati per:
    *   Mostrare gli `AlertDialog` per la modifica della Bio, della Foto Profilo e dell'Username, raccogliendo gli input tramite `EditText` e gestendo i Toast di conferma.
    *   Caricare l'immagine del profilo in modo efficiente utilizzando la libreria **Glide** (applicando crop circolari, placeholder, e caching sul disco).
    *   Eseguire il flusso di disconnessione (Logout): pulisce le preferenze locali (`SharedPreferences`), revoca il token di Firebase e reindirizza l'utente alla `MainActivity`.

---

## ⚠️ Note Tecniche e Debito Tecnico Risolto

Durante l'ultimo ciclo di refactoring, il modulo ha subìto una profonda modernizzazione:
*   **Migrazione a Jetpack Compose**: L'intero layout XML e le relative classi di view-binding sono stati eliminati e soppiantati dal singolo file dichiarativo `ProfileScreen.kt`.
*   **Gestione RecyclerView Ibrida**: Poiché l'ecosistema dell'app si basa ancora fortemente su Adapter classici complessi (come `PostAdapter` per Zoom, condivisione e interazioni varie), l'interoperabilità tra Compose e il framework View originale è stata mantenuta. La `RecyclerView` riceve ora limiti nativi di larghezza/altezza e un parametro di peso (`weight(1f)`) combinato con un clipping visivo rigoroso per garantire un'esperienza Edge-to-Edge nativa e corretta all'interno dell'infrastruttura di Android 15.
*   **Aggiornamenti Dinamici**: Lo stato dello scrolling della RecyclerView non subisce più ripristini involontari; l'adapter viene istanziato una sola volta in fase di composizione e viene aggiornato in modo performante invocando `currentAdapter.setPostList()`.