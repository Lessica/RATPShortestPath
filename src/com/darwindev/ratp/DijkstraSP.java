package com.darwindev.ratp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class DijkstraSP {

    private int sourceNode;
    private boolean[] marked;
    private int[] previous;
    private double[] distance;

    private boolean verifyNonNegative(EdgeWeightedGraph G) {
        for (int i = 0; i < G.getNodeCount(); i++) {
            LinkedList<Edge> edges = G.getNode(i);
            for (Edge n : edges) {
                if (n.weight() <= 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public ArrayList<Integer> parse(EdgeWeightedGraph G, int s) {
        // To ensure all weights of edges are positive.
        if (!verifyNonNegative(G)) {
            return null;
        }
        sourceNode = s;
        int v = G.getNodeCount() + 1;
        marked = new boolean[v];
        previous = new int[v];
        distance = new double[v];
        // Use an array to store unvisited nodes
        HashSet<Integer> openedNodes = new HashSet<>();
        // Open all nodes
        for (int i = 0; i < v; i++) {
            previous[i] = -1; // UNDEFINED
            distance[i] = Double.MAX_VALUE; // +INFINITY
            openedNodes.add(i);
        }
        // Use an array list to record visit orders.
        ArrayList<Integer> visitOrder = new ArrayList<>();
        // Distance from source to source
        distance[s] = 0; // distance
        marked[s] = true; // mark
        visitOrder.add(s); // visit
        while (!openedNodes.isEmpty()) {
            // Choose the smallest distance.
            double smallestDistance = Double.MAX_VALUE;
            int smallestNode = -1;
            for (Integer thisNode : openedNodes) {
                if (distance[thisNode] < smallestDistance) {
                    smallestDistance = distance[thisNode];
                    smallestNode = thisNode;
                }
            }
            // Go to the smallest one.
            openedNodes.remove(smallestNode);
            visitOrder.add(smallestNode);
            // If remained nodes are not available, it is not a connected graph, terminate the progress.
            if (smallestNode == -1) {
                break;
            }
            // Check all neighbours and update distances
            for (Edge directedEdge : G.getNode(smallestNode)) {
                int childNode = directedEdge.other(smallestNode);
                double alt = distance[smallestNode] + directedEdge.weight();
                if (alt < distance[childNode]) {
                    marked[childNode] = true;
                    previous[childNode] = smallestNode;
                    distance[childNode] = alt;
                }
            }
        }
        return visitOrder;
    }

    boolean hasPathTo(int v) {
        return marked[v];
    }

    double distTo(int v) {
        return distance[v];
    }

    public ArrayList<Integer> pathTo(int v) {
        ArrayList<Integer> shortestPath = new ArrayList<>();
        int thisNode = v;
        while (thisNode > -1) {
            shortestPath.add(thisNode);
            thisNode = previous[thisNode];
            if (thisNode == sourceNode) {
                shortestPath.add(sourceNode);
                break;
            }
        }
        Collections.reverse(shortestPath);
        return shortestPath;
    }

    public static void main(String[] args) throws IOException {

        FileReader reader = new FileReader("data-output/stop-detail.json");
        Type type = new TypeToken<HashMap<Integer, Map>>(){}.getType();
        Gson gson = new Gson();
        HashMap<Integer, Map> stopMap = gson.fromJson(reader, type);

        EdgeWeightedGraph graph = new EdgeWeightedGraph("data-output/edge.csv");

        DijkstraSP dijkstraSP = new DijkstraSP();
        dijkstraSP.parse(graph, 50);
        ArrayList<Integer> suggestedPath = dijkstraSP.pathTo(94);
        System.out.println("the suggested shortest path(distance) is:");
        System.out.println(suggestedPath);
        for (Integer stopId : suggestedPath) {
            Map stopDetail = stopMap.get(stopId);
            System.out.println(stopDetail.get("name"));
        }

    }

}
