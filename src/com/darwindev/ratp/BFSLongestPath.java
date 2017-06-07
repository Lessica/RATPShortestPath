package com.darwindev.ratp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BFSLongestPath {
    private int diameter;
    private ArrayList<Integer> longestPath;

    public BFSLongestPath(EdgeWeightedGraph G)
    {
        diameter = 0;
        int v = -1, w = -1;
        BFSShortestPath bfsShortestPath = new BFSShortestPath();
        for (int i = 0; i < G.getNodeCount(); i++)
        {
            bfsShortestPath.bfs(G, i);
            int max = 0;
            for (int j = 0; j < G.getNodeCount(); j++)
            {
                int dist = bfsShortestPath.distTo(j);
                if (dist > max)
                {
                    max = dist;
                    w = j;
                }
            }
            if (max > diameter)
            {
                diameter = max;
                v = i;
            }
        }
        bfsShortestPath.bfs(G, v);
        longestPath = bfsShortestPath.pathTo(w);
    }

    public int getDiameter() {
        return diameter;
    }

    public ArrayList<Integer> getLongestPath() {
        return longestPath;
    }

    public static void main(String[] args) throws IOException {

        FileReader reader = new FileReader("data-output/stop-detail.json");
        Type type = new TypeToken<HashMap<Integer, Map>>(){}.getType();
        Gson gson = new Gson();
        HashMap<Integer, Map> stopMap = gson.fromJson(reader, type);

        EdgeWeightedGraph graph = new EdgeWeightedGraph("data-output/edge.csv");

        BFSLongestPath bfsLongestPath = new BFSLongestPath(graph);

        ArrayList<Integer> longestPath = bfsLongestPath.getLongestPath();
        System.out.println("Diameter of the graph: " + bfsLongestPath.getDiameter());
        System.out.println("Longest path: " + longestPath);
        for (Integer stopId : longestPath) {
            Map stopDetail = stopMap.get(stopId);
            System.out.println(stopDetail.get("name"));
        }
        double totalLength = 0;
        System.out.println("Lengths of sub-paths: ");
        for (int i = 0; i < longestPath.size() - 1; i++) {
            int vv = longestPath.get(i), ww = longestPath.get(i + 1);
            Edge edge = graph.getEdge(vv, ww);
            System.out.println(Integer.toString(vv) + "->" + Integer.toString(ww) + ": " + Double.toString(edge.weight()));
            totalLength += edge.weight();
        }
        System.out.println("Total length of the path: " + Double.toString(totalLength));

    }

}
