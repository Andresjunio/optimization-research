

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.gurobi.gurobi.GRB;
import com.gurobi.gurobi.GRBEnv;
import com.gurobi.gurobi.GRBLinExpr;
import com.gurobi.gurobi.GRBModel;
import com.gurobi.gurobi.GRBVar;

public class MTZ_Model {
	
	/*
	 * This is part of a personal optimization studies, make by Andres Junio
	 */
	
	public static List<List<Integer>> convertDataToAGraph(String filePath) throws Exception{
		List<List<Integer>> graph = new ArrayList<List<Integer>>();
		try {
			File file = new File(filePath);
			Scanner input = new Scanner(file);
			while(input.hasNextLine()) {
				String line = input.nextLine();
				String[] numbersStr = line.split("\t");
				List<Integer> numbers = new ArrayList<Integer>();
				for(String number : numbersStr) {
					numbers.add(Integer.parseInt(number));
				}
				graph.add(numbers);
			}
			input.close();
			return graph;
		}catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static void main(String[] args) throws Exception {
	
		List<List<Integer>> costMatrix = (List<List<Integer>>) convertDataToAGraph("src/archives/cities.txt");
		int numberOfCities = costMatrix.size();
//		double[] origin = new double[numberOfCities];
//		double[] destinations = new double[numberOfCities];
//		
//		double[] aux = new double[numberOfCities -1];
		
//		Create a index of origins and destinations
//		for(int i=1;i<=numberOfCities;i++) {
//			origin[i-1] = (double) i;
//			destinations[i-1] = (double) i;
//		}
		
//		Create a index to a support variable
//		for(int i=2;i<=numberOfCities;i++) {
//			aux[i] = (double) i;
//		}
		
		GRBEnv env = new GRBEnv();
		GRBModel model = new GRBModel(env);
		
//		Binary decision variable creation
		GRBVar[][] x = new GRBVar[numberOfCities][numberOfCities];
		for(int i=0;i<numberOfCities;i++) {
			for(int j=0;j<numberOfCities;j++) {
				double actualCost = (double) costMatrix.get(i).get(j);
				x[i][j] = model.addVar(0, 1, actualCost, GRB.BINARY, null);
			}
		}
		
//		u decision variable
		GRBVar[] u = new GRBVar[numberOfCities];
		for(int i=0;i<numberOfCities;i++) {
			u[i] = model.addVar(0, numberOfCities-1, 0, GRB.INTEGER, null);
		}
		
//		only one city is visited constraint
		for(int i=0;i< numberOfCities; i++) {
			GRBLinExpr expression = new GRBLinExpr();
			for(int j=0;j<numberOfCities;j++) {
				expression.addTerm(1.0, x[i][j]);
			}
			model.addConstr(expression, GRB.EQUAL, 1.0, null);
		}
		
//		only one city to visit constraint
		for(int j=0;j<numberOfCities;j++) {
			GRBLinExpr expression = new GRBLinExpr();
			for(int i=0;i<numberOfCities;i++) {
				expression.addTerm(1.0, x[i][j]);
			}
			model.addConstr(expression, GRB.EQUAL, 1.0, null);
		}
		
//		no sub-paths constraint
		for (int i = 1; i < numberOfCities; i++) {
	        for (int j = 1; j < numberOfCities; j++) {
	            if (i != j) {
	                GRBLinExpr noSubPathConstraint = new GRBLinExpr();
	                noSubPathConstraint.addTerm(1.0, u[i]);
	                noSubPathConstraint.addTerm(-1.0, u[j]);
	                noSubPathConstraint.addTerm(numberOfCities - 1, x[i][j]);
	                model.addConstr(noSubPathConstraint, GRB.LESS_EQUAL, numberOfCities - 2, null);
	            }
	        }
	    }
		
//		set objective function
		GRBLinExpr objective = new GRBLinExpr();
		for(int i=0;i<numberOfCities;i++) {
			for(int j=0;j<numberOfCities;j++) {
				double actualCost = costMatrix.get(i).get(j);
				objective.addTerm(actualCost, x[i][j]);
			}
		}
		
		model.setObjective(objective, GRB.MINIMIZE);
		model.optimize();
		
	    if (model.get(GRB.IntAttr.Status) == GRB.OPTIMAL) {
	        System.out.println("Minimal distance: " + model.get(GRB.DoubleAttr.ObjVal));
	
//	        show the path
	        System.out.print("Path: ");
	        for (int i = 0; i < numberOfCities; i++) {
	            for (int j = 0; j < numberOfCities; j++) {
	                if (x[i][j].get(GRB.DoubleAttr.X) == 1) {
	                    System.out.print(i + ", " + j +"\n");
	                }
	            }
	        }
	    } else {
	        System.out.println("Cannot find the optimal solution.");
	    }
	}
}
