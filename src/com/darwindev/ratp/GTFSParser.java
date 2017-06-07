package com.darwindev.ratp;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.lang3.StringUtils;

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
     * @return Distance in Meters
     */
    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final double R = 6371.0; // Radius of the earth

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
                "METRO_1", "METRO_2", "METRO_3", "METRO_3b", "METRO_4", "METRO_5",
                "METRO_6", "METRO_7", "METRO_7b", "METRO_8", "METRO_9", "METRO_10",
                "METRO_11", "METRO_12", "METRO_13", "METRO_14"
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
            String fileName = "data-input/RATP_GTFS_" + metroLine + "/stops.txt";
            FileInputStream stream = new FileInputStream(fileName);
            InputStreamReader streamReader = new InputStreamReader(stream);
            BufferedReader reader = new BufferedReader(streamReader);
            reader.readLine(); // ignore title row
            Integer beforeIdIndex = stopIdIndex;
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lineData = CSVUtils.parseLine(line).toArray(new String[0]);
                String stopName = StringUtils.stripAccents(lineData[2]).toUpperCase();
                String latitude = lineData[4];
                String longitude = lineData[5];
                if (!stopNames.contains(stopName)) {
                    stopNames.add(stopName);
                    HashMap<String, Object> stopDetail = new HashMap<>();
                    stopDetail.put("name", stopName);
                    stopDetail.put("latitude", latitude);
                    stopDetail.put("longitude", longitude);
                    stopDetail.put("degree", 0);
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

        FileWriter jsonWriter = new FileWriter("data-output/stop-detail.json");
        Gson gson = new Gson();
        String json = gson.toJson(stopMap);
        jsonWriter.write(json);
        jsonWriter.close();

        // get sequences
        Integer seqCount = 0;
        HashMap<String, ArrayList<ArrayList<String>>> validSequences = new HashMap<>();
        for (String metroLine : metroLines) {
            validSequences.put(metroLine, new ArrayList<>());
            // trip id -> route id
            HashMap<String, String> tripRouteRel = new HashMap<>();
            String fileName1 = "data-input/RATP_GTFS_" + metroLine + "/trips.txt";
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
            String fileName = "data-input/RATP_GTFS_" + metroLine + "/stop_times.txt";
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
                }
            });
            reader.close();
            streamReader.close();
            stream.close();
            seqCount += stopSeqSet.size();
            validSequences.get(metroLine).addAll(stopSeqSet);
            System.out.println("parsed: " + fileName + ", " + stopSeqSet.size() + " sequences.");
        }
        System.out.println("all sequences parsed, " + seqCount.toString() + " sequences in total.");

        // build link between nodes
        HashSet<String> linkedEdge = new HashSet<>();
        for (String metroLine : metroLines) {
            ArrayList<ArrayList<String>> validSequence = validSequences.get(metroLine);
            for (ArrayList<String> validSeq : validSequence) {
                for (int i = 0; i < validSeq.size() - 1; i++) {
                    String strId1 = validSeq.get(i);
                    String strId2 = validSeq.get(i + 1);
                    String str1 = strId1 + " " + strId2;
                    String str2 = strId2 + " " + strId1;
                    if (!linkedEdge.contains(str1) && !linkedEdge.contains(str2)) {
                        Integer id1 = Integer.parseInt(strId1);
                        Integer id2 = Integer.parseInt(strId2);
                        Integer stopDegree1 = (Integer) stopMap.get(id1).get("degree");
                        stopMap.get(id1).put("degree", stopDegree1 + 1);
                        Integer stopDegree2 = (Integer) stopMap.get(id2).get("degree");
                        stopMap.get(id2).put("degree", stopDegree2 + 1);
                        linkedEdge.add(str1);
                    }
                }
            }
        }

        FileWriter writer1 = new FileWriter("data-output/stop-rel.csv");
        writer1.write("stopId,stopName,stopLatitude,stopLongitude\n");
        stopMap.forEach((s1, s2) -> {
            try {
                writer1.write(s1 + "," + s2.get("name") + "," + s2.get("degree") + "," + s2.get("latitude") + "," + s2.get("longitude") + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        writer1.close();

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

        FileWriter writer3 = new FileWriter("data-output/edge.csv");
        writer3.write("node1,node2,distance\n");
        linkedEdge.forEach(s -> {
            try {
                String[] vw = s.split(" ");
                Integer s1 = Integer.parseInt(vw[0]);
                Integer s2 = Integer.parseInt(vw[1]);
                Map stop1 = stopMap.get(s1);
                Map stop2 = stopMap.get(s2);
                double lat1 = Double.parseDouble((String) stop1.get("latitude"));
                double lon1 = Double.parseDouble((String) stop1.get("longitude"));
                double lat2 = Double.parseDouble((String) stop2.get("latitude"));
                double lon2 = Double.parseDouble((String) stop2.get("longitude"));
                double dis = distance(lat1, lat2, lon1, lon2, 33, 33); // Paris Altitude - 33m
                writer3.write(Integer.toString(s1) + "," + Integer.toString(s2) + "," + Double.toString(dis) +  "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        writer3.close();

    }

}
