package com.darwindev.ratp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BFSGraphCluster {
    public static void main(String[] args) throws IOException {

        FileReader reader = new FileReader("data-output/stop-detail.json");
        Type type = new TypeToken<HashMap<Integer, Map>>(){}.getType();
        Gson gson = new Gson();
        HashMap<Integer, Map> stopMap = gson.fromJson(reader, type);

        EdgeWeightedGraph graph = new EdgeWeightedGraph("data-output/edge.txt");

        BFSLongestPath bfsBetweenness = new BFSLongestPath(graph);
        while (true) {
            ArrayList<Edge> edgeSortByBetweenness = bfsBetweenness.edgesSortByBetweenness();
            if (edgeSortByBetweenness.size() > 0) {
                Edge e = edgeSortByBetweenness.get(0);
                Map stopDetail1 = stopMap.get(e.either());
                Map stopDetail2 = stopMap.get(e.other(e.either()));
                System.out.println("Remove edge: " + stopDetail1.get("name") + "->" + stopDetail2.get("name") + ", betweenness = " + Integer.toString(e.getBetweenness()));
                graph.removeEdge(e);
                if (!BFSShortestPath.isConnectedGraph(graph))
                {
                    break;
                }
                bfsBetweenness = new BFSLongestPath(graph);
            }
        }

    }
}
