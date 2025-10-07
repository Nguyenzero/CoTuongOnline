package Dao;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;

import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseConnection {
    private static Firestore db;

    public static Firestore getConnection() {
        if (db == null) {
            try {
                FileInputStream serviceAccount =
                        new FileInputStream("src/main/resources/cotuong-firebase-adminsdk.json"); // đường dẫn tới file JSON

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                }
                db = FirestoreClient.getFirestore();

                System.out.println("✅ Kết nối Firebase thành công!");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("❌ Kết nối Firebase thất bại!");
            }
        }
        return db;
    }

    public static void main(String[] args) {
        getConnection();
    }
}