import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.IOException;
import java.util.Collections;

class DBConnector {
    private final MongoClient client;
    private DBConfig dbConfig;

    DBConnector() throws IOException {
        dbConfig = new DBConfig();

        MongoCredential credential = MongoCredential.createCredential(dbConfig.getUsername(), dbConfig.getAuthDb(), dbConfig.getPassword().toCharArray());
        client = MongoClients.create(
                MongoClientSettings.builder()
                        .credential(credential)
                        .applyToClusterSettings(builder ->
                                builder.hosts(Collections.singletonList(new ServerAddress(dbConfig.getAddress(), dbConfig.getPort()))))
                        .build());
    }

    MongoDatabase getDatabase(String name) {
        if(!name.isEmpty()) {
            return client.getDatabase(name);
        }
        return null;
    }

    void createTable(MongoDatabase database, String tableName) {
        database.createCollection(tableName);
    }

    void addDocument(MongoDatabase database, String tableName, String id, String tweet) {
        MongoCollection collection = database.getCollection(tableName);
        Document document = new Document();
        document.append(id, tweet);
        collection.insertOne(document);
    }
}
