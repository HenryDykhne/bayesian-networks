import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

public class VarElim {
    private static BayesianNetwork bn;
    private static HashMap<String, String> obs;
    private static String x;

    public static void main(String[] args) {
        parseBn(args[0]);
        parseObs(args[1]);
        varElim();
        System.out.println("hello world");
    }

    private static void varElim() {
        //set of all cpt's in s
        ArrayList<ArrayList<Double>> cpts = new ArrayList<>();
        for(int i = 0; i < bn.getNumNodes(); i++) {
            cpts.add(bn.getNodeByIndex(i).getProbParams());
        }

        for(String varName: obs.keySet()) {
            Node node = bn.getNodeByName(varName);
            //constrain
            node.getProbParams();
        }
    }

    public static void parseBn(String bnFilename) {
        bn = new BayesianNetwork();
        try (Scanner scanner = new Scanner(new File(bnFilename))) {
            String line = scanner.nextLine();

            // get number of variables
            bn.setNumNodes(Integer.parseInt(line.split(" ")[0]));

            //read in each node paragraph
            for(int i = 0; i < bn.getNumNodes(); i++) {
                parseNode(scanner, i);
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

    private static void parseNode(Scanner scanner, int nodeIndex) {
        String line; 

        scanner.nextLine(); //eat empty line

        //get size of state space for this node
        line = scanner.nextLine();
        int numValues = Integer.parseInt(line.split(" ")[0]);
        
        //get name of node
        line = scanner.nextLine();
        String name = line.split(" ")[0];

        //get values of node 
        ArrayList<String> values = new ArrayList<>();
        for(int i = 0; i < numValues; i++) {
            line = scanner.nextLine();
            values.add(line);
        }

        //get children nodes
        ArrayList<Integer> childrenIndexes = new ArrayList<>();
        line = scanner.nextLine();
        int numChildren = Integer.parseInt(line.split(" ")[0]);
        for(int i = 0; i < numChildren; i++) {
            childrenIndexes.add(Integer.parseInt(line.split(" ")[i + 2])); //offset accounts for number of spaces and the first number specifying number of children
        }

        //get parent nodes
        ArrayList<Integer> parentIndexes = new ArrayList<>();
        line = scanner.nextLine();
        int numParents = Integer.parseInt(line.split(" ")[0]);
        for(int i = 0; i < numParents; i++) {
            parentIndexes.add(Integer.parseInt(line.split(" ")[i + 2])); //offset accounts for number of spaces and the first number specifying number of parents
        }

        scanner.nextLine(); //eat unused coordinate line

        //get probability params
        ArrayList<Double> probParams = parseProbParams(scanner);
        int numProbParams = probParams.size();

        Node node = new Node(nodeIndex, name, numValues, values, numChildren, childrenIndexes, numParents, parentIndexes, numProbParams, probParams);
        bn.addNode(node);
    }

    private static ArrayList<Double> parseProbParams(Scanner scanner) {
        String line;
        ArrayList<Double> probParams = new ArrayList<>();

        //parsing index of number of probability assigments and calculating number of lines
        line = scanner.nextLine();
        int numProbParams = Integer.parseInt(line.split(" |\t")[0]);
        int numLinesOfProbParams = (int) Math.ceil((double) numProbParams / 5);

        //reads the list of valid assigments into the constraint
        for(int i = 0; i < numLinesOfProbParams; i++) {
            line = scanner.nextLine();
            for(int j = 0; (j < 5) && (j + 5 * i < numProbParams); j++){
                probParams.add(Double.parseDouble(line.split(" ")[j]));
            }
        }
        return probParams;
    }

    public static void parseObs(String obsFilename) {
        obs = new HashMap<>();
        try (Scanner scanner = new Scanner(new File(obsFilename))) {
            String line = scanner.nextLine();

            // get x variable
            x = scanner.nextLine();

            //read in each node paragraph
            while(scanner.hasNextLine()) {
                line = scanner.nextLine();
                obs.put(line.split(" ")[0], line.split(" ")[1]);
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }
}

