import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Main {
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            int numTasks = Integer.parseInt(br.readLine());
            for (int t = 1; t <= numTasks; t++) {
                String[] levelsAndLifts = br.readLine().split(" ");
                int levels = Integer.parseInt(levelsAndLifts[0]);
                int lifts = Integer.parseInt(levelsAndLifts[1]);
                Graph graph = new Graph(levels);

                for (int i = 0; i < lifts; i++) {
                    String[] lift = br.readLine().split(" ");
                    for (int j = Integer.parseInt(lift[0]); j < Integer.parseInt(lift[1]); j += Integer.parseInt(lift[2])) {
                        int dest = j + Integer.parseInt(lift[2]);
                        graph.addEdges(j, dest, i + 1);
                    }
                }

                BreadthFirstSearch bfs = new BreadthFirstSearch(graph);
                String[] personRoute = br.readLine().split(" ");
                bfs.execute(Integer.parseInt(personRoute[0]), Integer.parseInt(personRoute[1]));

                String output = "";
                for(int i = bfs.getRoute().size() - 1; i >= 0; i--) {
                    output += bfs.getRoute().get(i);
                }
                System.out.println(t + " " + output);
            }
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }
}

class BreadthFirstSearch {
    private Queue<Vertex> queue;
    private ArrayList<LiftLevel> route;
    private Graph graph;

    BreadthFirstSearch(Graph graph) {
        this.queue = new LinkedList<>();
        this.graph = graph;
        this.route = new ArrayList<>();
    }

    ArrayList<LiftLevel> getRoute() { return route; }

    void execute(int source, int destination) {
        queue.add(graph.getVertex(source));
        while(!queue.isEmpty()) {
            Vertex vtx = queue.poll();
            vtx.setState(State.FOUND);

            Map<Vertex,Edge> adj = graph.getAdjVertices(vtx);

            for(Map.Entry<Vertex,Edge> entry : adj.entrySet()) {
                Vertex key = entry.getKey();
                Edge val = entry.getValue();

                if(key.getState()== State.FOUND){
                    int posDist = vtx.getDistance();
                    posDist+=val.getStep();

                    if(posDist<key.getDistance()) {
                        key.setPrevious(vtx);
                        key.setDistance(posDist);
                        key.setUsedEdge(val);
                    }
                }

                if(key.getState()== State.NONE) {
                    key.setState(State.FOUND);
                    key.setPrevious(vtx);

                    int currDistance = key.getDistance();
                    currDistance+=val.getStep();

                    key.setDistance(currDistance);
                    key.setUsedEdge(val);

                    queue.add(key);
                }
            }
            vtx.setState(State.DONE);
        }
        Vertex d = graph.getVertex(destination);
        otherPath(d);
    }

    private void otherPath(Vertex dest) {
        if(dest != null) {
            Edge e = dest.getUsedEdge();
            if(e != null) {
                int elev = dest.getUsedEdge().getT();
                int level = ((dest.getValue() < dest.getPrevious().getValue()) ? 2 * elev : 2 * elev - 1);

                if(route.size() > 0) {
                    LiftLevel lastElement = route.get(route.size() - 1);
                    if(lastElement.getLift() != level)
                        route.add(new LiftLevel(level, dest.getValue()));
                } else {
                    route.add(new LiftLevel(level, dest.getValue()));
                }
            }
            otherPath(dest.getPrevious());
        }
    }
}

class LiftLevel {
    private int level, lift;

    LiftLevel(int lift, int level) {
        this.lift = lift;
        this.level = level;
    }

    int getLift() { return lift; }

    @Override
    public String toString() {
        return "(" + lift + "," + level + ")";
    }
}

class Graph {
    private Map<Vertex,ArrayList<Edge>> adjMap;

    Graph(int v) {
        this.adjMap = new HashMap<>();
        for(int i=0; i <= v;i++) {
            adjMap.put(new Vertex(i), new ArrayList<>());
        }
    }

    void addEdges(int a, int b, int t) {
        Vertex v1 = this.getVertex(a);
        Vertex v2 = this.getVertex(b);

        Edge e1 = new Edge(a,Math.abs((a-b)));
        Edge e2 = new Edge(b,Math.abs(((a-b))));

        e1.setT(t);
        e2.setT(t);

        adjMap.get(v2).add(e1);
        adjMap.get(v1).add(e2);
    }

    Map<Vertex,Edge> getAdjVertices(Vertex target) {
        Map<Vertex,Edge> mapAdjVerticesEdges = new HashMap<>();
        for(Edge e : adjMap.get(target)) {
            mapAdjVerticesEdges.put(getVertex(e.getDestination()),e);
        }
        return mapAdjVerticesEdges;
    }

    Vertex getVertex(int v2) {
        for(Vertex v1 : adjMap.keySet()) {
            if (v1.equals(new Vertex(v2)))
                return v1;
        }
        return null;
    }
}

class Vertex  {
    private int value, distance;
    private Edge usedEdge;
    private Vertex previous;
    private State state;

    Vertex(int value) {
        this.value = value;
        this.distance = 0;
        this.state = State.NONE;
        this.previous = null;
        this.usedEdge = null;
    }

    Vertex getPrevious() { return previous; }
    Edge getUsedEdge() { return usedEdge; }
    int getValue() { return value; }
    State getState() { return state; }
    Integer getDistance() { return distance; }

    void setPrevious(Vertex previous) { this.previous = previous; }
    void setUsedEdge(Edge usedEdge) { this.usedEdge = usedEdge; }
    void setState(State state) { this.state = state; }
    void setDistance(int distance) { this.distance = distance; }

    public int hashCode() { return value; }
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Vertex other = (Vertex) obj;
        return value == other.value;
    }
}

class Edge {
    private int dest, step, t;

    Edge(int dest, int step) {
        this.dest = dest;
        this.step = step;
    }

    int getDestination() { return dest; }
    int getStep() { return step; }
    int getT() { return t; }

    void setT(int t) { this.t = t; }
}

enum State {NONE, FOUND, DONE}
