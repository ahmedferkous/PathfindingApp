package com.example.pathfindingapp.Graph;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Node implements Comparable<Node>{
    private static final String TAG = "Node";
    @Override
    public int compareTo(Node o) {
        return Double.compare(this.f, o.f);
    }

    private int value;
    private Set<Edge> edges;
    private boolean open = false;
    private boolean isOnPath = false;
    private boolean visible = false;
    private boolean startNode = false;
    private boolean endNode = false;
    private boolean obstruction = false;

    public Node parent;
    public int f = Integer.MAX_VALUE;
    public int g = Integer.MAX_VALUE;
    public int h;
    public int x, y;

    public Node(int value) {
        this.edges = new HashSet<>();
        this.value = value;
    }


    public int calculateHeuristic(Node target) {
        int value = (int) (Math.round((distance(x, y, target.x, target.y))*10));
        h = value;
        Log.d(TAG, "calculateHeuristic: (x, y): " + x + ", " + y + " (x2,y2): " + target.x + ", " + target.y + "  value: " + h + " FROM " );
        return value;
    }

    private static double distance(int x1, int y1, int x2, int y2) {
        double value = Math.sqrt(Math.pow(x2 - x1, 2) +
                Math.pow(y2 - y1, 2) * 1.0);
        Log.d(TAG, "distance: " + value);
        return value;
    }


    public boolean isOnPath() {
        return isOnPath;
    }

    public void setOnPath(boolean onPath) {
        isOnPath = onPath;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public int getValue() {
        return value;
    }

    public boolean addEdge(Edge edge) {
        return edges.add(edge);
    }

    public List<Edge> getEdges() {
        return new ArrayList<>(edges);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isStartNode() {
        return startNode;
    }

    public void setStartNode(boolean startNode) {
        this.startNode = startNode;
    }

    public boolean isEndNode() {
        return endNode;
    }

    public void setEndNode(boolean endNode) {
        this.endNode = endNode;
    }

    public boolean isObstruction() {
        return obstruction;
    }

    public void setObstruction(boolean obstruction) {
        this.obstruction = obstruction;
    }
}