package com.example.pathfindingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.pathfindingapp.Graph.Edge;
import com.example.pathfindingapp.Graph.Node;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

public class PathfindingActivity extends AppCompatActivity {
    // 115 for my phone
    // 65 for emulator
    private static final String TAG = "MainActivity";
    private static boolean stop = false;
    private static boolean running = false;
    private RecyclerView recView;
    private GridAutofitLayoutManager manager;
    private NodeAdapter adapter;
    private AlgorithmThread algorithmThread;
    private int spanCount, numberOfRows;
    private static String type;
    private static int millisecondsIncrement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pathfinding);
        initViews();

        Intent intent = getIntent();
        if (intent != null) {
            spanCount = intent.getIntExtra(ConfigActivity.TYPE_SPAN_COUNT, -1);
            numberOfRows = intent.getIntExtra(ConfigActivity.TYPE_ROW_COUNT, -1);
            millisecondsIncrement = intent.getIntExtra(ConfigActivity.TYPE_MILLI_INCREMENT, -1);
            type = intent.getStringExtra(ConfigActivity.TYPE_ALGORITHM);

            if (spanCount != -1 && numberOfRows != -1 && millisecondsIncrement != -1 && type != null) {
                stop = false;
                adapter = new NodeAdapter(this, type);
                manager = new GridAutofitLayoutManager(this, ConfigActivity.WIDTH, null);
                recView.setAdapter(adapter);
                recView.setLayoutManager(manager);
                setupAdapter();
            }
        }

    }

    private void initViews() {
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        recView = findViewById(R.id.recView);
    }

    private void setupAdapter() {
        ArrayList<Node> nodes = new ArrayList<>();
        int totalNodes = spanCount * numberOfRows;
        for (int i = 0; i < totalNodes; i++) {
            nodes.add(new Node((i + 1), type));
        }
        adapter.setNodes(nodes);
    }

    private void computeNodes() {
        int n = spanCount;
        int k = manager.getItemCount() / n;
        adapter.setNodes(ComputeNodes.computeGraph(n, k, adapter.getNodes()));

        runAlgorithm(adapter.receiveNode(false), adapter.receiveNode(true));
    }

    private boolean validLayoutChoice() {
        if (!(NodeAdapter.START_NODE_EXISTS) && !(NodeAdapter.END_NODE_EXISTS)) {
            Log.d(TAG, "validLayoutChoice: " + NodeAdapter.START_NODE_EXISTS + " " + NodeAdapter.END_NODE_EXISTS);
            Toast.makeText(this, "Neither the Starting Node or Ending Node has been selected!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!NodeAdapter.START_NODE_EXISTS) {
            Log.d(TAG, "validLayoutChoice: " + NodeAdapter.START_NODE_EXISTS + " " + NodeAdapter.END_NODE_EXISTS);
            Toast.makeText(this, "Please Select A Starting Node", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!NodeAdapter.END_NODE_EXISTS) {
            Log.d(TAG, "validLayoutChoice: " + NodeAdapter.START_NODE_EXISTS + " " + NodeAdapter.END_NODE_EXISTS);
            Toast.makeText(this, "Please Select A End Node", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void runAlgorithm(Node startNode, Node endNode) {
        algorithmThread = new AlgorithmThread(startNode, endNode);
        algorithmThread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_item, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionRun:
                if (!running) {
                    if (validLayoutChoice()) {
                        AlertDialog.Builder builderOne = new AlertDialog.Builder(this)
                                .setMessage("Run Algorithm?")
                                .setNegativeButton("No", null)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        adapter.edit();
                                        NodeAdapter.SAVED = true;
                                        stop = false;
                                        running = true;
                                        computeNodes();
                                    }
                                });
                        builderOne.create().show();
                    }
                }
                break;
            case R.id.actionReset:
                if (!running) {
                    AlertDialog.Builder builderTwo = new AlertDialog.Builder(this)
                            .setMessage("Are you sure you want to reset?")
                            .setNegativeButton("No", null)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    NodeAdapter.SAVED = false;
                                    NodeAdapter.START_NODE_EXISTS = false;
                                    NodeAdapter.END_NODE_EXISTS = false;
                                    setupAdapter();
                                }
                            });
                    builderTwo.create().show();
                }
                break;
            case R.id.actionEdit:
                if (!running) {
                    AlertDialog.Builder builderThree = new AlertDialog.Builder(this)
                            .setMessage("Edit Layout?")
                            .setNegativeButton("No", null)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    NodeAdapter.SAVED = false;
                                    adapter.edit();
                                }
                            });
                    builderThree.create().show();
                }
                break;
            case R.id.actionStop:
                if (running) {
                    AlertDialog.Builder builderTwo = new AlertDialog.Builder(this)
                            .setMessage("Stop Algorithm?")
                            .setNegativeButton("No", null)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    stop = true;
                                    running = false;
                                }
                            });
                    builderTwo.create().show();
                }
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit the layout?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NodeAdapter.SAVED = false;
                        NodeAdapter.START_NODE_EXISTS = false;
                        NodeAdapter.END_NODE_EXISTS = false;
                        stop = true;
                        running = false;

                        if (algorithmThread != null) {
                            algorithmThread.stopRunningAlgorithm();
                        }

                        Intent intent = new Intent(PathfindingActivity.this, ConfigActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
        builder.create().show();
    }

    public static class ComputeNodes {
        public static final String TOP_LEFT = "TOP_LEFT";
        public static final String TOP_RIGHT = "TOP_RIGHT";
        public static final String BOTTOM_LEFT = "BOTTOM_LEFT";
        public static final String BOTTOM_RIGHT = "BOTTOM_RIGHT";
        public static final String TOP_NODE = "TOP_NODE";
        public static final String BOTTOM_NODE = "BOTTOM_NODE";
        public static final String LEFT_NODE = "LEFT_NODE";
        public static final String RIGHT_NODE = "RIGHT_NODE";
        public static final String MIDDLE_NODE = "MIDDLE_NODE";

        // n col, k rows, c current row,
        public static String calculateTypeOfNode(int n, int k, int h, int value) {
            if (value == 1) {
                return TOP_LEFT;
            }
            if (value == n) {
                return TOP_RIGHT;
            }
            if (value == ((n * (k - 1)) + 1)) {
                return BOTTOM_LEFT;
            }
            if (value == (n * k)) {
                return BOTTOM_RIGHT;
            }
            if (value > 1 && value < n) {
                return TOP_NODE;
            }
            if (value > (n * (k - 1)) + 1 && value < n * k) {
                return BOTTOM_NODE;
            }
            if (value == (n * (h - 1)) + 1) {
                return LEFT_NODE;
            }
            if (value == n * h) {
                return RIGHT_NODE;
            }
            return MIDDLE_NODE;
        }

        public static ArrayList<Node> computeGraph(int n, int k, ArrayList<Node> nodes) {
            int rowCounter = 1;
            int numCounter = 1;
            for (int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                String type = calculateTypeOfNode(n, k, rowCounter, i + 1);
                node.x = rowCounter;
                node.y = numCounter;

                switch (type) {
                    case TOP_LEFT:
                        Edge edgeTF1 = new Edge(node, nodes.get(Math.abs(i + 1)), 10);
                        Edge edgeTF2 = new Edge(node, nodes.get(Math.abs(i + n)), 10);
                        Edge edgeTF3 = new Edge(node, nodes.get(Math.abs(i + (n + 1))), 14);
                        node.addEdge(edgeTF1);
                        node.addEdge(edgeTF2);
                        node.addEdge(edgeTF3);
                        break;
                    case TOP_RIGHT:
                        Edge edgeTR1 = new Edge(node, nodes.get(Math.abs(i - 1)), 10);
                        Edge edgeTR2 = new Edge(node, nodes.get(Math.abs(i + n)), 10);
                        Edge edgeTR3 = new Edge(node, nodes.get(Math.abs(i + (n - 1))), 14);
                        node.addEdge(edgeTR1);
                        node.addEdge(edgeTR2);
                        node.addEdge(edgeTR3);
                        break;
                    case BOTTOM_RIGHT:
                        Edge edgeBR1 = new Edge(node, nodes.get(Math.abs(i - n)), 10);
                        Edge edgeBR2 = new Edge(node, nodes.get(Math.abs(i - 1)), 10);
                        Edge edgeBR3 = new Edge(node, nodes.get(Math.abs(i - (n + 1))), 14);
                        node.addEdge(edgeBR1);
                        node.addEdge(edgeBR2);
                        node.addEdge(edgeBR3);
                        break;
                    case BOTTOM_LEFT:
                        Edge edgeBL1 = new Edge(node, nodes.get(Math.abs(i - n)), 10);
                        Edge edgeBL2 = new Edge(node, nodes.get(Math.abs(i + 1)), 10);
                        Edge edgeBL3 = new Edge(node, nodes.get(Math.abs(i - (n - 1))), 14);
                        node.addEdge(edgeBL1);
                        node.addEdge(edgeBL2);
                        node.addEdge(edgeBL3);
                        break;
                    case TOP_NODE:
                        Edge edgeT0 = new Edge(node, nodes.get(Math.abs(i - 1)), 10);
                        Edge edgeT1 = new Edge(node, nodes.get(Math.abs(i + 1)), 10);
                        Edge edgeT2 = new Edge(node, nodes.get(Math.abs(i + (n - 1))), 14);
                        Edge edgeT3 = new Edge(node, nodes.get(Math.abs(i + n)), 10);
                        Edge edgeT4 = new Edge(node, nodes.get(Math.abs(i + (n + 1))), 14);
                        node.addEdge(edgeT0);
                        node.addEdge(edgeT1);
                        node.addEdge(edgeT2);
                        node.addEdge(edgeT3);
                        node.addEdge(edgeT4);
                        break;
                    case RIGHT_NODE:
                        Edge edgeR0 = new Edge(node, nodes.get(Math.abs(i - n)), 10);
                        Edge edgeR1 = new Edge(node, nodes.get(Math.abs(i - (n + 1))), 14);
                        Edge edgeR2 = new Edge(node, nodes.get(Math.abs(i - 1)), 10);
                        Edge edgeR3 = new Edge(node, nodes.get(Math.abs(i + (n - 1))), 14);
                        Edge edgeR4 = new Edge(node, nodes.get(Math.abs(i + n)), 10);
                        node.addEdge(edgeR0);
                        node.addEdge(edgeR1);
                        node.addEdge(edgeR2);
                        node.addEdge(edgeR3);
                        node.addEdge(edgeR4);
                        break;
                    case LEFT_NODE:
                        Edge edgeL0 = new Edge(node, nodes.get(Math.abs(i - n)), 10);
                        Edge edgeL1 = new Edge(node, nodes.get(Math.abs(i - (n - 1))), 14);
                        Edge edgeL2 = new Edge(node, nodes.get(Math.abs(i + 1)), 10);
                        Edge edgeL3 = new Edge(node, nodes.get(Math.abs(i + n)), 10);
                        Edge edgeL4 = new Edge(node, nodes.get(Math.abs(i + (n + 1))), 14);
                        node.addEdge(edgeL0);
                        node.addEdge(edgeL1);
                        node.addEdge(edgeL2);
                        node.addEdge(edgeL3);
                        node.addEdge(edgeL4);
                        break;
                    case BOTTOM_NODE:
                        Edge edgeB0 = new Edge(node, nodes.get(Math.abs(i - 1)), 10);
                        Edge edgeB1 = new Edge(node, nodes.get(Math.abs(i + 1)), 10);
                        Edge edgeB2 = new Edge(node, nodes.get(Math.abs(i - (n + 1))), 14);
                        Edge edgeB3 = new Edge(node, nodes.get(Math.abs(i - n)), 10);
                        Edge edgeB4 = new Edge(node, nodes.get(Math.abs(i - (n - 1))), 14);
                        node.addEdge(edgeB0);
                        node.addEdge(edgeB1);
                        node.addEdge(edgeB2);
                        node.addEdge(edgeB3);
                        node.addEdge(edgeB4);
                        break;
                    case MIDDLE_NODE:
                        Edge edgeM0 = new Edge(node, nodes.get(Math.abs(i - (n + 1))), 14);
                        Edge edgeM1 = new Edge(node, nodes.get(Math.abs(i - n)), 10);
                        Edge edgeM2 = new Edge(node, nodes.get(Math.abs(i - (n - 1))), 14);
                        Edge edgeM3 = new Edge(node, nodes.get(Math.abs(i - 1)), 10);
                        Edge edgeM4 = new Edge(node, nodes.get(Math.abs(i + 1)), 10);
                        Edge edgeM5 = new Edge(node, nodes.get(Math.abs(i + (n - 1))), 14);
                        Edge edgeM6 = new Edge(node, nodes.get(Math.abs(i + n)), 10);
                        Edge edgeM7 = new Edge(node, nodes.get(Math.abs(i + (n + 1))), 14);
                        node.addEdge(edgeM0);
                        node.addEdge(edgeM1);
                        node.addEdge(edgeM2);
                        node.addEdge(edgeM3);
                        node.addEdge(edgeM4);
                        node.addEdge(edgeM5);
                        node.addEdge(edgeM6);
                        node.addEdge(edgeM7);
                        break;
                    default:
                        break;
                }

                nodes.set(i, node);

                if ((i + 1) % n == 0) {
                    rowCounter++;
                    numCounter = 1;
                } else {
                    numCounter++;
                }
            }

            return nodes;
        }

    }

    private class AlgorithmThread extends Thread {
        private  boolean permStop = false;
        private final Node startNode, endNode;
        private ArrayList<Node> allNodes;
        private final int size;

        public AlgorithmThread(Node startNode, Node endNode) {
            this.startNode = startNode;
            this.endNode = endNode;
            this.size = adapter.getItemCount();
            allNodes = adapter.getNodes();
        }

        public Node aStar(Node startNode, Node endNode) throws InterruptedException {
            PriorityQueue<Node> closedList = new PriorityQueue<>();
            PriorityQueue<Node> openList = new PriorityQueue<>();

            startNode.g = 0;
            startNode.f = startNode.g + startNode.calculateHeuristic(endNode);
            openList.add(startNode);

            while (!openList.isEmpty()) {
                if (!stop) {
                    Node n = openList.peek();

                    if (n == endNode) {
                        return n;
                    }

                    for (Edge edge : n.getEdges()) {
                        Node m = edge.to;
                        int totalWeight = n.g + edge.getWeight();

                        if (!m.isObstruction()) {
                            if (!openList.contains(m) && !closedList.contains(m)) {
                                m.parent = n;
                                m.g = totalWeight;
                                m.f = m.g + m.calculateHeuristic(endNode);
                                openList.add(m);
                                m.setOpen(1);
                                showProgressOnAdapter(m);
                            } else {
                                if (totalWeight < m.g) {
                                    m.parent = n;
                                    m.g = totalWeight;
                                    m.f = m.g + m.calculateHeuristic(endNode);

                                    if (closedList.contains(m)) {
                                        closedList.remove(m);
                                        openList.add(m);
                                        m.setOpen(1);

                                    }
                                    showProgressOnAdapter(m);
                                }
                            }
                        }
                    }

                    openList.remove(n);
                    closedList.add(n);
                    n.setOpen(0);
                    showProgressOnAdapter(n);
                    Thread.sleep(millisecondsIncrement);
                } else {
                    stopRunningAlgorithm();
                    break;
                }
            }
            return null;
        }

        public Node dijkstra(Node startNode, Node endNode) throws InterruptedException {
            PriorityQueue<Node> pq = new PriorityQueue<>();
            startNode.cost = 0;
            pq.add(startNode);

            while (!pq.isEmpty()) {
                if (!stop) {
                    Node retrievedNode = pq.peek();

                    for (Edge edge : retrievedNode.getEdges()) {
                        Node adjNode = edge.to;
                        if (!adjNode.isObstruction()) {
                            if (edge.getWeight() + retrievedNode.cost < adjNode.cost) {
                                Log.d(TAG, "dijkstra: " + edge.getWeight() + " " + retrievedNode.cost);
                                adjNode.cost = edge.getWeight() + retrievedNode.cost;
                                adjNode.parent = retrievedNode;
                                pq.add(adjNode);
                                adjNode.setOpen(1);
                                showProgressOnAdapter(adjNode);
                            }
                        }
                    }
                    Thread.sleep(millisecondsIncrement);
                    retrievedNode.setOpen(0);
                    showProgressOnAdapter(retrievedNode);
                    pq.remove(retrievedNode);
                    } else {
                        stopRunningAlgorithm();
                        break;
                    }
            }
            return endNode;
        }

        public ArrayList<Node> bestFirstSearch(Node startNode, Node endNode) throws InterruptedException {
            ArrayList<Node> visited = new ArrayList<>();
            PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
            startNode.cost = 0;
            priorityQueue.add(startNode);

            while (!priorityQueue.isEmpty()) {
                if (!stop) {
                    Node retrievedNode = priorityQueue.peek();

                    if (retrievedNode == endNode) {
                        endNode.found = true;
                        break;
                    }

                    if (!visited.contains(retrievedNode)) {
                        visited.add(retrievedNode);
                    }

                    for (Edge edge: retrievedNode.getEdges()) {
                        Node n = edge.to;
                        if (!n.isObstruction()) {
                            if (!visited.contains(n)) {
                                n.cost = edge.getWeight() + retrievedNode.cost;
                                n.setOpen(1);
                                priorityQueue.add(n);
                            }
                        }
                    }

                    showProgressOnAdapter(retrievedNode);
                    retrievedNode.setOpen(0);
                    priorityQueue.remove(retrievedNode);
                    Thread.sleep(millisecondsIncrement);
                } else {
                    stopRunningAlgorithm();
                    break;
                }
            }

            if (endNode.found) {
                return visited;
            } else {
                return new ArrayList<>();
            }
        }

        public ArrayList<Node> depthFirstSearch(Node startNode, Node endNode) throws InterruptedException {
            ArrayList<Node> visited = new ArrayList<>();
            Stack<Node> stack = new Stack<>();

            while (!(startNode == endNode)) {
                if (!stop) {
                    if (!visited.contains(startNode)) {
                        startNode.setOpen(0);
                        visited.add(startNode);
                    }
                    for (Edge e: startNode.getEdges()) {
                        Node n = e.to;
                        if (!n.isObstruction()) {
                            if (!(visited.contains(n))) {
                                n.setOpen(1);
                                stack.push(n);
                            }
                        }
                    }

                    showProgressOnAdapter(startNode);
                    if (stack.size() > 0) {
                        startNode = stack.peek();
                        stack.pop();
                        if (startNode == endNode) {
                            endNode.found = true;
                        }
                        Thread.sleep(millisecondsIncrement);
                    } else {
                        break;
                    }
                } else {
                    stopRunningAlgorithm();
                    break;
                }
            }
            if (endNode.found) {
                visited.add(endNode);
                return visited;
            } else {
                return new ArrayList<>();
            }
        }

        public void stopRunningAlgorithm() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    NodeAdapter.SAVED = false;
                    adapter.edit();
                }
            });
        }

        public ArrayList<Integer> printPath(Node endNode) {
            Node n = endNode;

            if (n == null)
                return null;

            ArrayList<Integer> values = new ArrayList<>();

            while (n.parent != null) {
                values.add(n.getValue());
                n = n.parent;
            }
            values.add(n.getValue());
            Collections.reverse(values);

            return values;
        }

        public ArrayList<Integer> printWeightlessPath(ArrayList<Node> visited) {
            ArrayList<Integer> values = new ArrayList<>();

            for (Node n:visited) {
                values.add(n.getValue());
            }

            return values;
        }

        public void showProgressOnAdapter(Node n) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.showProgress(n);
                }
            });
        }

        @Override
        public void run() {
            Node reached = null;
            ArrayList<Node> visited = null;

            switch (type) {
                case ConfigActivity.A_STAR:
                    try {
                        reached = aStar(startNode, endNode);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case ConfigActivity.DIJKSTRA:
                    try {
                        reached = dijkstra(startNode, endNode);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case ConfigActivity.BFS:
                    try {
                        visited = bestFirstSearch(startNode, endNode);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case ConfigActivity.DFS:
                    try {
                        visited = depthFirstSearch(startNode, endNode);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }

            Node finalReached = reached;
            ArrayList<Node> finalVisited = visited;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (finalReached != null) {
                        adapter.showPath(printPath(finalReached));
                    } else {
                        if (finalVisited != null) {
                            adapter.showPath(printWeightlessPath(finalVisited));
                        }
                    }
                }
            });
        }
    }
}