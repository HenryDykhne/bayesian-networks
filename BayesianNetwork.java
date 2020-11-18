import java.util.ArrayList;

public class BayesianNetwork {

    private int numNodes;
    private ArrayList<Node> nodes;

    public BayesianNetwork() {
        nodes = new ArrayList<>();
    }

    public void setNumNodes(int numNodes) {
        this.numNodes = numNodes;
	}

	public int getNumNodes() {
		return numNodes;
	}

	public void addNode(Node node) {
        nodes.add(node);
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }
    
    public Node getNodeByIndex(int index) {
        return nodes.get(index);
    }

    public Node getNodeByName(String name){
        for(int i = 0; i < numNodes; i ++){
            Node node = getNodeByIndex(i);
            if(node.getName().equals(name)){
                return node;
            }
        }
        return null;
    }

}