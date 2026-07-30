# Spottio Reports Module

Il modulo `reports` è responsabile della gestione delle segnalazioni (report) all'interno dell'applicazione Spottio. Permette agli utenti di segnalare contenuti inappropriati e fornisce un pannello di amministrazione per moderare e gestire queste segnalazioni. È stato migrato completamente a **Jetpack Compose**, eliminando la dipendenza dai file di layout XML e dai RecyclerView Adapter tradizionali.

---

## 🏗️ Architettura e Struttura dei Package

Il modulo segue i principi architetturali Modern Android Development (MAD) con una chiara separazione tra strato dati e strato UI:

```text
com.example.spottio.reports
│
├── data/                       # Livello Dati
│   └── Report.kt               # Modello dati che rispecchia la struttura su Firestore
│
└── presentation/               # Livello UI (Jetpack Compose)
    ├── AdminReportsActivity.kt # Activity principale del pannello di moderazione
    └── ReportUiComponents.kt   # Componenti Compose (Dialog di segnalazione, Lista, Singolo Item)
```

*(Nota: a seconda delle dimensioni del progetto, i file potrebbero trovarsi direttamente sotto il package `reports`, ma questa struttura logica riflette la suddivisione delle responsabilità).*

---

## 🧩 Descrizione dei Componenti

### Strato Dati

*   **`Report.kt`**: Una `data class` che definisce i campi di una segnalazione. Viene mappata direttamente sui documenti della collection `reports` su Firebase Firestore. Contiene informazioni critiche come l'ID del post segnalato, l'autore originale, l'utente che ha effettuato la segnalazione, il motivo, una descrizione opzionale e il testo del post (per facilitare la moderazione senza ulteriori query). Sfrutta le annotazioni `@JvmField`, `@JvmOverloads` ed `@Exclude` per interoperabilità con Firebase.

### Strato Presentazione (UI)

*   **`AdminReportsActivity.kt`**: L'Activity (o in futuro, una rotta Navigation) che funge da *Pannello Segnalazioni* per gli amministratori.
    *   Sostituisce il vecchio approccio basato su `RecyclerView` e `Adapter`.
    *   Mantiene uno stato reattivo della lista delle segnalazioni utilizzando `mutableStateListOf<Report>()`.
    *   Gestisce la logica di business per caricare i dati (ascoltando i cambiamenti in tempo reale su Firestore tramite `addSnapshotListener`), ignorare una segnalazione (eliminandola dal DB) o eliminare il post offensivo (che rimuove in cascata il post e la relativa segnalazione).

*   **`ReportUiComponents.kt`**: Un file centrale che raccoglie tutti i `@Composable` necessari, sostituendo i precedenti file XML (`dialog_report.xml`, `activity_admin_reports.xml`, `item_report.xml`).
    *   `ReportDialog`: Una finestra modale usata nell'app principale per consentire a un utente di segnalare un post. Include un menu a tendina per il motivo e un campo di testo per i dettagli.
    *   `AdminReportsScreen`: Il layout principale del pannello di moderazione. Include l'intestazione e una `LazyColumn` per elencare i report.
    *   `ReportItem`: L'equivalente in Compose del vecchio `item_report.xml`. Mostra i dettagli di una singola segnalazione in una `Card` (motivo, chi segnala, chi è segnalato, testo del post) e fornisce i pulsanti "Ignora" e "Elimina Post". Mostra anche un `AlertDialog` di conferma prima dell'eliminazione.

---

## 🚀 Funzionalità Principali

1.  **Invio Segnalazioni**: Tramite la `ReportDialog`, gli utenti possono inviare facilmente segnalazioni categorizzate.
2.  **Pannello di Moderazione in Tempo Reale**: L'`AdminReportsActivity` si aggiorna istantaneamente quando vengono aggiunte nuove segnalazioni, grazie ai listener in tempo reale di Firestore.
3.  **Gestione Diretta (Ignora o Elimina)**: Gli amministratori possono risolvere le segnalazioni direttamente dall'interfaccia, ignorando i falsi allarmi o rimuovendo i post che violano le linee guida.
4.  **UI 100% Jetpack Compose**: Il modulo è stato modernizzato, passando da un approccio imperativo (XML + RecyclerView) a uno dichiarativo, migliorando la leggibilità, la manutenibilità e la coerenza del design.

---

## 🛠️ Stack Tecnologico

*   **Linguaggio**: Kotlin
*   **UI Toolkit**: Jetpack Compose, Material Design 3
*   **Gestione Stato**: Compose State (`mutableStateListOf`, `mutableStateOf`)
*   **Backend / DB**: Firebase Firestore (con interrogazioni in tempo reale)