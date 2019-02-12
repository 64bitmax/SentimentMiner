import com.mongodb.client.*;
import org.bson.Document;

import java.io.IOException;

class DBConnector {
    private final MongoClient client;
    private DBConfig dbConfig;

    DBConnector() throws IOException {
         dbConfig = new DBConfig();

//        MongoCredential credential = MongoCredential.createCredential(dbConfig.getUsername(), dbConfig.getAuthDb(), dbConfig.getPassword().toCharArray());
//        client = MongoClients.create(
//                MongoClientSettings.builder()
//                        .credential(credential)
//                        .applyToClusterSettings(builder ->
//                                builder.hosts(Collections.singletonList(new ServerAddress(dbConfig.getAddress(), dbConfig.getPort()))))
//                        .build());


        client = MongoClients.create(dbConfig.getURI());
    }

    MongoDatabase getDatabase(String name) {
        if(!name.isEmpty()) {
            return client.getDatabase(name);
        }
        return null;
    }

    void createTable(MongoDatabase database, String tableName) {
        boolean exists = checkTable(database, tableName);
        if(!exists) {
            database.createCollection(tableName);
        }
    }

    boolean checkTable (MongoDatabase database, String table) {
        MongoIterable<String> names = database.listCollectionNames();

        for(String name : names) {
            if(name.equals(table)) {
                return true;
            }
        }

        return false;
    }

    void addDocument(MongoDatabase database, String tableName, String id, String tweet) {
        MongoCollection collection = database.getCollection(tableName);
        Document document = new Document();
        document.append(id, tweet);
        collection.insertOne(document);
    }
}
