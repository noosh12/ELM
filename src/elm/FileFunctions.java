package elm;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;

public class FileFunctions
{
	public static void main( String [] args )
	{
		int totalQuantity = 0;
		int count = 0;
		List<OrderItem> orderLine = new ArrayList<>();							//stores the OrderItem objects
		HashMap<String, Integer> gmdQuantities= new HashMap<String,Integer>();	//meal quantities
		HashMap<String, String> gmdNames= new HashMap<String,String>();			//meal names
		HashMap<String, ArrayList<OrderItem>> ordersByShippingMethod = new HashMap<String, ArrayList<OrderItem>> (); // Orders by shipping method
		
		ArrayList<String> skus = new ArrayList<String>();
		ArrayList<String> names = new ArrayList<String>();
		HashMap<String, String> Names= new HashMap<String,String>();
		
		ArrayList<String> orderedOld = new ArrayList<String>();
		
		System.out.println("EASY LIFE MEALS  |  GYM MEALS DIRECT");
		System.out.println();
		System.out.println("loading file input.csv ...");

		try
		{
			BufferedReader input = new BufferedReader(new FileReader("input.csv"));//Buffered Reader object instance with FileReader
			System.out.print("Reading...");
			String fileRead = input.readLine(); // Headers
			fileRead = input.readLine();
			

			while (fileRead != null)
			{
				// split input line on commas, except those between quotes ("")
				String[] tokenize = fileRead.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
				System.out.print("line size = "+tokenize.length+"   ");
				while(tokenize.length<56){
					fileRead = fileRead + input.readLine();
					tokenize = fileRead.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
					System.out.println("line size = "+tokenize.length);
				}		

				String orderID = tokenize[0];							//OrderID
				String discountCode = tokenize[12];						//coupon that was used
				String shippingMethod = tokenize[14];					//shipping method that was used
				int lineItemQuantity = Integer.parseInt(tokenize[16]);	//quantity of current item
				String lineItemName = tokenize[17];						//product name of current item
				String lineItemSKU = tokenize[20];						//SKU of current item (i.e. GMD-12)
				String billingName = tokenize[24];	
				String shippingName = tokenize[34];						//Shipping Name provided by customer
				String shippingAddress1 = tokenize[36];					//Shipping Address provided
				String shippingCity = tokenize[39];						//Shipping city provided (suburb)
				String notes=tokenize[44];								//notes provided by customer regarding shipping
				
				if(shippingName != null && !shippingName.isEmpty()){
					shippingName = billingName;
				}

				orderLine.add(
					new OrderItem(orderID, discountCode, shippingMethod, lineItemQuantity, lineItemName,
						lineItemSKU, billingName, shippingAddress1, shippingCity, notes)
				);
				
				System.out.println("Built Order "+count);
				
				System.out.print("reading in new line from file... ");
				fileRead = input.readLine();
				count+=1;
				System.out.println("line read");
			}

			input.close();
			System.out.println(" " + count + " objects built");
			System.out.println();
		}
		catch (FileNotFoundException fnfe)
		{
			System.out.println("error: file not found!");
			System.exit(1);
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(1);
		}

		String oldOrderID = "NOTAREALID";
		// Here we loop through all of our order objects to obtain useful info out of them
		for (OrderItem order : orderLine)
		{
			String sku;
			String name;

			int extraSku;
			totalQuantity += order.getLineItemQuantity();
			sku = order.getLineItemSKU();
			// Tally order quantities in a HashMap on SKU
			if (gmdQuantities.containsKey(sku)){	//if item already exists in hashMap
				name = order.getLineItemName();
				if (name.equals(gmdNames.get(sku))){ //if name matches sku value
					gmdQuantities.put(sku, gmdQuantities.get(sku) + order.getLineItemQuantity());
				}
				else{ //if name does not match sku value (very rare)
					extraSku = Integer.parseInt(sku.replaceAll("[\\D]", ""));
					if(extraSku<10)
						sku = "OLD-0"+extraSku; //duplicate sku item changes prefix to ZZZ i.e GMD-12 -> ZZZ-12
					else
						sku = "OLD-"+extraSku; //duplicate sku item changes prefix to ZZZ i.e GMD-12 -> ZZZ-12
					
					if(gmdQuantities.containsKey(sku)){
						gmdQuantities.put(sku, gmdQuantities.get(sku) + order.getLineItemQuantity());
					}
					else{
						gmdQuantities.put(sku, order.getLineItemQuantity());
						gmdNames.put(sku, order.getLineItemName());
						skus.add(sku);
						
					}
					orderedOld.add(order.getOrderID());
				}				
			} else {
				gmdQuantities.put(sku, order.getLineItemQuantity());
				gmdNames.put(sku, order.getLineItemName());
				skus.add(sku);
			}		

			// Storing the different shipping methods and the different orders to each shipping method
			if (!(order.getOrderID().equals(oldOrderID))){
				String shippingMethod = order.getShippingMethod().toLowerCase();
				if (!ordersByShippingMethod.containsKey(shippingMethod)){
					ordersByShippingMethod.put(shippingMethod, new ArrayList<OrderItem>());		
				}
				ordersByShippingMethod.get(shippingMethod).add(order);
				oldOrderID=order.getOrderID();
			}		
		}
		
		System.out.print("Sorting Meal Names...");
		for(String currentSku: skus){
			Names.put(gmdNames.get(currentSku), currentSku);
			names.add(gmdNames.get(currentSku));			
		}
		Collections.sort(names);
		skus.clear();
		for(String currentName : names){
			skus.add(Names.get(currentName));
		}
		System.out.println(" Done!");


		System.out.print("Calculating meal totals...");
		PrintMealTotals(gmdQuantities,gmdNames,totalQuantity, skus);//Prints the totals of each meal

		System.out.print("Calculating ingredient totals...");
		CalcPrintIngredients(gmdQuantities,gmdNames); //Calculate the ingredients required

		System.out.print("Printing sorted delivery methods...");
		PrintShipping(ordersByShippingMethod, orderedOld); //Print the shipping details of each order of each method
	}

	/*
	 *   Writes the total sold quantities of each menu item
	 *   to file '_meal_totals.csv'
	 */
	public static void PrintMealTotals(HashMap<String, Integer> quantities, HashMap<String, String> names, int total, ArrayList<String> skus){
		try
		{
			// creating a BufferedWriter instance with FileWriter
			// the flag set to 'true' tells it to append a file if file exists. 'false' creates/recreates the file
			BufferedWriter totals = new BufferedWriter(new FileWriter("_meal_totals.csv", false));
			String mealName;
			String sauceName;
			
			
			int[] typeTotals = new int[6];
			HashMap<String, Integer> sauceTotals = new HashMap<String,Integer>();	//sauce totals
			ArrayList<String> sauces = new ArrayList<String>();

			totals.write("TOTAL MEALS: "+total);
			totals.newLine();
			totals.newLine();
			totals.newLine();
			totals.write("NAME"+","+"TOTAL"+","+"MEAL");
			totals.newLine();

			// Write the quantities of each meal to file
			for(String sku : skus){
				totals.write(names.get(sku)+ "," +quantities.get(sku)+ ","+sku);
				totals.newLine();
				
				if (sku.contains("GMD")||sku.contains("OLD")){
					//Totalling the totals for each meal type
					mealName = names.get(sku).toLowerCase(); 				
					if(mealName.contains("rice")){
						if(mealName.contains("large"))
							typeTotals[0]+=quantities.get(sku);
						if(mealName.contains("small"))
							typeTotals[1]+=quantities.get(sku);
					}
					if(mealName.contains("potato")){
						if(mealName.contains("large"))
							typeTotals[2]+=quantities.get(sku);
						if(mealName.contains("small"))
							typeTotals[3]+=quantities.get(sku);
					}
					if(mealName.contains("veg")){
						if(mealName.contains("large"))
							typeTotals[4]+=quantities.get(sku);
						if(mealName.contains("small"))
							typeTotals[5]+=quantities.get(sku);
					}
					
					String[] mealNameSplit = names.get(sku).split(" - ");
					sauceName = mealNameSplit[0];
					
					if (sauceTotals.containsKey(sauceName)){ //if item already exists in hashMap
						sauceTotals.put(sauceName, sauceTotals.get(sauceName)+quantities.get(sku));					
					}
					else {
						sauceTotals.put(sauceName, quantities.get(sku));
						sauces.add(sauceName);
					}
				}
			}
			
					
			if (!sauceTotals.isEmpty()){
				//Writing the totals for each meal type
				totals.newLine();
				totals.newLine();
				totals.write("TYPE TOTALS"+","+"LARGE"+","+"SMALL");
				totals.newLine();
				totals.write("Rice"+","+typeTotals[0]+","+typeTotals[1]);
				totals.newLine();
				totals.write("Sweet Potato"+","+typeTotals[2]+","+typeTotals[3]);
				totals.newLine();
				totals.write("Vege"+","+typeTotals[4]+","+typeTotals[5]);
							
				//Writing the totals for each sauce type
				totals.newLine();
				totals.newLine();
				totals.newLine();
				totals.write("SAUCE TOTALS"+","+"TOTAL");				
				for (String sauce : sauces){
					totals.newLine();
					totals.write(sauce +","+sauceTotals.get(sauce));
				}
			}
				
			totals.close();
			System.out.println(" Done!");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * This method calculates the quantities of each ingredient and prints them to file.
	 * File will be '_ingredients.csv'
	 */
	public static void CalcPrintIngredients(HashMap<String, Integer> quantities, HashMap<String, String> names){

		Ingredient chicken = new Ingredient("Chicken");// create temporary instance of Ingredient object
		Ingredient beef = new Ingredient("Beef");
		Ingredient sPotato = new Ingredient("Sweet Potato");
		Ingredient rice = new Ingredient("Rice");
		Ingredient veg = new Ingredient("Veg");

		/*
		 * Totalling Ingredient Quantities
		 * This is accomplished by searching the menu item name for keywords like large, chicken, rice, etc
		 * i.e 'Pepperberry Steak - Large/Rice' would add 200g to the beef and rice quantities
		 */
		for(String sku : quantities.keySet()){
			String tempName = names.get(sku).toLowerCase();

			if(tempName.contains("large")){
				if(tempName.contains("chicken"))
					chicken.addQuantity(quantities.get(sku), 200);
				if(tempName.contains("steak"))
					beef.addQuantity(quantities.get(sku), 200);
				if(tempName.contains("potato"))
					sPotato.addQuantity(quantities.get(sku), 200);
				if(tempName.contains("rice"))
					rice.addQuantity(quantities.get(sku), 200);
				if(tempName.contains("veg"))
					veg.addQuantity(quantities.get(sku), 180);
			}
			if(tempName.contains("small")){
				if(tempName.contains("chicken"))
					chicken.addQuantity(quantities.get(sku), 150);
				if(tempName.contains("steak"))
					beef.addQuantity(quantities.get(sku), 150);
				if(tempName.contains("potato"))
					sPotato.addQuantity(quantities.get(sku), 120);
				if(tempName.contains("rice"))
					rice.addQuantity(quantities.get(sku), 120);
				if(tempName.contains("veg"))
					veg.addQuantity(quantities.get(sku), 100);
			}
		}

		// Writing ingredient quantities to file
		try
		{
			// creating a BufferedWriter instance with FileWriter
			// the flag set to 'true' tells it to append a file if file exists. 'false' creates/recreates the file
			BufferedWriter ingredientsFile = new BufferedWriter(new FileWriter("_ingredients.csv", false));

			ingredientsFile.write("INGREDIENT"+","+"kg");
			ingredientsFile.newLine(); ingredientsFile.newLine(); ingredientsFile.newLine();
			ingredientsFile.write(chicken.getName()+","+(float)chicken.getQuantity()/1000);
			ingredientsFile.newLine();
			ingredientsFile.write(beef.getName()+","+(float)beef.getQuantity()/1000);
			ingredientsFile.newLine(); ingredientsFile.newLine(); ingredientsFile.newLine();
			ingredientsFile.write(rice.getName()+","+(float)rice.getQuantity()/1000);
			ingredientsFile.newLine();
			ingredientsFile.write(sPotato.getName()+","+(float)sPotato.getQuantity()/1000);
			ingredientsFile.newLine();
			ingredientsFile.write(veg.getName()+","+(float)veg.getQuantity()/1000);

			ingredientsFile.close();
			System.out.println(" Done!");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * Prints the orders of each shipping method
	 */
	public static void PrintShipping(HashMap<String, ArrayList<OrderItem>> ordersByShippingMethod, ArrayList <String> old){

		try
		{
			// creating a BufferedWriter instance with FileWriter
			// the flag set to 'true' tells it to append a file if file exists. 'false' creates/recreates the file
			BufferedWriter shipping = new BufferedWriter(new FileWriter("_shipping.csv", false));
			shipping.write("SHIPPING METHODS: " + ordersByShippingMethod.keySet().size());
			shipping.newLine();
			shipping.newLine();
			shipping.newLine();
			shipping.newLine();
			shipping.newLine();

			//Looping through all shipping methods and within that, looping through all orders while printing
			for(String shippingMethod : ordersByShippingMethod.keySet()){
				shipping.write("     METHOD,     NAME,     ADDRESS,     NOTES,     ORDER ID");
				shipping.newLine();
				for(OrderItem order : ordersByShippingMethod.get(shippingMethod)){
					String shippingString = order.getShippingMethod() + "," + order.getShippingName() + "," + order.getShippingAddress() + ",";
					if(old.contains(order.getOrderID()))
						shippingString = shippingString + "**CONTAINS OLD** "+order.getNotes()+","+order.getOrderID();
					else
						shippingString = shippingString+order.getNotes()+","+order.getOrderID();
								
					shipping.write(shippingString);
					shipping.newLine();
				}
				shipping.write("     TOTAL: " + ordersByShippingMethod.get(shippingMethod).size());
				shipping.newLine();
				shipping.newLine();
				shipping.newLine();
			}

			shipping.close();
			System.out.println(" Done!");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(0);
		}
	}
}
