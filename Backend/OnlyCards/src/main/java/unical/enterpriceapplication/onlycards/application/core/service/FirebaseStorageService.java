package unical.enterpriceapplication.onlycards.application.core.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FirebaseStorageService {
    public void uploadFile(MultipartFile file, String userId, FirebaseFolders folder) {
        try {
            String fileName = folder.getFolderName() + userId;

            // Carica il file nella cartella specificata
            Bucket bucket = StorageClient.getInstance().bucket();
            bucket.create(fileName, file.getBytes(), file.getContentType());
        } catch (IOException e) {
            throw new RuntimeException("Errore nel caricamento del file");
        }
    }   // Metodo per caricare un file
    public String getFile(String userId,  FirebaseFolders folder) {
        try {
            String fileName = folder.getFolderName() + userId;
            Bucket bucket = StorageClient.getInstance().bucket();

            // Ottieni il blob se il file esiste
            Blob blob = bucket.get(fileName);

            // Se il blob è null, il file non esiste, quindi restituisci una stringa vuota
            if (blob == null) return "";

            // Crea il link per accedere al file
            return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                    bucket.getName(), fileName.replaceAll("/", "%2F"));
        } catch (Exception e) {
            throw new RuntimeException("Errore nel recupero del file");
        }
    }   // Metodo per ottenere un file
    public void deleteFile(String userId, FirebaseFolders folder) {
        try {
            String fileName = folder.getFolderName() + userId;
            Bucket bucket = StorageClient.getInstance().bucket();

            // Ottieni il blob se il file esiste
            Blob blob = bucket.get(fileName);

            // Se il blob è null, il file non esiste, quindi non fare nulla
            if (blob == null) return;

            // Cancella il file
            blob.delete();
        } catch (Exception e) {
            throw new RuntimeException("Errore nell'eliminazione del file");
        }
    }   // Metodo per eliminare un file

}
