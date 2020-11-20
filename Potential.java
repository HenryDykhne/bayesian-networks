import java.util.ArrayList;

public class Potential {
    private int nodeIndex;
    private ArrayList<Integer> parentIndicies;
    private ArrayList<Double> probParams;
    private ArrayList<Integer> allIndicies;

    public Potential(Node node) {
        this.parentIndicies = (ArrayList<Integer>)node.getParentIndicies().clone();
        this.nodeIndex = node.getIndex();
        this.probParams = (ArrayList<Double>)node.getProbParams().clone();
        this.allIndicies = (ArrayList<Integer>)parentIndicies.clone();
        this.allIndicies.add(this.nodeIndex);
    }

    public Potential(ArrayList<Integer> parentIndicies, int nodeIndex, ArrayList<Double> probParams) {
        this.parentIndicies = parentIndicies;
        this.nodeIndex = nodeIndex;
        this.probParams = probParams;
        this.allIndicies = (ArrayList<Integer>)parentIndicies.clone();
        this.allIndicies.add(this.nodeIndex);
    }

    public Potential(ArrayList<Node> parentIndicies, ArrayList<Double> probParams) {
        this.parentIndicies = new ArrayList<>();
        this.allIndicies = new ArrayList<>();
        for(Node node : parentIndicies) {
            this.parentIndicies.add(node.getIndex());
        }
        this.nodeIndex = -1;
        this.probParams = probParams;
        for(Node node : parentIndicies){
            this.allIndicies.add(node.getIndex());
        }
    }

	public int getNodeIndex() {
		return nodeIndex;
	}

	public ArrayList<Double> getProbParams() {
		return probParams;
	}

	public boolean overNode(Node node) {
        if(parentIndicies.contains(node.getIndex()) || nodeIndex == node.getIndex()) {
            return true;
        }
		return false;
	}

	public ArrayList<Integer> getAllIndicies() {
		return allIndicies;
	}

}
