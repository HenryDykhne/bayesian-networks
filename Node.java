import java.util.ArrayList;
import java.util.List;

public class Node {
    private int nodeIndex;
    private String name;
    private int numValues;
    private ArrayList<String> values;
    private int numChildren;
    private ArrayList<Integer> childrenIndexes;
    private int numParents;
    private ArrayList<Integer> parentIndexes;
    private int numProbParams;
    private ArrayList<Double> probParams;

    public Node(int nodeIndex, String name, int numValues, ArrayList<String> values, int numChildren,
            ArrayList<Integer> childrenIndexes, int numParents, ArrayList<Integer> parentIndexes, int numProbParams,
            ArrayList<Double> probParams) {
        this.nodeIndex = nodeIndex;
        this.name = name;
        this.numValues = numValues;
        this.values = values;
        this.numChildren = numChildren;
        this.childrenIndexes = childrenIndexes;
        this.numParents = numParents;
        this.parentIndexes = parentIndexes;
        this.numProbParams = numProbParams;
        this.probParams = probParams;
    }

    public ArrayList<Double> getProbParams() {
        return probParams;
    }

	public String getName() {
		return name;
	}

	public int getNumProbParams() {
		return numProbParams;
	}

	public int getNumParents() {
		return numParents;
	}

	public int getValueIndex(String string) {
		return values.indexOf(string);
	}

	public int getNumValues() {
		return numValues;
	}

	public ArrayList<Integer> getParentIndicies() {
		return parentIndexes;
	}

	public int getIndex() {
		return nodeIndex;
	}

}
