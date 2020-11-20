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
        
        System.out.println("Query var: " + x);
        System.out.print("Observation: ");
        obs.entrySet().forEach(entry -> System.out.print(entry.getKey() + "=" + entry.getValue() + " "));
        System.out.print("\n\n");
        varElim();


    }

    private static void printPotential(Potential pot) {
        //print out potentials
        System.out.println("Potential over vars: " + pot.getAllIndicies().toString());
        System.out.print("\tDimension: ");
        System.out.print("[");
        for(int k = 0; k < pot.getAllIndicies().size(); k++){
            System.out.print(bn.getNodeByIndex(pot.getAllIndicies().get(k)).getNumValues());
            if(k != pot.getAllIndicies().size() - 1) {
                System.out.print(",");
            }
        }
        System.out.print("]\n");
        System.out.println("\tvector: " + pot.getProbParams().toString());
    }

    private static Potential varElim() {
        //set of all cpt's in s
        ArrayList<Potential> cpts = new ArrayList<>();
        System.out.println("Initial potentials:");
        for(int i = 0; i < bn.getNumNodes(); i++) {
            //modify to ensure they are added in reverse index order
            cpts.add(new Potential(bn.getNodeByIndex(i)));
            printPotential(cpts.get(i));
        }

        System.out.println("\nConstrain potentials:");
        for(String nodeName : obs.keySet()) {
            Node node = bn.getNodeByName(nodeName);

            //select potential to constrain
            Potential toConstrain = null;
            for (Potential potential: cpts) {
                if(potential.getNodeIndex() == node.getIndex()) {
                    toConstrain = potential;
                    break;
                }
            }
            
            if(toConstrain == null) {
                return null;
            }
            System.out.println("By: v=" + node.getIndex() + " u=" + node.getValueIndex(obs.get(nodeName)));
            //constrain
            for(int i = 0; i < node.getNumProbParams(); i++) {
                if((i - node.getValueIndex(obs.get(nodeName))) % (node.getNumValues()) != 0) {
                    toConstrain.getProbParams().set(i, 0.0);
                }
            }
            //print out constraining
            printPotential(toConstrain);
        }


        ArrayList<Node> remainingVarsToElim = (ArrayList<Node>)bn.getNodes().clone();
        remainingVarsToElim.remove(bn.getNodeByName(x));
        System.out.print("\nVariables to eliminate: ");
        for(int k = 0; k < remainingVarsToElim.size(); k++){
            System.out.print(remainingVarsToElim.get(k).getIndex() + " ");
        }
        System.out.print("\n\n");

        System.out.println("Potentials before elimination: ");
        for(Potential pot : cpts){
            printPotential(pot);
        }

        for(int i = 0; i < bn.getNumNodes() - 1; i++) {
            //selectVariable
            Node node = selectVariable(cpts, remainingVarsToElim);
            System.out.println("\nEliminate var " + node.getIndex());
            ArrayList<Potential> potentialsOverNodeQ = new ArrayList<>();
            for(Potential potential : cpts) {
                if(potential.overNode(node)) {
                    potentialsOverNodeQ.add(potential);
                    printPotential(potential);
                }
            }
            Potential potentialB1 = multiply(potentialsOverNodeQ);
            Potential potentialB2 = marginOut(potentialB1, node);

            System.out.println("New pot after var " + node.getIndex() + " is summed out:");
            printPotential(potentialB2);

            //remove everything in potentials Over Node from cpts and add potential B2
            cpts.removeAll(potentialsOverNodeQ);
            cpts.add(potentialB2);

            System.out.println("Potentials after eliminating var: " + node.getIndex());
            for(Potential pot : cpts){
                printPotential(pot);
            }
        }

        System.out.println("\nGet final result: ");
        for(Potential pot : cpts){
            printPotential(pot);
        }
        Potential potentialF = multiply(cpts);
        normalize(potentialF);
        return potentialF;
    }

    private static void normalize(Potential potentialF) {
        System.out.println("\nNormalize to P( var " + potentialF.getAllIndicies().toString() + " " + x + "| obs):");
        Double alpha = 0.0;
        for(int i = 0; i < potentialF.getProbParams().size(); i++){
            alpha += potentialF.getProbParams().get(i);
        }
        if(alpha == 0){
            return;
        }

        for(int i = 0; i < potentialF.getProbParams().size(); i++){
            potentialF.getProbParams().set(i, potentialF.getProbParams().get(i) / alpha);
        }
        printPotential(potentialF);
    }

    private static Potential marginOut(Potential potentialB1, Node nodeToBeMarginedOut) {
        ArrayList<Node> resultPotentialVariables = new ArrayList<>();
        for(int nodeIndex : potentialB1.getAllIndicies()) {
            if(nodeIndex != nodeToBeMarginedOut.getIndex()) {
                resultPotentialVariables.add(bn.getNodeByIndex(nodeIndex));
            }
        }
        //initializing new potenial to zeroes
        int potSize = 1;
        for(Node node : resultPotentialVariables){
            potSize *= node.getNumValues();
        }
        Potential newPotentialF = new Potential(resultPotentialVariables, new ArrayList<Double>(Collections.nCopies(potSize, 0.0)));

        for(int i = 0; i < newPotentialF.getProbParams().size(); i++) {
            ArrayList<Integer> domainSizesY = new ArrayList<>();
            ArrayList<Node> orderedPotentialVars = new ArrayList<>();
            //orderingNodes
            for(int j = bn.getNumNodes() - 1; j >=0; j--) {
                Node temp = bn.getNodeByIndex(j);
                if(resultPotentialVariables.contains(temp)){
                    orderedPotentialVars.add(temp);
                    domainSizesY.add(temp.getNumValues());
                }
            }
            ArrayList<Integer> asgmt = indexToAsgn(i, domainSizesY);
            for(int j = 0; j < nodeToBeMarginedOut.getNumValues(); j++) {
                //adding assigments in order
                Boolean added = false;
                ArrayList<Integer> fullAssigment = new ArrayList<>();
                ArrayList<Integer> fullDomainSizes = new ArrayList<>();
                for(int k = 0; k < orderedPotentialVars.size(); k++){
                    if(!added && orderedPotentialVars.get(k).getIndex() < nodeToBeMarginedOut.getIndex()){
                        fullAssigment.add(j);
                        fullDomainSizes.add(nodeToBeMarginedOut.getNumValues());
                        added = true;
                    }
                    fullAssigment.add(asgmt.get(k));
                    fullDomainSizes.add(orderedPotentialVars.get(k).getNumValues());
                }
                if(!added){
                    fullAssigment.add(j);
                    fullDomainSizes.add(nodeToBeMarginedOut.getNumValues());
                }
                int p = assigmentToIndex(fullAssigment, fullDomainSizes);
                newPotentialF.getProbParams().set(i, newPotentialF.getProbParams().get(i) + potentialB1.getProbParams().get(p));
            }
        }
        return newPotentialF;
    }

    private static int assigmentToIndex(ArrayList<Integer> assigments, ArrayList<Integer> domainSize) {
        int n = 0;
        for(int i = 0; i < assigments.size(); i++){
            int w = 1;
            for(int j = 0; j < i; j++){
                w *= domainSize.get(j);
            }
            n += assigments.get(i) * w;
        }
        return n;
    }

    private static Potential multiply(ArrayList<Potential> potentialsOverNode) {
        System.out.println("Multiplying");
        for(Potential pot : potentialsOverNode){
            printPotential(pot);
        }
        ArrayList<Node> nodesZ = new ArrayList<>();//double check this bit
        for(Potential potential : potentialsOverNode){
            for(int nodeIndex : potential.getAllIndicies()) {
                if(!nodesZ.contains(bn.getNodeByIndex(nodeIndex))) {
                    nodesZ.add(bn.getNodeByIndex(nodeIndex));
                }
            }
        }

        int potSize = 1;
        for(Node node : nodesZ){
            potSize *= node.getNumValues();
        }
        Potential newPotentialF = new Potential(nodesZ, new ArrayList<>(Collections.nCopies(potSize, 1.0)));
        //notDoneyet
        for(int assigmentNum = 0; assigmentNum < newPotentialF.getProbParams().size(); assigmentNum++) {
            for(int i = 0; i < potentialsOverNode.size(); i++) {
                int projectedAssigmentNum = projectAssigmentNum(assigmentNum, nodesZ, potentialsOverNode.get(i));
                newPotentialF.getProbParams().set(assigmentNum, newPotentialF.getProbParams().get(assigmentNum) * potentialsOverNode.get(i).getProbParams().get(projectedAssigmentNum));
            }
        }
        System.out.println("Yielding");
        printPotential(newPotentialF);
        return newPotentialF;
    }

    private static int projectAssigmentNum(int assigmentNum, ArrayList<Node> nodesZ, Potential potential) {
        ArrayList<Node> fullList = new ArrayList<>();
        ArrayList<Integer> domainSize = new ArrayList<>();
        
        for(int j = bn.getNumNodes() - 1; j >=0; j--) {
            Node temp = bn.getNodeByIndex(j);
            if(nodesZ.contains(temp)){
                fullList.add(temp);
                domainSize.add(temp.getNumValues());
            }
        }

        ArrayList<Integer> assigmentsCorrespondingToList = indexToAsgn(assigmentNum, domainSize);
        ArrayList<Node> projectedList = new ArrayList<>();
        ArrayList<Integer> assigmentsCorrespondingToProjection = new ArrayList<>();
        ArrayList<Integer> projectedDomainSize = new ArrayList<>();
        for(int i = 0; i < fullList.size(); i++) {
            Node temp = fullList.get(i);
            if(potential.getAllIndicies().contains(temp.getIndex())){
                projectedList.add(temp);
                projectedDomainSize.add(temp.getNumValues());
                assigmentsCorrespondingToProjection.add(assigmentsCorrespondingToList.get(i));
            }
        }

        return assigmentToIndex(assigmentsCorrespondingToProjection, projectedDomainSize);
    }

    private static ArrayList<Integer> indexToAsgn(int n, ArrayList<Integer> b){
        int k = b.size();
        ArrayList<Integer> w = new ArrayList<>();
        w.add(1);
        for(int i = 1; i < k; i++) {
            w.add(w.get(i-1)*b.get(i-1));
        }

        ArrayList<Integer> a = new ArrayList<>();
        for(int i = 0; i < k-1; i++) {
            a.add((n % w.get(i + 1)) / w.get(i));
        }
        a.add(n / w.get(k - 1));
        return a;
    }

    private static Node selectVariable(ArrayList<Potential> cpts, ArrayList<Node> remainingVarsToElim) {
        Node toEliminate = null;
        HashMap<Node, ArrayList<Integer>> u = new HashMap<>();
        for(Node node : remainingVarsToElim) {
            //if not the node we want to keep
            if(!node.getName().equals(x)) {
                u.put(node, new ArrayList<>());
                for(Potential potential : cpts) {
                    if(potential.getAllIndicies().contains(node.getIndex())){
                        for(int i = 0; i < potential.getAllIndicies().size(); i++) {
                            if(!u.get(node).contains(potential.getAllIndicies().get(i))) {
                                u.get(node).add(potential.getAllIndicies().get(i));
                            }
                        }
                    }
                }
            }
        }

        for(Node node : remainingVarsToElim) {
            if(toEliminate == null || u.get(toEliminate).size() > u.get(node).size()) {
                toEliminate = node;
            }
        }
        
        remainingVarsToElim.remove(toEliminate);
        return toEliminate;
    }

    public static void parseBn(String bnFilename) {
        bn = new BayesianNetwork();
        try (Scanner scanner = new Scanner(new File(bnFilename))) {
            String line = scanner.nextLine();

            // get number of variables
            bn.setNumNodes(Integer.parseInt(line.split(" ")[0]));

            // read in each node paragraph
            for (int i = 0; i < bn.getNumNodes(); i++) {
                parseNode(scanner, i);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void parseNode(Scanner scanner, int nodeIndex) {
        String line;

        scanner.nextLine(); // eat empty line

        // get size of state space for this node
        line = scanner.nextLine();
        int numValues = Integer.parseInt(line.split(" ")[0]);

        // get name of node
        line = scanner.nextLine();
        String name = line.split(" ")[0];

        // get values of node
        ArrayList<String> values = new ArrayList<>();
        for (int i = 0; i < numValues; i++) {
            line = scanner.nextLine();
            values.add(line);
        }

        // get children nodes
        ArrayList<Integer> childrenIndexes = new ArrayList<>();
        line = scanner.nextLine();
        int numChildren = Integer.parseInt(line.split(" ")[0]);
        for (int i = 0; i < numChildren; i++) {
            childrenIndexes.add(Integer.parseInt(line.split(" ")[i + 2])); // offset accounts for number of spaces and
                                                                           // the first number specifying number of
                                                                           // children
        }

        // get parent nodes
        ArrayList<Integer> parentIndexes = new ArrayList<>();
        line = scanner.nextLine();
        int numParents = Integer.parseInt(line.split(" ")[0]);
        for (int i = 0; i < numParents; i++) {
            parentIndexes.add(Integer.parseInt(line.split(" ")[i + 2])); // offset accounts for number of spaces and the
                                                                         // first number specifying number of parents
        }

        scanner.nextLine(); // eat unused coordinate line

        // get probability params
        ArrayList<Double> probParams = parseProbParams(scanner);
        int numProbParams = probParams.size();

        Node node = new Node(nodeIndex, name, numValues, values, numChildren, childrenIndexes, numParents,
                parentIndexes, numProbParams, probParams);
        bn.addNode(node);
    }

    private static ArrayList<Double> parseProbParams(Scanner scanner) {
        String line;
        ArrayList<Double> probParams = new ArrayList<>();

        // parsing index of number of probability assigments and calculating number of
        // lines
        line = scanner.nextLine();
        int numProbParams = Integer.parseInt(line.split(" |\t")[0]);
        int numLinesOfProbParams = (int) Math.ceil((double) numProbParams / 5);

        // reads the list of valid assigments into the constraint
        for (int i = 0; i < numLinesOfProbParams; i++) {
            line = scanner.nextLine();
            for (int j = 0; (j < 5) && (j + 5 * i < numProbParams); j++) {
                probParams.add(Double.parseDouble(line.split(" ")[j]));
            }
        }
        return probParams;
    }

    public static void parseObs(String obsFilename) {
        obs = new HashMap<>();
        try (Scanner scanner = new Scanner(new File(obsFilename))) {
            String line;

            // get x variable
            x = scanner.nextLine();

            // read in each node paragraph
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                obs.put(line.split(" ")[0], line.split(" ")[1]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

