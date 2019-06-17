package com.mirai.whatsup.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.Auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.mirai.whatsup.entities.*
import com.mirai.whatsup.receycleView.item.*
import com.xwray.groupie.kotlinandroidextensions.Item
import java.util.*

object FireStoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document(
            "users/${FirebaseAuth.getInstance().currentUser?.uid ?: throw NullPointerException("UID is null..")}"
        )

    private val chatChannelCollectionRef = firestoreInstance.collection("chatchannels")
    private val groupeChatCollectionRef = firestoreInstance.collection("chatgroupes")
    private val userCollection = firestoreInstance.collection("users")

    fun initCurrentUserIfFirstTime(fileUrl: String, onComplete: () -> Unit) {
        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                val newUser = User(
                    FirebaseAuth.getInstance().currentUser?.email ?: FirebaseAuth.getInstance().currentUser?.displayName
                    ?: "",
                    FirebaseAuth.getInstance().currentUser?.phoneNumber ?: "",
                    fileUrl ?: ""
                )
                currentUserDocRef.set(newUser).addOnSuccessListener { onComplete() }
            } else
                onComplete()
        }
    }

    fun updateCurrentUser(name: String = "", bio: String = "", profilPicturePath: String? = null) {
        val userFieldMap = mutableMapOf<String, Any>()
        if (name.isNotBlank()) userFieldMap["name"] = name
        if (bio.isNotBlank()) userFieldMap["bio"] = bio
        if (profilPicturePath != null)
            userFieldMap["profilePicturePath"] = profilPicturePath
        currentUserDocRef.update(userFieldMap)
    }

    fun getCurrentUser(onComplete: (User) -> Unit) {
        currentUserDocRef.get()
            .addOnSuccessListener {
                onComplete(it.toObject(User::class.java)!!)
            }
    }


    // cette méthode permet de recupérer la liste des utilisateurs
/*    fun addUserListener(context: Context, onListen: (List<Item>) -> Unit): ListenerRegistration {
        return firestoreInstance.collection("users")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FIRESTORE", "Users listener error?", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()
                querySnapshot?.documents?.forEach {
                    if (it.id != FirebaseAuth.getInstance().currentUser?.uid)
                        items.add(PersonItem(it.toObject(User::class.java)!!, it.id, context))
                }

                onListen(items)
            }
    }*/

    /* cette méthode permet de rechercher les utilisateurs suivant deux criteres:
    * le username, le status*/

    fun addSearchUserListener(
        valeurRecherche: String,
        context: Context,
        onListen: (List<Item>) -> Unit
    ): ListenerRegistration {
        return firestoreInstance.collection("users")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FIRESTORE", "Users listener error?", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()
                querySnapshot?.documents?.forEach {
                    if (it.id != FirebaseAuth.getInstance().currentUser?.uid) {

                        if (!valeurRecherche.isEmpty()) {
                            if (it["name"].toString().toUpperCase().contains(valeurRecherche.toUpperCase())
                                || it["bio"].toString().toUpperCase().contains(valeurRecherche.toUpperCase())
                            ) {
                                items.add(PersonItem(it.toObject(User::class.java)!!, it.id, context))
                                Log.d("FIRESTOREUTIL", "NOUVELLE VALEURE AJOUTTEE !!!")
                            }
                        } else {
                            items.add(PersonItem(it.toObject(User::class.java)!!, it.id, context))

                        }
                    }
                }
                onListen(items)
            }
    }

    fun removeListener(registration: ListenerRegistration) = registration.remove()

    fun getUserByUid(uid: String, onComplete: (user: User) -> Unit) {
        userCollection.document(uid).get().addOnSuccessListener { onComplete(it.toObject(User::class.java)!!) }
    }

    /*
    cette méthode a pour but de creer ou de recupérer la liste de chaines de chat existant
     */
    fun getorcreateChatChannel(
        otheruserId: String,
        onComplete: (channelid: String) -> Unit
    ) {
        currentUserDocRef.collection("engagedChatChannels")
            .document(otheruserId).get().addOnSuccessListener {
                if (it.exists()) {
                    onComplete(it["channelId"] as String)
                    return@addOnSuccessListener
                }

                val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
                val newChannel = chatChannelCollectionRef.document()
                newChannel.set(ChatChannel(mutableListOf(currentUserId, otheruserId)))

                currentUserDocRef.collection("engagedChatChannels")
                    .document(otheruserId)
                    .set(mapOf("channelId" to newChannel.id))

                firestoreInstance.collection("users").document(otheruserId)
                    .collection("engagedChatChannels")
                    .document(currentUserId)
                    .set(mapOf("channelId" to newChannel.id))

                onComplete(newChannel.id)
            }
    }


    /*
    Cette méthode retourne la liste des chat dans une chaine donnée
     */
    fun addChatMessagesListeber(
        channelId: String, context: Context,
        onListner: (List<Item>) -> Unit
    ): ListenerRegistration {
        return chatChannelCollectionRef.document(channelId).collection("messages")
            .orderBy("time")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FIRESTORE", "ChatMessageslistener error.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()
                querySnapshot!!.documents.forEach {
                    if (it["type"] == MessageType.TEXT)
                        items.add(TextMessageItem(it.toObject(TextMessage::class.java)!!, context))
                    else
                        items.add(ImageMessageItem(it.toObject(ImageMessage::class.java)!!, context))
                    return@forEach
                }

                onListner(items)

            }
    }

    fun sendMessage(message: Message, channelId: String) {
        chatChannelCollectionRef.document(channelId)
            .collection("messages")
            .add(message)
    }


    /*
      cette méthode a pour but de creer un groupe de chat
       */

    fun createGroupeChat(
        members: MutableList<String>,
        groupeName: String,
        groupeDescription: String,
        onComplete: (groupeId: String) -> Unit
    ) {

        val newgroupe =
            ChatGroup(
                FirebaseAuth.getInstance().currentUser!!.uid,
                groupeName,
                groupeDescription,
                Date(0),
                "",
                members
            )
        val newChatgroup = groupeChatCollectionRef.document()
        newChatgroup.set(newgroupe).addOnSuccessListener {
            //ajoutons l'id du groupe à l'utilisateur
            currentUserDocRef.collection("groupes").add(mapOf("groupeId" to newChatgroup.id))

            // ajout de l'id du groupe à tous les autres membres sélectionnés pour ke groupe
            for (itemuserId in members) {
                val refCurentuser = firestoreInstance.document(
                    "users/${itemuserId}"
                )
                refCurentuser.collection("groupes").add(mapOf("groupeId" to newChatgroup.id))
            }
            onComplete(newChatgroup.id)
        }
    }

    fun updateImageGroup(profilPicturePath: String? = null, refGroupe: String, onComplete: () -> Unit) {
        val refCurentGroupe = firestoreInstance.collection("chatgroupes").document(refGroupe)
        refCurentGroupe.update("groupIcon", profilPicturePath)
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener {
                Log.e(
                    "FireStore",
                    "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Error adding document",
                    it.cause
                )
            }
    }

    // permet d'envoyer un message de groupe
    fun sendGroupeMessage(message: Message, groupeId: String) {
        groupeChatCollectionRef.document(groupeId)
            .collection("messages")
            .add(message)
    }

    // cette méthode permet de recupérer la liste des Groupe en temps réel
   /* fun addGroupeListener(context: Context, onListen: (List<Item>) -> Unit): ListenerRegistration {
        return firestoreInstance.collection("chatgroupes")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FIRESTORE", "Groupes listener error?", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()
                querySnapshot?.documents?.forEach {
                    val curentGroup = it.toObject(ChatGroup::class.java)!!

                    for (itm in curentGroup.members!!) {
                        if (itm == FirebaseAuth.getInstance().currentUser?.uid
                            || curentGroup.adminId == FirebaseAuth.getInstance().currentUser?.uid
                        ) {
                            items.add(GroupeItem(it.toObject(ChatGroup::class.java)!!, it.id, context))
                            break
                        }
                    }
                }

                onListen(items)
            }
    }*/

    /* cette méthode permet de recupérer la liste des Groupe en temps réel
    * et aussi selon un critere de recherche bien defini */
    fun addSearchGroupeListener(
        searchingcriterion: String,
        context: Context,
        onListen: (List<Item>) -> Unit
    ): ListenerRegistration {
        return firestoreInstance.collection("chatgroupes")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FIRESTORE", "Groupes listener error?", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()
                querySnapshot?.documents?.forEach {
                    // on convertie a chaque fois le groupe courant en objet
                    val curentGroup = it.toObject(ChatGroup::class.java)!!

                    if (!searchingcriterion.isEmpty()) {
                        if (it["groupeName"].toString().toUpperCase().contains(searchingcriterion.toUpperCase())
                            || it["groupeDescription"].toString().toUpperCase().contains(searchingcriterion.toUpperCase())
                        ) {
                            // on parcour les membres du groupe un par un
                            for (itm in curentGroup.members!!) {

                                /* si l'utilisateur fait partir des membres du groupe oubien s'il est l'administrateur
                                 du groupe on affiche le groupe dans son telephone
                                 */
                                if (itm == FirebaseAuth.getInstance().currentUser?.uid
                                    || curentGroup.adminId == FirebaseAuth.getInstance().currentUser?.uid
                                ) {


                                    items.add(GroupeItem(it.toObject(ChatGroup::class.java)!!, it.id, context))
                                    break
                                }
                            }
                        }
                    } else {
                        // on parcour les membres du groupe un par un
                        for (itm in curentGroup.members!!) {

                            /* si l'utilisateur fait partir des membres du groupe oubien s'il est l'administrateur
                             du groupe on affiche le groupe dans son telephone
                             */
                            if (itm == FirebaseAuth.getInstance().currentUser?.uid
                                || curentGroup.adminId == FirebaseAuth.getInstance().currentUser?.uid
                            ) {


                                items.add(GroupeItem(it.toObject(ChatGroup::class.java)!!, it.id, context))
                                break
                            }
                        }

                    }

                }

                onListen(items)
            }
    }

    /*
Cette méthode retourne la liste des chat dans un groupe donner
 */
    fun addGroupeChatMessagesListener(
        groupeId: String,
        context: Context,
        onListner: (List<Item>) -> Unit
    ): ListenerRegistration {
        return groupeChatCollectionRef.document(groupeId).collection("messages")
            .orderBy("time")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FIRESTORE", "ChatMessageslistener error.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()
                querySnapshot!!.documents.forEach {
                    if (it["type"] == MessageType.TEXT) {
                        val textMessage = it.toObject(TextMessage::class.java)!!
                        items.add(TextMessageItemGroup(textMessage, context))
                    } else {
                        val imageMessage = it.toObject(ImageMessage::class.java)!!
                        items.add(ImageMessageItemGroup(imageMessage, context))
                    }
                    return@forEach
                }

                onListner(items)

            }
    }

}