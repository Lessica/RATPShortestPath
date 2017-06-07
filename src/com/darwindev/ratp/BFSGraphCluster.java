package com.darwindev.ratp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class BFSGraphCluster {
    private HashSet<Edge> edgeSet;

    public BFSGraphCluster(EdgeWeightedGraph G)
    {
        edgeSet = new HashSet<>();
        for (int i = 0; i < G.getNodeCount(); i++) {
            edgeSet.addAll(G.getNode(i));
        }
        edgeSet.forEach(Edge::cleanBetweenness);
        BFSShortestPath bfsShortestPath = new BFSShortestPath();
        for (int i = 0; i < G.getNodeCount(); i++)
        {
            bfsShortestPath.bfs(G, i);
            for (int j = i; j < G.getNodeCount(); j++)
            {
                for (int t = j; t != i; t = bfsShortestPath.edgeTo(t).other(t)) {
                    bfsShortestPath.edgeTo(t).addBetweenness(1);
                }
            }
        }
    }

    public ArrayList<Edge> edgesSortByBetweenness() {
        ArrayList<Edge> edgeArray = new ArrayList<>(edgeSet);
        edgeArray.sort((o1, o2) -> (o1.getBetweenness() < o2.getBetweenness()) ? 1 : -1);
        return edgeArray;
    }

    public static void main(String[] args) throws IOException {

        FileReader reader = new FileReader("data-output/stop-detail.json");
        Type type = new TypeToken<HashMap<Integer, Map>>(){}.getType();
        Gson gson = new Gson();
        HashMap<Integer, Map> stopMap = gson.fromJson(reader, type);

        EdgeWeightedGraph graph = new EdgeWeightedGraph("data-output/edge.csv");

        BFSGraphCluster bfsGraphCluster = new BFSGraphCluster(graph);
        while (true) {
            ArrayList<Edge> edgeSortByBetweenness = bfsGraphCluster.edgesSortByBetweenness();
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
                bfsGraphCluster = new BFSGraphCluster(graph);
            }
        }
        System.out.println("Connected components: ");
        BFSShortestPath shortestPath = new BFSShortestPath();
        ArrayList<ArrayList<Integer>> bfsResultAll = shortestPath.bfsAll(graph);
        for (ArrayList<Integer> bfsResult : bfsResultAll) {
            System.out.println("\n" + bfsResult + ", length = " + bfsResult.size());
        }

    }
}
