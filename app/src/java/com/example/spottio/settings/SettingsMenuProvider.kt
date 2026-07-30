package com.example.spottio.settings

enum class SettingType {
    PASSWORD, CAMBIO_EMAIL, COLORE_SFONDO, SCARICA_APK, PRIVACY_PROFILO, ESPORTA_DATI, STORIA, INFORMATIVA_PRIVACY, POLICY_COMMUNITY, ELIMINA_ACCOUNT
}

data class SettingItem(
    val title: String,
    val type: SettingType
)

object SettingsMenuProvider {
    fun getMenuItems(): List<SettingItem> {
        return listOf(
            SettingItem("Cambio Password", SettingType.PASSWORD),
            SettingItem("Cambio Email", SettingType.CAMBIO_EMAIL),
            SettingItem("Colore di Sfondo", SettingType.COLORE_SFONDO),
            SettingItem("Scarica App (APK)", SettingType.SCARICA_APK),
            SettingItem("Privacy Profilo", SettingType.PRIVACY_PROFILO),
            SettingItem("Esporta Dati", SettingType.ESPORTA_DATI),
            SettingItem("Storia", SettingType.STORIA),
            SettingItem("Informativa Privacy", SettingType.INFORMATIVA_PRIVACY),
            SettingItem("Policy della Community", SettingType.POLICY_COMMUNITY),
            SettingItem("Elimina Account", SettingType.ELIMINA_ACCOUNT)
        )
    }
}