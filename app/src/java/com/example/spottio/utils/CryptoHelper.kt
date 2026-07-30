package com.example.spottio.utils

import android.util.Base64
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoHelper {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val SYSTEM_MASTER_KEY = "Spottio_Master_SecretKey_2026_SecureKey!#"

    private fun getChatSecretKey(chatId: String): String {
        return "${SYSTEM_MASTER_KEY}_$chatId"
    }

    /**
     * Replica esatta dell'algoritmo OpenSSL EVP_BytesToKey usato di default da CryptoJS.
     * Genera la vera chiave AES e l'IV (Initialization Vector) combinando la password e il salt.
     */
    private fun evpKDF(password: ByteArray, salt: ByteArray, keySize: Int, ivSize: Int): Pair<ByteArray, ByteArray> {
        val digest = MessageDigest.getInstance("MD5")
        var current = ByteArray(0)
        val result = ByteArrayOutputStream()

        while (result.size() < keySize + ivSize) {
            digest.update(current)
            digest.update(password)
            digest.update(salt)
            current = digest.digest()
            result.write(current)
        }

        val keyIv = result.toByteArray()
        val key = Arrays.copyOfRange(keyIv, 0, keySize)
        val iv = Arrays.copyOfRange(keyIv, keySize, keySize + ivSize)

        return Pair(key, iv)
    }

    fun encrypt(message: String, conversationId: String): String {
        if (message.isBlank()) return message
        return try {
            val passwordBytes = getChatSecretKey(conversationId).toByteArray(Charsets.UTF_8)

            // 1. Genera 8 byte di salt casuale (esattamente come fa JS)
            val salt = ByteArray(8)
            SecureRandom().nextBytes(salt)

            // 2. Deriva la Chiave (32 byte) e l'IV (16 byte)
            val (key, iv) = evpKDF(passwordBytes, salt, 32, 16)

            // 3. Cripta il messaggio
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
            val encryptedBytes = cipher.doFinal(message.toByteArray(Charsets.UTF_8))

            // 4. Concatena: "Salted__" (8 byte) + salt (8 byte) + messaggio criptato
            val saltedMagic = "Salted__".toByteArray(Charsets.US_ASCII)
            val output = ByteArrayOutputStream()
            output.write(saltedMagic)
            output.write(salt)
            output.write(encryptedBytes)

            // 5. Codifica tutto in Base64 (il risultato inizierà magicamente con "U2FsdGVkX1")
            Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            message
        }
    }

    fun decrypt(encryptedMessage: String, conversationId: String): String {
        if (encryptedMessage.isBlank()) return encryptedMessage

        return try {
            val decodedBytes = Base64.decode(encryptedMessage, Base64.DEFAULT)
            val saltedMagic = "Salted__".toByteArray(Charsets.US_ASCII)

            // Controlla se il messaggio ha la firma JS "Salted__"
            val hasSalt = decodedBytes.size > 16 && Arrays.equals(Arrays.copyOfRange(decodedBytes, 0, 8), saltedMagic)

            if (hasSalt) {
                // 1. Estrai il salt e il vero testo criptato
                val salt = Arrays.copyOfRange(decodedBytes, 8, 16)
                val cipherText = Arrays.copyOfRange(decodedBytes, 16, decodedBytes.size)

                // 2. Ricostruisci la stessa Chiave e IV partendo dalla password e dal salt recuperato
                val passwordBytes = getChatSecretKey(conversationId).toByteArray(Charsets.UTF_8)
                val (key, iv) = evpKDF(passwordBytes, salt, 32, 16)

                // 3. Decripta
                val cipher = Cipher.getInstance(ALGORITHM)
                cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
                val decryptedBytes = cipher.doFinal(cipherText)

                String(decryptedBytes, Charsets.UTF_8)
            } else {
                // Se non c'è il salt (es. testo non criptato o vecchi messaggi di test), restituiscilo così com'è
                encryptedMessage
            }
        } catch (e: Exception) {
            e.printStackTrace()
            encryptedMessage
        }
    }
}