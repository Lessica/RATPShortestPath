package com.darwindev.ratp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by Zheng on 31/05/2017.
 *
 */
public class BFSSP {

    private int sourceNode;
    private boolean[] marked;
    private int[] previous;
    private int[] distance;

    // The function to do BFS traversal.
    public ArrayList<Integer> parse(EdgeWeightedGraph G, int s) {
        sourceNode = s;
        int v = G.getNodeCount() + 1;
        marked = new boolean[v];
        previous = new int[v];
        distance = new int[v];
        for (int i = 0; i < v; i++) {
            previous[i] = -1; // UNDEFINED
            distance[i] = Integer.MAX_VALUE; // +INFINITY
        }
        // Use an array list to record visit orders.
        ArrayList<Integer> visitOrder = new ArrayList<>();
        // Perform search algorithm without recursive calls.
        // BFS uses Queue data structure.
        Queue<Integer> queue = new LinkedList<>();
        Queue<Integer> distanceQueue = new LinkedList<>();
        // Put root node into queue
        // Put distance into queue (correspond to node)
        queue.add(s);
        marked[s] = true;
        previous[s] = -1;
        distanceQueue.add(0);
        distance[s] = 0;
        while (!queue.isEmpty()) {
            int node = queue.remove();
            int nodeDistance = distanceQueue.remove();
            visitOrder.add(node);
            // In case of choice, the vertex with the smallest identifier will be chosen.
            for (Edge childEdge : G.getNode(node)) {
                int thisNode = childEdge.other(node);
                if (!marked[thisNode]) {
                    queue.add(thisNode);
                    // Mark child node
                    marked[thisNode] = true;
                    previous[thisNode] = node;
                    // Update distance
                    distanceQueue.add(nodeDistance + 1);
                    distance[thisNode] = nodeDistance + 1;
                }
            }
        }
        return visitOrder;
    }

    boolean hasPathTo(int v) {
        return marked[v];
    }

    int distTo(int v) {
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

        EdgeWeightedGraph graph = new EdgeWeightedGraph("data-output/edge.txt");
        graph.print();

        BFSSP bfssp = new BFSSP();
        bfssp.parse(graph, 77);
        ArrayList<Integer> suggestedPath = bfssp.pathTo(67);
        System.out.println(suggestedPath);
        for (Integer stopId : suggestedPath) {
            Map stopDetail = stopMap.get(stopId);
            System.out.println(stopDetail.get("name"));
        }

    }

}
