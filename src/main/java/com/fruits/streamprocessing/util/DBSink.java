package com.fruits.streamprocessing.util;

import com.fruits.streamprocessing.FruitStreaming;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Database sink function for {@link FruitStreaming}
 *  @author Éléa Dufresne
 */
public class DBSink implements SinkFunction<Tuple2<String, Integer>> {

    /* credentials to connect to the database */
    private final String url, user, password;
    public DBSink(String url, String user, String password) {
            this.url = url;
            this.user = user;
            this.password = password;
    }

    /** Updates the number of oranges that share a common feature in this MySQL database
     * @param value Tuple2 containing a feature and the # of oranges that was just observed */
    @Override
    public void invoke(Tuple2<String, Integer> value, Context context) {
        // if there is nothing to insert we might run into issues, so we continue executing
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String sql_query = "INSERT INTO data (feature, count) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE count = count + ?";

            try (PreparedStatement statement = connection.prepareStatement(sql_query)) {
                statement.setString(1, value.f0); // feature
                statement.setInt(2, value.f1); // count
                statement.setInt(3, value.f1); // update count
                statement.executeUpdate();
            } catch (SQLException e) {
                System.err.println("ERROR: could not execute the query \"" + sql_query + "\" ("
                    + e.getMessage()+")");
            }
        } catch (SQLException e) {
            System.err.println("ERROR: could not connect to the database (" + e.getMessage()+")");
        }
    }
}
