import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

import com.google.common.collect.HashBiMap;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;


public class MovieRecommender {
    DataModel dataModel;
    UserSimilarity similarity;
    UserNeighborhood neighborhood;
    UserBasedRecommender recommender;
    int contReviews = 0;
    HashBiMap<String, Integer> products = HashBiMap.create();
    HashMap<String, Integer> users = new HashMap<String, Integer>();

    private void parseFile(String path) {
        try {
            PrintWriter writer = new PrintWriter("finalText.txt", "UTF-8");
            GZIPInputStream input = new GZIPInputStream(new FileInputStream(path));
            Reader decoder = new InputStreamReader(input);
            BufferedReader reader = new BufferedReader(decoder);
            String line;
            String user = "", score = "", product = "";
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("product/productId:"))
                {
                    product = line.split(" ")[1];
                    contReviews++;
                }
                if (line.startsWith("review/userId:")) user = line.split(" ")[1];

                else if (line.startsWith("review/score:"))
                {
                    score = line.split(" ")[1];
                    if (!products.containsKey(product)) products.put(product, products.size());

                    if (!users.containsKey(user)) users.put(user, users.size());
                    writer.write(users.get(user) + "," + products.get(product) + "," + score + "\n");
                }
            }
            reader.close();
            writer.close();
        } catch (Exception e) {

        }
    }


    public int getTotalReviews() {
        return contReviews;
    }

    public int getTotalProducts() {
        return products.size();
    }

    public int getTotalUsers() {
        return users.size();
    }

    public MovieRecommender(String path) {
        parseFile(path);
        try {
            dataModel = new FileDataModel(new File("finalText.txt"));
            similarity = new PearsonCorrelationSimilarity(dataModel);
            neighborhood = new ThresholdUserNeighborhood(0.1, similarity, dataModel);
            recommender = new GenericUserBasedRecommender(dataModel, neighborhood, similarity);
        }
        catch (Exception e) {
        }
    }

    public List<String> getRecommendationsForUser(String user) {
        try {
            List<RecommendedItem> recommendation = recommender.recommend(users.get(user), 3);
            System.out.println("USUARIO: " + users.get(user));
            List<String> out = new ArrayList<String>();
            for (RecommendedItem r : recommendation)
            {
                System.out.println(r.getItemID());
                System.out.println(products.inverse().get((int)r.getItemID()));
                out.add(products.inverse().get((int)r.getItemID()));
            }
            System.out.println(contReviews);
            System.out.println("este es el final " +products.get("B0002O7Y8U"));
            return out;
        }
        catch(Exception e){
            return null;
        }
    }
}