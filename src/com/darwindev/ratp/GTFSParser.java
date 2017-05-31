package com.darwindev.ratp;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class GTFSParser {

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     *
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     * @returns Distance in Meters
     */
    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    public static void main(String[] args) throws IOException {

        String[] metroLines = {
                "1", "2", "3", "3b", "4", "5",
                "6", "7", "7b", "8", "9", "10",
                "11", "12", "13", "14"
        };

        // original id -> new id
        HashMap<String, String> stopIdRel = new HashMap<>();
        // new id <-> stop name
        HashMap<Integer, Map> stopMap = new HashMap<>();
        HashMap<String, String> stopBiMap = new HashMap<>();

        // get stops
        Integer stopIdIndex = 0;
        HashSet<String> stopNames = new HashSet<>();
        for (String metroLine : metroLines) {
            String fileName = "dataset-metro/RATP_GTFS_METRO_" + metroLine + "/stops.txt";
            FileInputStream stream = new FileInputStream(fileName);
            InputStreamReader streamReader = new InputStreamReader(stream);
            BufferedReader reader = new BufferedReader(streamReader);
            reader.readLine(); // ignore title row
            Integer beforeIdIndex = stopIdIndex;
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lineData = CSVUtils.parseLine(line).toArray(new String[0]);
                String stopName = lineData[2];
                String latitude = lineData[4];
                String longitude = lineData[5];
                if (!stopNames.contains(stopName)) {
                    stopNames.add(stopName);
                    HashMap<String, String> stopDetail = new HashMap<>();
                    stopDetail.put("name", stopName);
                    stopDetail.put("latitude", latitude);
                    stopDetail.put("longitude", longitude);
                    stopMap.put(stopIdIndex, stopDetail);
                    stopBiMap.put(stopName, stopIdIndex.toString());
                    // build relationship between new id and stop name
                    stopIdIndex++;
                }
                String stopId = lineData[0];
                stopIdRel.put(stopId, stopBiMap.get(stopName));
                // build relationship between old id and new id
                // different original id can connect to the same new id
            }
            reader.close();
            streamReader.close();
            stream.close();
            System.out.println("parsed: " + fileName + ", " + (stopIdIndex - beforeIdIndex) + " stops.");
        }
        System.out.println("all stops parsed, " + stopNames.size() + " stops in total.");

        FileWriter writer1 = new FileWriter("data-output/stop-rel.txt");
        stopMap.forEach((s1, s2) -> {
            try {
                writer1.write(s1 + "," + s2.get("name") + "," + s2.get("latitude") + "," + s2.get("longitude") + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        writer1.close();

        // get sequences
        HashMap<String, ArrayList<ArrayList<String>>> validSequences = new HashMap<>();
        for (String metroLine : metroLines) {
            validSequences.put(metroLine, new ArrayList<>());
            // trip id -> route id
            HashMap<String, String> tripRouteRel = new HashMap<>();
            String fileName1 = "dataset-metro/RATP_GTFS_METRO_" + metroLine + "/trips.txt";
            FileInputStream stream1 = new FileInputStream(fileName1);
            InputStreamReader streamReader1 = new InputStreamReader(stream1);
            BufferedReader reader1 = new BufferedReader(streamReader1);
            reader1.readLine(); // ignore title row
            String line1;
            while ((line1 = reader1.readLine()) != null) {
                String[] lineData = CSVUtils.parseLine(line1).toArray(new String[0]);
                String routeId = lineData[0];
                String tripId = lineData[2];
//                String directionId = lineData[5];
                tripRouteRel.put(tripId, routeId);
            }
            HashSet<String> visitedRoute = new HashSet<>();
            HashSet<ArrayList<String>> stopSeqSet = new HashSet<>();
            HashMap<String, ArrayList<String>> stopSeqRel = new HashMap<>();
            String fileName = "dataset-metro/RATP_GTFS_METRO_" + metroLine + "/stop_times.txt";
            FileInputStream stream = new FileInputStream(fileName);
            InputStreamReader streamReader = new InputStreamReader(stream);
            BufferedReader reader = new BufferedReader(streamReader);
            reader.readLine(); // ignore title row
            String line2;
            while ((line2 = reader.readLine()) != null) {
                String[] lineData = CSVUtils.parseLine(line2).toArray(new String[0]);
                String tripId = lineData[0];
                String stopId = lineData[3];
                String routeId = tripRouteRel.get(tripId);
                if (!stopSeqRel.containsKey(tripId) && !visitedRoute.contains(routeId)) {
                    stopSeqRel.put(tripId, new ArrayList<>());
                    visitedRoute.add(routeId);
                }
                ArrayList <String> stopSeq = stopSeqRel.get(tripId);
                if (stopSeq == null) {
                    continue;
                }
                stopSeq.add(stopId);
            }
            stopSeqRel.forEach((s, oldSeq) -> {
                ArrayList<String> newSeq = new ArrayList<>();
                for (String anOldSeq : oldSeq) {
                    newSeq.add(stopIdRel.get(anOldSeq));
                }
                if (!stopSeqSet.contains(newSeq)) {
                    stopSeqSet.add(newSeq);
                    System.out.println(newSeq);
                }
            });
            reader.close();
            streamReader.close();
            stream.close();
            validSequences.get(metroLine).addAll(stopSeqSet);
            System.out.println("parsed: " + fileName + ", " + stopSeqSet.size() + " sequences.");
        }

        FileWriter writer2 = new FileWriter("data-output/valid-sequence.txt");
        validSequences.forEach((s, arrayLists) -> {
            try {
                for (ArrayList<String> arrayList : arrayLists) {
                    writer2.write(s + ": " + arrayList + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        writer2.close();

        // build link between nodes
        HashSet<String> linkedEdge = new HashSet<>();
        for (String metroLine : metroLines) {
            ArrayList<ArrayList<String>> validSequence = validSequences.get(metroLine);
            for (ArrayList<String> validSeq : validSequence) {
                for (int i = 0; i < validSeq.size() - 1; i++) {
                    String str1 = validSeq.get(i) + " " + validSeq.get(i + 1);
                    String str2 = validSeq.get(i + 1) + " " + validSeq.get(i);
                    if (!linkedEdge.contains(str1) && !linkedEdge.contains(str2)) {
                        linkedEdge.add(str1);
                    }
                }
            }
        }

        FileWriter writer3 = new FileWriter("data-output/edge.txt");
        linkedEdge.forEach(s -> {
            try {
                String[] vw = s.split(" ");
                Map stop1 = stopMap.get(Integer.parseInt(vw[0]));
                Map stop2 = stopMap.get(Integer.parseInt(vw[1]));
                double lat1 = Double.parseDouble((String) stop1.get("latitude"));
                double lon1 = Double.parseDouble((String) stop1.get("longitude"));
                double lat2 = Double.parseDouble((String) stop2.get("latitude"));
                double lon2 = Double.parseDouble((String) stop2.get("longitude"));
                double dis = distance(lat1, lat2, lon1, lon2, 0, 0);
                writer3.write(s + " " + Double.toString(dis) +  "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        writer3.close();

        EdgeWeightedGraph graph = new EdgeWeightedGraph("data-output/edge.txt");
        graph.print();

    }

}
