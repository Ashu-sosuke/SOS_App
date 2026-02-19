package com.example.sos.contactCred

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class TrustedContactsViewModel : ViewModel() {

    private val repo = TrustedContactsRepository()

    private val _contacts = MutableStateFlow<List<TrustedContact>>(emptyList())
    val contacts: StateFlow<List<TrustedContact>> = _contacts

    init {
        repo.listenContacts {
            _contacts.value = it
        }
    }

    fun addContact(name: String, phonePlain: String, relation: String) {

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val normalized = PhoneUtils.normalize(phonePlain)
        val phoneHash = CryptoManager.hash(normalized)

        // Check duplicate in Firestore
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("trusted_contacts")
            .whereEqualTo("phoneHash", phoneHash)
            .get()
            .addOnSuccessListener { snapshot ->

                if (!snapshot.isEmpty) {
                    println("Duplicate phone number detected")
                    return@addOnSuccessListener
                }

                val contact = TrustedContact(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    phone = CryptoManager.encrypt(normalized),
                    phoneHash = phoneHash,
                    relation = relation
                )

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("trusted_contacts")
                    .document(contact.id)
                    .set(contact)
            }
    }


    fun updateContact(contact: TrustedContact) {
        repo.updateContact(contact)
    }

    fun deleteContact(contact: TrustedContact) {
        repo.deleteContact(contact.id)
    }

    fun existsByPhone(plainPhone: String): Boolean {
        return _contacts.value.any {
            CryptoManager.decrypt(it.phone) == plainPhone
        }
    }
}
